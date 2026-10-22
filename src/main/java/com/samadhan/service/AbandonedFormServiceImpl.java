package com.samadhan.service;

import com.samadhan.entity.AbandonedFormSession;
import com.samadhan.repository.AbandonedFormSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AbandonedFormServiceImpl implements AbandonedFormService {

    @Autowired
    private AbandonedFormSessionRepository abandonedFormSessionRepository;

    @Override
    public void saveDraft(String userContactNumber, String serviceType, String formDataSnapshot) {
        AbandonedFormSession session = abandonedFormSessionRepository
                .findByUserContactNumberAndServiceTypeAndResolvedFalse(userContactNumber, serviceType)
                .orElseGet(() -> {
                    AbandonedFormSession newSession = new AbandonedFormSession();
                    newSession.setUserContactNumber(userContactNumber);
                    newSession.setServiceType(serviceType);
                    newSession.setCreatedAt(LocalDateTime.now());
                    return newSession;
                });
        session.setFormDataSnapshot(formDataSnapshot);
        session.setLastUpdatedAt(LocalDateTime.now());
        abandonedFormSessionRepository.save(session);
    }

    @Override
    public void resolveDraft(String userContactNumber, String serviceType) {
        abandonedFormSessionRepository
                .findByUserContactNumberAndServiceTypeAndResolvedFalse(userContactNumber, serviceType)
                .ifPresent(session -> {
                    session.setResolved(true);
                    session.setLastUpdatedAt(LocalDateTime.now());
                    abandonedFormSessionRepository.save(session);
                });
    }
}
