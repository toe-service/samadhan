package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDateTime;

// One row per (userContactNumber, serviceType) in-progress form — upserted every time the user
// app reports a backgrounded, unsubmitted form (see AbandonedFormController#saveDraft), and
// marked resolved once the user actually submits that request (#resolveDraft) or it gets stale.
// Lets the admin panel surface "someone started a request and left" without needing the user to
// have gotten far enough to create a real TransferRequestDetails row.
@Entity
@Table(name = "abandoned_form_session")
public class AbandonedFormSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_contact_number")
    private String userContactNumber;

    // TRANSFERSERVICE / BOOKVEHICLE / HOMESHIFTING — kept as a plain string (not the enum) since
    // this can be saved before the user has picked anything the backend would otherwise validate.
    @Column(name = "service_type")
    private String serviceType;

    // Whatever fields the user had filled in, as a JSON string — shape varies by serviceType and
    // isn't validated here; this is for an admin to read, not for the backend to act on.
    @Lob
    @Column(name = "form_data_snapshot")
    private String formDataSnapshot;

    @Column(name = "resolved")
    private boolean resolved;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserContactNumber() {
        return userContactNumber;
    }

    public void setUserContactNumber(String userContactNumber) {
        this.userContactNumber = userContactNumber;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getFormDataSnapshot() {
        return formDataSnapshot;
    }

    public void setFormDataSnapshot(String formDataSnapshot) {
        this.formDataSnapshot = formDataSnapshot;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
