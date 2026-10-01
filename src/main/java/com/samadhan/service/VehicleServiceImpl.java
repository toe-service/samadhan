package com.samadhan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.exception.AccountDisabledException;
import com.samadhan.exception.VehicleLimitExceededException;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService{

	// Fleet-size caps: an individual (owner-operator) account is meant to be a single vehicle, not
	// a fleet — anything past that requires becoming a full (non-individual) vendor. A non-
	// individual vendor without an active subscription is capped at a small fleet; an active
	// subscriber has no cap. See TransferVendor#isSubscriber for what "active subscription" means.
	private static final int INDIVIDUAL_VEHICLE_LIMIT = 1;
	private static final int NON_SUBSCRIBER_VEHICLE_LIMIT = 3;

	@Autowired
	VehicleRepository vehicleRepo;

	@Autowired
	TransferVendorRepository transferVendorRepository;

	@Autowired
	org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Autowired
	LocationService locationService;

	private final StorageService storageService;

	public VehicleServiceImpl(StorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public List<Vehicle> getAllVehiclesByVendor(Long vendorId, boolean isActive) {
		List<Vehicle> vehicleByVendor=new ArrayList<>();
		if(isActive) {
		boolean isOngoing=false;
		vehicleByVendor=vehicleRepo.findByActiveVendorId(vendorId, isOngoing);
		}else {
		vehicleByVendor=vehicleRepo.findAllByVendorIdIncludingInactive(vendorId);
		}
		return vehicleByVendor;
	}

	@Override
	public Vehicle createVehicle(String vehicleNumber, String vehicleContactNumber, String currentLocation,
			VendorPickupVehicleEnum vendorVehicle, VehicleCategoryEnum vehicleCategory, String vehicleLatitude,
			String vehicleLongitude, String fcmToken, Long transferVendorId, MultipartFile rcFile) {

		if (transferVendorId != null) {
			enforceVehicleLimit(transferVendorId);
		}

		Vehicle vehicle = new Vehicle();

		if (vehicleNumber != null) {
			String userName = vehicleNumber.replaceAll("\\s+", "") + "@gmail.com";

			String password = vehicleNumber.replaceAll("\\s+", "")
					.substring(Math.max(0, vehicleNumber.replaceAll("\\s+", "").length() - 6));

			vehicle.setUserName(userName);
			vehicle.setPassword(password);
		}

		vehicle.setVehicleNumber(vehicleNumber);
		vehicle.setVehicleContactNumber(vehicleContactNumber);
		vehicle.setCurrentLocation(currentLocation);
		vehicle.setVendorVehicle(vendorVehicle);
		vehicle.setVehicleCategory(vehicleCategory);
		vehicle.setVehicleLatitude(vehicleLatitude);
		vehicle.setVehicleLongitude(vehicleLongitude);
		vehicle.setFcmToken(fcmToken);

		if (transferVendorId != null) {
			TransferVendor transferVendor = new TransferVendor();
			transferVendor.setId(transferVendorId);
			vehicle.setTransferVendor(transferVendor);
		}

		vehicle = vehicleRepo.save(vehicle);

		if (rcFile != null && !rcFile.isEmpty()) {
			String rcKey = String.format("transfer-vehicles/%d/rc/%d_%s", vehicle.getId(),
					System.currentTimeMillis(), rcFile.getOriginalFilename());

			try {
		//		storageService.uploadFile(rcKey, rcFile.getInputStream(), rcFile.getSize(), rcFile.getContentType());
				vehicle.setRcStorageKey(rcKey);
			} catch (Exception e) {
				throw new RuntimeException("Failed to upload RC document", e);
			}

			vehicle = vehicleRepo.save(vehicle);
		}

		return vehicle;
	}

	// Checked before the new vehicle is even built — a vendor that's about to hit their fleet
	// cap should never get a partially-created row. Counts only active vehicles (findByVendorId
	// already filters is_active), so a previously deactivated vehicle doesn't count against the
	// cap — matches the soft-delete convention used everywhere else in this class.
	private void enforceVehicleLimit(Long transferVendorId) {
		TransferVendor vendor = transferVendorRepository.findById(transferVendorId).orElse(null);
		if (vendor == null) {
			return;
		}

		int activeVehicleCount = vehicleRepo.findByVendorId(transferVendorId).size();

		if (Boolean.TRUE.equals(vendor.getIsIndividual())) {
			if (activeVehicleCount >= INDIVIDUAL_VEHICLE_LIMIT) {
				throw new VehicleLimitExceededException(
						"Individual accounts are limited to " + INDIVIDUAL_VEHICLE_LIMIT + " vehicle. "
						+ "Please buy a subscription to add more vehicles, and convert this account from Individual to Vendor.");
			}
			return;
		}

		if (!vendor.isSubscriber() && activeVehicleCount >= NON_SUBSCRIBER_VEHICLE_LIMIT) {
			throw new VehicleLimitExceededException(
					"Non-subscribers are limited to " + NON_SUBSCRIBER_VEHICLE_LIMIT + " vehicles. "
					+ "Please buy a subscription to add more vehicles.");
		}
	}

	@Override
	public Vehicle updateLocation(String address, Long vehicleId, String lat, String lng) {

		Vehicle vehicle = vehicleRepo.findById(vehicleId)
		            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		vehicle.setCurrentLocation(address);
		// Previously only the human-readable address was saved here — vehicle_latitude/
		// vehicle_longitude (what findNearbyVehicles actually matches on) went untouched by this
		// endpoint despite it receiving lat/lng on every call. Same gap as loginRole had, fixed
		// the same way.
		if (lat != null) {
			vehicle.setVehicleLatitude(lat);
		}
		if (lng != null) {
			vehicle.setVehicleLongitude(lng);
		}
		vehicleRepo.save(vehicle);
		return vehicle;
	}

	@Override
	public Vehicle loginVehicle(String userName, String password, Double lat, Double lng) {
		Vehicle vehicle = vehicleRepo.findByUserName(userName);

		if (vehicle == null || vehicle.getPassword() == null) {
			return null;
		}

		if (Boolean.FALSE.equals(vehicle.getIsActive())) {
			throw new AccountDisabledException("This vehicle account has been disabled");
		}

		if (vehicle.getTransferVendor() != null) {
			vehicle.setVendorName(vehicle.getTransferVendor().getVendorName());
		}

		String storedPassword = vehicle.getPassword();
		boolean passwordMatches;

		if (com.samadhan.util.PasswordUtil.isBcryptHash(storedPassword)) {
			passwordMatches = passwordEncoder.matches(password, storedPassword);
		} else if (storedPassword.equals(password)) {
			passwordMatches = true;
			// Legacy plaintext password — transparently migrate to a bcrypt hash on successful
			// login, same as TransferVendor login (see LoginService#loginTransfervendor).
			vehicle.setPassword(passwordEncoder.encode(password));
		} else {
			passwordMatches = false;
		}

		if (!passwordMatches) {
			return null;
		}

		// Refreshes the vehicle's live position on every login — previously vehicle_latitude/
		// vehicle_longitude were only ever written once, at registration (createVehicle above),
		// so findNearbyVehicles (the query behind both ride-notification matching and the
		// vendor's own vehicle feed) was matching against a position that could go stale for the
		// vehicle's entire lifetime. Also mirrors it into currentLocation (human-readable, via
		// the same reverse-geocode as VehicleController#updateRoleLocation) so the vendor
		// dashboard's display stays in sync too. Optional — null/null from callers (e.g. the
		// public vehicle-delete flow) that only need to verify credentials just skip this.
		if (lat != null && lng != null) {
			vehicle.setVehicleLatitude(String.valueOf(lat));
			vehicle.setVehicleLongitude(String.valueOf(lng));

			Map<String, Object> locationResult = locationService.getLocation(lat, lng);
			Object address = locationResult.get("address");
			if (address != null) {
				vehicle.setCurrentLocation((String) address);
			}
		}

		return vehicleRepo.save(vehicle);
	}

	@Override
	public Vehicle registerVehicle(Vehicle vehicle) {

		return null;
	}

	// Soft delete — marks the vehicle inactive instead of removing the row, so transfer/ride
	// history tied to this vehicle stays intact. Same pattern as UserServiceImpl#deactivateUser.
	@Override
	public Vehicle deactivateVehicle(Long vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
				.orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		vehicle.setIsActive(false);
		return vehicleRepo.save(vehicle);
	}

	// Vendor-facing soft delete for the "All Vehicles" fleet management page — unlike
	// deactivateVehicle above (called by the vehicle's own JWT-authenticated self-service
	// endpoints), this is invoked with the vendor's own credentials, so ownership is checked
	// explicitly here instead of via a token claim, same pattern as the vehicle-assignment
	// ownership check in TransferRequestServiceImpl#requestTransferApproval.
	@Override
	public Vehicle deactivateVehicleForVendor(Long vehicleId, Long vendorId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
				.orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		if (vehicle.getTransferVendor() == null || !vendorId.equals(vehicle.getTransferVendor().getId())) {
			throw new AccessDeniedException("Vehicle " + vehicleId + " does not belong to vendor " + vendorId);
		}
		vehicle.setIsActive(false);
		return vehicleRepo.save(vehicle);
	}

	// Reverses deactivateVehicle — re-enables a previously disabled vehicle.
	@Override
	public Vehicle activateVehicle(Long vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
				.orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		vehicle.setIsActive(true);
		return vehicleRepo.save(vehicle);
	}

	// Reverses deactivateVehicleForVendor — same ownership check as its deactivate counterpart.
	@Override
	public Vehicle activateVehicleForVendor(Long vehicleId, Long vendorId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
				.orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		if (vehicle.getTransferVendor() == null || !vendorId.equals(vehicle.getTransferVendor().getId())) {
			throw new AccessDeniedException("Vehicle " + vehicleId + " does not belong to vendor " + vendorId);
		}
		vehicle.setIsActive(true);
		return vehicleRepo.save(vehicle);
	}

}
