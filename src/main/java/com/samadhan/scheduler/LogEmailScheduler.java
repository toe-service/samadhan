package com.samadhan.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Emails the previous day's server log file(s) to a fixed recipient every night. Runs at 12:10
// AM IST (not exactly midnight) so Spring Boot's default logback rolling policy — which rotates
// on the next log write after the date changes, not proactively at 00:00:00 — has a few minutes
// to have actually rotated logs/app.log into logs/app.log.<yesterday>.*.gz before this looks for
// it. Note: the other schedulers in this package (SubscriptionScheduler etc.) don't pin a zone,
// so they run on the server's default timezone (likely UTC on Railway) rather than IST — this one
// deliberately does, since "12AM" for a log email should mean IST midnight, not UTC midnight.
@Component
public class LogEmailScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LogEmailScheduler.class);
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Stay comfortably under Gmail's ~25MB total message size cap.
    private static final long MAX_ATTACHMENT_BYTES = 20L * 1024 * 1024;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${logging.file.name:logs/app.log}")
    private String activeLogFilePath;

    @Value("${logs.email.recipient}")
    private String recipient;

    @Value("${logs.email.enabled:true}")
    private boolean enabled;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Kolkata")
    public void emailYesterdaysLogs() {
        if (!enabled) {
            return;
        }
        // spring.mail.host is hardcoded in every profile, so mailSender itself always exists —
        // the real "is this actually configured" check is whether a username was ever set.
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            logger.warn("Skipping daily log email — MAIL_USERNAME/MAIL_APP_PASSWORD not configured.");
            return;
        }

        Path activeLog = Paths.get(activeLogFilePath);
        Path logsDir = activeLog.toAbsolutePath().getParent();
        String baseName = activeLog.getFileName().toString(); // "app.log"
        // Pinned to IST, not the JVM default zone — the cron trigger fires at true IST midnight
        // (see @Scheduled's zone below), so "yesterday" must be computed in that same zone or a
        // UTC-default server would look for the wrong day's rotated file at the exact trigger
        // moment (00:10 IST = 18:40 UTC the *same* calendar day the file was written for).
        LocalDate yesterday = LocalDate.now(IST).minusDays(1);
        String datePrefix = baseName + "." + DATE_FORMAT.format(yesterday) + ".";

        List<Path> attachments = new ArrayList<>();
        if (logsDir != null && Files.isDirectory(logsDir)) {
            try (DirectoryStream<Path> stream =
                    Files.newDirectoryStream(logsDir, p -> p.getFileName().toString().startsWith(datePrefix))) {
                for (Path p : stream) {
                    attachments.add(p);
                }
            } catch (IOException e) {
                logger.error("Failed to list rotated log files in {}: {}", logsDir, e.getMessage(), e);
            }
        }
        attachments.sort(Comparator.comparing(Path::getFileName));

        // Rotation may not have happened yet (no log line was written since midnight to trigger
        // it) — fall back to the still-active file rather than sending nothing.
        boolean usingFallback = false;
        if (attachments.isEmpty()) {
            if (Files.exists(activeLog)) {
                attachments.add(activeLog);
                usingFallback = true;
            } else {
                logger.warn("No rotated log for {} and no active log file found at {} — nothing to email.",
                        yesterday, activeLog);
                return;
            }
        }

        long totalBytes = 0;
        for (Path p : attachments) {
            try {
                totalBytes += Files.size(p);
            } catch (IOException e) {
                logger.warn("Could not read size of {}: {}", p, e.getMessage());
            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailUsername);
            helper.setTo(recipient);
            helper.setSubject("TransferEaze server logs — " + DATE_FORMAT.format(yesterday));

            StringBuilder body = new StringBuilder();
            body.append("Server logs for ").append(DATE_FORMAT.format(yesterday)).append(".\n\n");
            if (usingFallback) {
                body.append("Note: the log hadn't rotated for the new day yet at send time, so this is ")
                        .append("the live app.log file as of 12:10 AM IST (may include a few minutes of today's logs too).\n\n");
            }

            if (totalBytes > MAX_ATTACHMENT_BYTES) {
                body.append("Log files totalled ~").append(totalBytes / (1024 * 1024))
                        .append("MB, too large to attach. Files (on the server, under ").append(logsDir).append("):\n");
                for (Path p : attachments) {
                    body.append(" - ").append(p.getFileName()).append("\n");
                }
                helper.setText(body.toString());
            } else {
                body.append("Attached: ").append(attachments.size()).append(" file(s).");
                helper.setText(body.toString());
                for (Path p : attachments) {
                    helper.addAttachment(p.getFileName().toString(), p.toFile());
                }
            }

            mailSender.send(message);
            logger.info("Sent daily log email to {} with {} attachment(s) ({} bytes)",
                    recipient, attachments.size(), totalBytes);
        } catch (Exception e) {
            logger.error("Failed to send daily log email: {}", e.getMessage(), e);
        }
    }
}
