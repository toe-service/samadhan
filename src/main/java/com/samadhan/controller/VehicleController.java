package com.samadhan.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Vehicle;
import com.samadhan.exception.ConflictException;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.response.ResponseObject;
import com.samadhan.security.TokenApi;
import com.samadhan.service.LocationService;
import com.samadhan.service.VehicleService;
import com.samadhan.service.driversService;
import com.samadhan.util.ResponseUtil;

@RestController
@RequestMapping(value = "/vehicle")
public class VehicleController {
	
	  private final LocationService locationService;
	  
	  public VehicleController(LocationService service){
	        this.locationService = service;
	    }

	  @Autowired
	  VehicleService vehicleService;
	  
	  @Autowired
	  driversService driverService;

	  @Autowired
	  TokenApi tokenApi;

	@PatchMapping(value = "/update-roleLocation/{Id}")
	public ResponseEntity<ResponseObject<?>> updateRoleLocation(@PathVariable Long Id, @RequestParam double lat,
            @RequestParam double lng, @RequestParam String userType) {

		Map<String, Object>  resp = locationService.getLocation(lat,lng);

		String address = (String) resp.get("address");

		if(userType.equalsIgnoreCase("vehicle")) {
		Vehicle vehicle=vehicleService.updateLocation(address,Id);
		return ResponseEntity.ok(ResponseUtil.populateResponseObject(vehicle, "SUCCESS", null));
		}else {
		Driver driver=driverService.updateLocation(address,Id);
		return ResponseEntity.ok(ResponseUtil.populateResponseObject(driver, "SUCCESS", null));
		}

	}
	
	@PostMapping(value = "/register-Vehicle")
	public Vehicle registerVehicle(@RequestBody Vehicle vehicle) {

		Vehicle resp = vehicleService.registerVehicle(vehicle);
		return resp;
	}

	// Authenticated (default security rule — see SecurityConfig). Hard delete: permanently
	// removes the row (unlike DELETE /v1/vehicle/{vehicleId}, which only deactivates it). The
	// JWT's own userId claim must match the vehicleId being deleted, same ownership check as
	// V1UserLoginAndRegistrationController#deleteVehicle. Rejected with a ConflictException if
	// the vehicle has transfer/ride history — deactivate it instead in that case.
	@DeleteMapping(value = "/{vehicleId}")
	public ResponseEntity<ResponseObject<?>> deleteVehicle(
			@PathVariable Long vehicleId, HttpServletRequest httpRequest) throws ConflictException {

		String authHeader = httpRequest.getHeader("Authorization");
		String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
		Long tokenVehicleId = jwt != null ? tokenApi.extractUserId(jwt) : null;

		if (tokenVehicleId == null || !tokenVehicleId.equals(vehicleId)) {
			throw new AccessDeniedException("You are not authorized to delete this vehicle");
		}

		vehicleService.deleteVehicle(vehicleId);
		ResponseObject<String> success = ResponseUtil.populateResponseObject(
				"Vehicle deleted successfully.", "SUCCESS", null);
		return ResponseEntity.ok(success);
	}

}
