package com.samadhan.util;

import com.samadhan.entity.Ride;
import com.samadhan.response.*;
import com.samadhan.response.Error;


public class ResponseUtil {

	
	
//	public static ResponseObject<Object> populateResponseObject(final Object baseResponseModel,
//			final String responseStatus, final Error error) {
//		ResponseObject<Object> response = new ResponseObject<>();
//		response.setResponse(baseResponseModel);
//		Status status = new Status();
//		status.setResponseStatus(responseStatus);
//		status.setError(error);
//		response.setStatus(status);
//		return response;
//
//	}

	public static Object populateResponseObject(Object baseresponse,final String responseStatus, final Error error) {
		ResponseObject<Object> response = new ResponseObject<>();
		response.setResponse(baseresponse);
		Status status = new Status();
		if(responseStatus.equalsIgnoreCase("success")) {
			status.setResponseStatus("200");
		}
		else {
			status.setResponseStatus(responseStatus);
		}
		status.setError(error);
		response.setStatus(status);
		return response;
	}
	
}
