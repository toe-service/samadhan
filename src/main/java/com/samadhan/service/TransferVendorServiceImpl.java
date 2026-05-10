package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;

@Service
public class TransferVendorServiceImpl implements TransferVendorService{

	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	@Override
	public TransferVendor registerVendor(TransferVendor transferVendor) {
		
		if(transferVendor.getVendorEmail() != null) {
			
			String password = transferVendor.getVendorEmail().replace("@gmail.com", "");
			transferVendor.setVendorPassword(password);
		}
		
		TransferVendor transferVendorRegister=transferVendorRepo.save(transferVendor);
		return transferVendorRegister;
	}

}
