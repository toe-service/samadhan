package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepo  extends JpaRepository<WalletTransaction, Long> {

}
