package com.samadhan.service;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.InvalidCredentialsException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.UserRepository;
import com.samadhan.request.UserOtpRequest;
import com.samadhan.request.UserOtpVerifyRequest;
import com.samadhan.request.UserRegisterRequest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class LoginService {

    @Value("${sms.service.provider.url}")
    private String smsProviderUrl;

    @Value("${sms.client.token}")
    private String smsProviderKey;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransferVendorRepository transferVendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDetails isOtpValid(UserOtpVerifyRequest userOtpVerifyRequest) throws OtpMismatchException {
        UserDetails userDetails = userRepository.findByUserContactNumber(userOtpVerifyRequest.getUserContactNumber())
                .orElseThrow(() -> new OtpMismatchException("user details not found"));

        if (userDetails.getOtp() != userOtpVerifyRequest.getOtp()) {
            throw new OtpMismatchException("otp is invalid");
        }
        return userDetails;
    }

    public void registerUser(UserRegisterRequest userRegisterRequest) throws ConflictException {

        boolean isExist = userRepository
                .existsByMobileOrEmail(userRegisterRequest.getUserContactNumber(), userRegisterRequest.getUserEmail()) == 1;
        if(isExist) {
            throw new ConflictException("User Already exists with same mobile number or email");
        }
        UserDetails userDetails = new UserDetails();
        userDetails.setUserEmail(userRegisterRequest.getUserEmail());
        userDetails.setUserContactNumber(userRegisterRequest.getUserContactNumber());
        userDetails.setUserPassword(userRegisterRequest.getUserPassword());
        Integer otp = generateAndSendOtp(userRegisterRequest.getUserContactNumber());
        userDetails.setOtp(otp);
        userRepository.save(userDetails);
    }

    public void sendOtp(UserOtpRequest userOtpRequest) throws ConflictException {
        Integer otp = generateAndSendOtp(userOtpRequest.getContactNumber());
        UserDetails userDetails = userRepository.findByUserContactNumber(userOtpRequest.getContactNumber())
                .orElseGet(() -> {
                    UserDetails newUser = new UserDetails();
                    newUser.setUserContactNumber(userOtpRequest.getContactNumber());
                    return newUser;
                });

        userDetails.setOtp(otp);
        userRepository.save(userDetails);
    }

//    public void updateUserLatLong(Long userId, String latitude, String longitude) throws NotFoundException {
//        if(!userRepository.existsById(userId)) {
//            throw new NotFoundException("user with this userId[%s] not exists".formatted(userId));
//        }
//
//        Optional<UserDetails> optionalUser = userRepository.findById(userId);
//        UserDetails updatedUserDetails = optionalUser.map(user -> {
//            user.setUserLatitude(latitude);
//            user.setUserLongitude(longitude);
//            return user;
//        }).get();
//
//        userRepository.save(updatedUserDetails);
//
//    }

    public Integer generateAndSendOtp(String mobileNumber) {
        Integer otp = generateOtp();
        sendOtpSms(mobileNumber, "Four digit OTP to login in Transfer Service is " + otp);
        return otp;
    }

    private void sendOtpSms(String mobileNumber, String message) {
        try{
            JSONObject obj = new JSONObject();
            obj.put("route", "q"); // this will cost 5 rupees per sms
//            obj.put("route", "otp");
            obj.put("message", message);
            obj.put("numbers", mobileNumber);
            obj.put("authorization", smsProviderKey);
            obj.put("flash", "0");

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("authorization", smsProviderKey);
            headers.set("Content-Type", "application/json"); // optional - in case you auth in headers
            HttpEntity<JSONObject> entity = new HttpEntity<>(obj, headers);


            ResponseEntity<JSONObject> respEntity = new RestTemplate()
                    .exchange(smsProviderUrl, HttpMethod.POST, entity, JSONObject.class);

            System.out.println("url is " + smsProviderUrl);
            System.out.println("mobile number and message is "+mobileNumber+" | "+message);
        } catch (Exception exp){
            throw exp;
        }
    }

    private Integer generateOtp() {
        Random random = new Random();

        return IntStream.generate(() -> 1000 + random.nextInt(9000))
                .findFirst()
                .getAsInt();

    }

	// Passwords used to be stored (and compared, via findByUserAndPassword's raw SQL "=") in
	// plaintext. This looks up by email only, then verifies in Java so a bcrypt hash can be
	// checked properly. Existing vendors still have their old plaintext password stored — the
	// legacy-plaintext branch below verifies against that once, then immediately re-hashes and
	// saves it, so every vendor is transparently migrated to a hashed password the next time
	// they log in, with no bulk migration and no forced password reset.
	public TransferVendor loginTransfervendor(String userName, String password) {

		TransferVendor transferv = transferVendorRepository.findByVendorEmail(userName);

		if (transferv == null || transferv.getVendorPassword() == null) {
			throw new RuntimeException("Invalid credentials");
		}

		String storedPassword = transferv.getVendorPassword();

		if (isBcryptHash(storedPassword)) {
			if (!passwordEncoder.matches(password, storedPassword)) {
				throw new RuntimeException("Invalid credentials");
			}
		} else {
			if (!storedPassword.equals(password)) {
				throw new RuntimeException("Invalid credentials");
			}
			transferv.setVendorPassword(passwordEncoder.encode(password));
			transferVendorRepository.save(transferv);
		}

		return transferv;
	}

	private boolean isBcryptHash(String value) {
		return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
	}

	private static final int RESET_OTP_VALID_MINUTES = 10;
	private static final int RESET_OTP_MAX_ATTEMPTS = 5;

	// Always succeeds silently for an unknown/incomplete vendor record — the controller returns
	// the same generic message either way, so a caller can't use this endpoint to discover which
	// emails are registered vendors.
	public void requestVendorPasswordReset(String vendorEmail) {
		if (vendorEmail == null || vendorEmail.isBlank()) {
			return;
		}

		TransferVendor vendor = transferVendorRepository.findByVendorEmail(vendorEmail);
		if (vendor == null || vendor.getVendorContactNumber() == null) {
			return;
		}

		Integer otp = generateOtp();
		vendor.setResetOtpHash(passwordEncoder.encode(String.valueOf(otp)));
		vendor.setResetOtpExpiry(LocalDateTime.now().plusMinutes(RESET_OTP_VALID_MINUTES));
		vendor.setResetOtpAttempts(0);
		transferVendorRepository.save(vendor);

		sendOtpSms(vendor.getVendorContactNumber(),
				"Your OTP to reset your TransferEaze vendor password is " + otp + ". It is valid for "
						+ RESET_OTP_VALID_MINUTES + " minutes.");
	}

	public void resetVendorPassword(String vendorEmail, Integer otp, String newPassword) throws OtpMismatchException {
		if (newPassword == null || newPassword.length() < 6) {
			throw new IllegalArgumentException("New password must be at least 6 characters long");
		}

		TransferVendor vendor = transferVendorRepository.findByVendorEmail(vendorEmail);
		if (vendor == null || vendor.getResetOtpHash() == null || vendor.getResetOtpExpiry() == null) {
			throw new OtpMismatchException("Invalid or expired OTP");
		}

		if (vendor.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
			vendor.setResetOtpHash(null);
			vendor.setResetOtpExpiry(null);
			vendor.setResetOtpAttempts(0);
			transferVendorRepository.save(vendor);
			throw new OtpMismatchException("OTP has expired. Please request a new one");
		}

		int attempts = vendor.getResetOtpAttempts() == null ? 0 : vendor.getResetOtpAttempts();
		if (attempts >= RESET_OTP_MAX_ATTEMPTS) {
			throw new OtpMismatchException("Too many incorrect attempts. Please request a new OTP");
		}

		if (otp == null || !passwordEncoder.matches(String.valueOf(otp), vendor.getResetOtpHash())) {
			vendor.setResetOtpAttempts(attempts + 1);
			transferVendorRepository.save(vendor);
			throw new OtpMismatchException("Invalid OTP");
		}

		vendor.setVendorPassword(passwordEncoder.encode(newPassword));
		vendor.setResetOtpHash(null);
		vendor.setResetOtpExpiry(null);
		vendor.setResetOtpAttempts(0);
		transferVendorRepository.save(vendor);
	}

	public void changeVendorPassword(Long vendorId, String currentPassword, String newPassword) {
		if (newPassword == null || newPassword.length() < 6) {
			throw new IllegalArgumentException("New password must be at least 6 characters long");
		}

		TransferVendor vendor = transferVendorRepository.findById(vendorId)
				.orElseThrow(() -> new InvalidCredentialsException("Vendor not found"));

		String storedPassword = vendor.getVendorPassword();
		boolean matches = isBcryptHash(storedPassword)
				? passwordEncoder.matches(currentPassword, storedPassword)
				: storedPassword != null && storedPassword.equals(currentPassword);

		if (!matches) {
			throw new InvalidCredentialsException("Current password is incorrect");
		}

		vendor.setVendorPassword(passwordEncoder.encode(newPassword));
		transferVendorRepository.save(vendor);
	}

}
