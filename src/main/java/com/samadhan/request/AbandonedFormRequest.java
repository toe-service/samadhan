package com.samadhan.request;

public class AbandonedFormRequest {
    private String userContactNumber;
    private String serviceType;
    // Only required for the save call — the resolve call just needs contact + serviceType to
    // find the existing row.
    private String formDataSnapshot;

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
}
