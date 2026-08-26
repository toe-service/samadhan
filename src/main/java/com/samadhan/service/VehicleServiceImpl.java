package com.samadhan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.repository.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService{

	@Autowired
	VehicleRepository vehicleRepo;

	@Autowired
	org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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
		vehicleByVendor=vehicleRepo.findByVendorId(vendorId);
		}
		return vehicleByVendor;
	}

	@Override
	public Vehicle createVehicle(String vehicleNumber, String vehicleContactNumber, String currentLocation,
			VendorPickupVehicleEnum vendorVehicle, VehicleCategoryEnum vehicleCategory, String vehicleLatitude,
			String vehicleLongitude, String fcmToken, Long transferVendorId, MultipartFile rcFile) {

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
				storageService.uploadFile(rcKey, rcFile.getInputStream(), rcFile.getSize(), rcFile.getContentType());
				vehicle.setRcStorageKey(rcKey);
			} catch (Exception e) {
				throw new RuntimeException("Failed to upload RC document", e);
			}

			vehicle = vehicleRepo.save(vehicle);
		}

		return vehicle;
	}

	@Override
	public Vehicle updateLocation(String address, Long vehicleId) {
		
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
		            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		vehicle.setCurrentLocation(address);
		vehicleRepo.save(vehicle);
		return vehicle;
	}

	@Override
	public Vehicle loginVehicle(String userName, String password) {
		Vehicle vehicle = vehicleRepo.findByUserName(userName);

		if (vehicle == null || vehicle.getPassword() == null) {
			return null;
		}

		String storedPassword = vehicle.getPassword();

		if (com.samadhan.util.PasswordUtil.isBcryptHash(storedPassword)) {
			return passwordEncoder.matches(password, storedPassword) ? vehicle : null;
		}

		if (!storedPassword.equals(password)) {
			return null;
		}

		// Legacy plaintext password — transparently migrate to a bcrypt hash on successful login,
		// same as TransferVendor login (see LoginService#loginTransfervendor).
		vehicle.setPassword(passwordEncoder.encode(password));
		vehicleRepo.save(vehicle);
		return vehicle;
	}

	@Override
	public Vehicle registerVehicle(Vehicle vehicle) {
		
		return null;
	}

}
