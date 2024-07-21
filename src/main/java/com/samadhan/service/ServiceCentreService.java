package com.samadhan.service;

import java.util.List;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;

public interface ServiceCentreService {

	ServiceCentre getAllServiceCentres();

	ServiceCentre createServiceCentre(ServiceCentre serviceCentre);

	List<ServiceCentre> getAllServiceCentreByfilters(String city, String pickuplatitude, String pickuplongitude);

}
