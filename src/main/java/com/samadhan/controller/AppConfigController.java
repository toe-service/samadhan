package com.samadhan.controller;

import com.samadhan.dto.AppConfigResponse;
import com.samadhan.entity.AppVersionConfig;
import com.samadhan.repository.AppVersionConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// POST /v1/admin/app-config   — upsert a version entry (X-Admin-Key required)
// GET  /v1/app-config?appId=  — read current config for an app (public)
@RestController
@RequestMapping("/v1")
public class AppConfigController {

    private final AppVersionConfigRepository repository;

    @Value("${admin.reconcile.key:}")
    private String adminKey;

    public AppConfigController(AppVersionConfigRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/app-config")
    public ResponseEntity<AppConfigResponse> getAppConfig(@RequestParam String appId) {
        return repository.findById(appId)
                .map(cfg -> ResponseEntity.ok(new AppConfigResponse(cfg.getMinVersion(), cfg.isForceUpdate())))
                .orElse(ResponseEntity.notFound().build());
    }

    // Upsert: creates or updates the version config for an appId.
    // Protected by the same X-Admin-Key used for /pay/admin/reconcile-payment.
    //
    // Example:
    //   POST /v1/admin/app-config
    //   X-Admin-Key: <ADMIN_RECONCILE_KEY>
    //   Body: { "appId": "com.transfereaze.user", "minVersion": "1.2.0", "forceUpdate": true }
    @PostMapping("/admin/app-config")
    public ResponseEntity<String> upsertAppConfig(
            @RequestHeader(value = "X-Admin-Key", required = false) String key,
            @RequestBody AppVersionConfig body) {

        if (adminKey == null || adminKey.isBlank() || key == null || !adminKey.equals(key)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        if (body.getAppId() == null || body.getAppId().isBlank()) {
            return ResponseEntity.badRequest().body("appId is required");
        }
        if (body.getMinVersion() == null || body.getMinVersion().isBlank()) {
            return ResponseEntity.badRequest().body("minVersion is required");
        }

        AppVersionConfig cfg = repository.findById(body.getAppId()).orElse(new AppVersionConfig());
        cfg.setAppId(body.getAppId());
        cfg.setMinVersion(body.getMinVersion());
        cfg.setForceUpdate(body.isForceUpdate());
        repository.save(cfg);

        return ResponseEntity.ok("App config updated: " + body.getAppId() + " → " + body.getMinVersion());
    }
}
