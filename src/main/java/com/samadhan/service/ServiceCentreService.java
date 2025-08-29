package com.samadhan.service;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.enums.serviceTypeEnum;

public interface ServiceCentreService {

	List<ServiceCentre> getAllServiceCentres();

	ServiceCentre createServiceCentre(ServiceCentre serviceCentre);

	List<ServiceCentre> getAllServiceCentreByfilters(Integer serviceType, String pickuplatitude, String pickuplongitude,String destinationlatitude, String destinationlongitude);

    List<ServiceCentreWrapper> getAllServiceCentreDriverByfilters(Integer serviceType, String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude) throws JsonProcessingException;

    VehicleTransfer requestRideTransfer(String vehicleType, String vehicleModel, Date requestDate, String city, String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude);
}
