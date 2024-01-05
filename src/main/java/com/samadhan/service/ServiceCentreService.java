package com.samadhan.service;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;

public interface ServiceCentreService {

	ServiceCentre getAllServiceCentres();

	ServiceCentre createServiceCentre(ServiceCentre serviceCentre);

	ServiceCentre getAllServiceCentreByfilters(String city, Long pickuplatitude, Long pickuplongitude, Long destinationlatitude, Long destinationlongitude, serviceTypeEnum serviceType);

}
