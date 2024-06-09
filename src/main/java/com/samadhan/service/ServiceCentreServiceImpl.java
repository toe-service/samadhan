package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.repository.ServiceCentreRepo;
import org.springframework.stereotype.Service;

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
	public ServiceCentre getAllServiceCentreByfilters(String city, Long pickuplatitude, Long pickuplongitude, Long destinationlatitude, Long destinationlongitude, serviceTypeEnum serviceType) {
		ServiceCentre service=(ServiceCentre) serviceCentreRepo.findByfilters(city);
		return null;
	}

}
