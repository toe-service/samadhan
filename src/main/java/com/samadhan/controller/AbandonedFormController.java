package com.samadhan.controller;

import com.samadhan.request.AbandonedFormRequest;
import com.samadhan.response.ResponseObject;
import com.samadhan.service.AbandonedFormService;
import com.samadhan.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Lets the user app report an in-progress, unsubmitted form when the user backgrounds/closes
// it (see the frontend's App State listener), and clear that report once the user actually
// submits the real request — so the admin panel's "Drafts in Progress" list (AdminController
// #getAbandonedFormSessions) only ever shows genuinely abandoned sessions.
@RestController
@RequestMapping("/v1/abandoned-form")
public class AbandonedFormController {

    @Autowired
    private AbandonedFormService abandonedFormService;

    @PostMapping
    public ResponseEntity<ResponseObject<?>> saveDraft(@RequestBody AbandonedFormRequest request) {
        abandonedFormService.saveDraft(
                request.getUserContactNumber(), request.getServiceType(), request.getFormDataSnapshot());
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(request, "SUCCESS", null));
    }

    @PostMapping("/resolve")
    public ResponseEntity<ResponseObject<?>> resolveDraft(@RequestBody AbandonedFormRequest request) {
        abandonedFormService.resolveDraft(request.getUserContactNumber(), request.getServiceType());
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(request, "SUCCESS", null));
    }
}
