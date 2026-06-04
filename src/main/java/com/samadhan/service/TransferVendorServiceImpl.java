package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.repository.VendorWalletRepository;

@Service
public class TransferVendorServiceImpl implements TransferVendorService{

	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	@Autowired
	VendorWalletRepository VendorWalletRepo;
	
	@Override
	public TransferVendor registerVendor(TransferVendor transferVendor) {
		
		if(transferVendor.getVendorEmail() != null) {
			
			String password = transferVendor.getVendorEmail().replace("@gmail.com", "");
			transferVendor.setVendorPassword(password);
		}
		
		TransferVendor transferVendorRegister=transferVendorRepo.save(transferVendor);
		return transferVendorRegister;
	}

	@Override
	public VendorWallet walletByVendor(String vendorId) {
		VendorWallet walletByVendor=VendorWalletRepo.findByVendor(vendorId);

		return walletByVendor;
	}

}
