package com.samadhan.util;

import com.samadhan.entity.Ride;
import com.samadhan.response.*;
import com.samadhan.response.Error;
import org.springframework.http.ResponseEntity;


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

	public static <T> ResponseObject<T> populateResponseObject(T baseresponse,
															   final String responseStatus,
															   final Error error) {
		ResponseObject<T> response = new ResponseObject<>();
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
