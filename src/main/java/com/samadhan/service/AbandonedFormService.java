package com.samadhan.service;

public interface AbandonedFormService {
    void saveDraft(String userContactNumber, String serviceType, String formDataSnapshot);

    void resolveDraft(String userContactNumber, String serviceType);
}
