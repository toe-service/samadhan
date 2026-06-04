package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.VendorWallet;

public interface VendorWalletRepository   extends JpaRepository<VendorWallet, Long>{

	@Query(value="select * from vendor_wallet where vendor_id=:vendorId" ,nativeQuery = true)
	VendorWallet findByVendor(Long vendorId);

}
