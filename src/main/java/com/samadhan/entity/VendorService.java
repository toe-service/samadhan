package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.samadhan.enums.serviceTypeEnum;

@Entity
@Table(
    name = "vendor_service",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_id", "service_type"})
)
public class VendorService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonBackReference
    private TransferVendor transferVendor;

 //   @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private serviceTypeEnum serviceType;

    @Column(name = "is_active")
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransferVendor getTransferVendor() {
        return transferVendor;
    }

    public void setTransferVendor(TransferVendor transferVendor) {
        this.transferVendor = transferVendor;
    }

    public serviceTypeEnum getServiceType() {
        return serviceType;
    }

    public void setServiceType(serviceTypeEnum serviceType) {
        this.serviceType = serviceType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}