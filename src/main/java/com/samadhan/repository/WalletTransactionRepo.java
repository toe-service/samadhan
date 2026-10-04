package com.samadhan.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepo  extends JpaRepository<WalletTransaction, Long> {

    @Query(value = "Select * from wallet_transaction where transfer_request_id = :requestId AND vendor_id =:vendorId AND transaction_type=:transactionType", nativeQuery = true)
	WalletTransaction findByVendorANDRequest(Long requestId, Long vendorId, String transactionType);

    List<WalletTransaction> findByVendor_IdOrderByIdDesc(Long vendorId);

    // Idempotency check for Razorpay-driven credits — see WalletTransaction#razorpayPaymentId.
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);

    // The original credit/purchase row for a given payment id, looked up when a refund.processed
    // webhook arrives for it — OrderByIdAsc guarantees the first (original) row even though a
    // refund reversal row may later be saved carrying the same razorpayPaymentId for traceability.
    Optional<WalletTransaction> findFirstByRazorpayPaymentIdOrderByIdAsc(String razorpayPaymentId);

    // Idempotency check for refunds — see WalletTransaction#razorpayRefundId. Razorpay redelivers
    // webhooks, so the same refund.processed event can arrive more than once.
    boolean existsByRazorpayRefundId(String razorpayRefundId);

    // Idempotency check for payment.failed-after-capture reversals (see
    // PaymentController#handlePaymentFailed), which have no refund id of their own to key off of
    // the way refund.processed does: 1 row for this payment id means only the original
    // credit/purchase exists (safe to reverse); >1 means a reversal already happened — either this
    // same event redelivered, or a refund.processed for the same payment got there first.
    long countByRazorpayPaymentId(String razorpayPaymentId);

    @Modifying
    @Transactional
	@Query(value = "delete from wallet_transaction where transfer_request_id = :transferId", nativeQuery = true)
	void deleteBydeleteByTransferRequest(Long transferId);
    
//    @Query(value = "Select * from wallet_transaction where transfer_request_id = :requestId AND vendor_id =:vendorId AND transaction_type='LEAD FEE'", nativeQuery = true)
//   	WalletTransaction findByVendorANDRequestLEAD(Long requestId, Long vendorId);

}
