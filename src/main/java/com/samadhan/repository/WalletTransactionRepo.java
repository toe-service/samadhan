package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepo  extends JpaRepository<WalletTransaction, Long> {

    @Query(value = "Select * from wallet_transaction where transfer_request_id = :requestId AND vendor_id =:vendorId AND transaction_type=:transactionType", nativeQuery = true)
	WalletTransaction findByVendorANDRequest(Long requestId, Long vendorId, String transactionType);
    
//    @Query(value = "Select * from wallet_transaction where transfer_request_id = :requestId AND vendor_id =:vendorId AND transaction_type='LEAD FEE'", nativeQuery = true)
//   	WalletTransaction findByVendorANDRequestLEAD(Long requestId, Long vendorId);

}
