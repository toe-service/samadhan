package com.samadhan.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.entity.Driver;
import com.samadhan.entity.VehicleTransfer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.repository.ServiceCentreRepo;
import org.springframework.stereotype.Service;

@Service
public class ServiceCentreServiceImpl implements ServiceCentreService{

	@Autowired
	ServiceCentreRepo serviceCentreRepo;
	
	
	@Override
	public List<ServiceCentre> getAllServiceCentres() {
	List<ServiceCentre> service= serviceCentreRepo.findAll();
		return service;
	}


	@Override
	public ServiceCentre createServiceCentre(ServiceCentre serviceCentre) {
		ServiceCentre service=(ServiceCentre) serviceCentreRepo.save(serviceCentre);
		return service;
	}


	@Override
	public List<ServiceCentre> getAllServiceCentreByfilters(Integer serviceType, String pickuplatitude, String pickuplongitude,String destinationlatitude, String destinationlongitude) {
		double distance=50.0;
		//find service centre within 50km
		System.out.println("In getAllServiceCentreByfilters");
		List<ServiceCentre> serviceCentresWithinFiftyKm=serviceCentreRepo.findAllServiceCentreByfilters(pickuplatitude,pickuplongitude,distance,serviceType);

		for(ServiceCentre servcentre : serviceCentresWithinFiftyKm){
			servcentre.setDestinationLatitude(destinationlatitude);
			servcentre.setDestinationLongitude(destinationlongitude);
		}

		return serviceCentresWithinFiftyKm;
	}

	@Override
	public List<ServiceCentreWrapper> getAllServiceCentreDriverByfilters(Integer serviceType, String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude) throws JsonProcessingException {
		double distance=50.0;
		//find drivers within 50km
		System.out.println("In getAllServiceCentreDriverByfilters");
		List<Object> serviceCentresDriverWithinFiftyKm=serviceCentreRepo.findAllServiceCentreDriversByfilters(pickuplatitude,pickuplongitude,distance);
		List<ServiceCentreWrapper> servicecentrewrapperList=new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(serviceCentresDriverWithinFiftyKm);

		JSONArray jsonArray = new JSONArray(jsonString);
		System.out.println(jsonArray.toString(2)); // pretty print

		for(Object o: jsonArray) {
			ServiceCentreWrapper serviceCentrewrapper=new ServiceCentreWrapper();
			JSONArray jsonArrayy = new JSONArray(o.toString());
			//String latitude = jsonArrayy.getString(0);
			serviceCentrewrapper.setSourceLatitude(pickuplatitude);
			//String longitude = jsonArrayy.getString(1);
			serviceCentrewrapper.setSourceLongitude(pickuplongitude);
			String phone = jsonArrayy.getString(2);
			serviceCentrewrapper.setDriverContactNumber(phone);
			String name = jsonArrayy.getString(3);
			serviceCentrewrapper.setDriverName(name);
			long id = jsonArrayy.getInt(4);
			serviceCentrewrapper.setId(id);
			String devicetoken = jsonArrayy.getString(5);
			serviceCentrewrapper.setDriverToken(devicetoken);
			serviceCentrewrapper.setDestinationLatitude(destinationlatitude);
			serviceCentrewrapper.setDestinationLongitude(destinationlongitude);

			// Print values
//			System.out.println("Latitude: " + latitude);
//			System.out.println("Longitude: " + longitude);
			System.out.println("Phone: " + phone);
			System.out.println("Name: " + name);
			System.out.println("Id: " + id);
			System.out.println("devicetoken: " + devicetoken);

			servicecentrewrapperList.add(serviceCentrewrapper);

		}


		return servicecentrewrapperList;
	}

	@Override
	public VehicleTransfer requestRideTransfer(String vehicleType, String vehicleModel, Date requestDate, String city, String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude) {

	//VehicleTransfer requestRideTransfer=vehicleRepo.save();

	return null;
	}

}
