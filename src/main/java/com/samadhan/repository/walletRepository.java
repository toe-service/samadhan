package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.VendorWallet;

@Repository
public interface walletRepository  extends JpaRepository<VendorWallet, Long> {
	
	@Query(value="select * from vendor_wallet where vendor_id=:vendorId" ,nativeQuery = true)
	VendorWallet findByVendorId(Long vendorId);

}
