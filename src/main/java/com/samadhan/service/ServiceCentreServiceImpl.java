package com.samadhan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.repository.ServiceCentreRepo;

@Service
public class ServiceCentreServiceImpl implements ServiceCentreService{

	@Autowired
	ServiceCentreRepo serviceCentreRepo;
	
	
	@Override
	public ServiceCentre getAllServiceCentres() {
	ServiceCentre service=(ServiceCentre) serviceCentreRepo.findAll();
		return service;
	}


	@Override
	public ServiceCentre createServiceCentre(ServiceCentre serviceCentre) {
		ServiceCentre service=(ServiceCentre) serviceCentreRepo.save(serviceCentre);
		return service;
	}


	@Override
	public List<ServiceCentre> getAllServiceCentreByfilters(String city, String pickuplatitude, String pickuplongitude) {
		//ServiceCentre service=(ServiceCentre) serviceCentreRepo.findByfilters();
		
		double distance=50.0;
		//find service centre within 50km
		
		List<ServiceCentre> serviceCentresWithinFiftyKm=serviceCentreRepo.findAllServiceCentreByfilters(pickuplatitude,pickuplongitude,distance);
		
		return serviceCentresWithinFiftyKm;
	}

}
