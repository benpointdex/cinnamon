package com.henry.cinnamon.controller;

import com.henry.cinnamon.model.CreateTenantRequest;
import com.henry.cinnamon.model.TenantCreatedResponse;
import com.henry.cinnamon.model.VerifyTenantRequest;
import com.henry.cinnamon.services.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService){
        this.tenantService= tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantCreatedResponse> createTenant(@RequestBody CreateTenantRequest req) {
        TenantCreatedResponse response = tenantService.createTenant(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String code,
            @RequestBody(required = false) VerifyTenantRequest bodyReq) {

        String resolvedTenantId = (bodyReq != null && bodyReq.tenantId() != null) ? bodyReq.tenantId() : tenantId;
        String resolvedCode = (bodyReq != null && bodyReq.code() != null) ? bodyReq.code() : code;
        String resolvedEmail = (bodyReq != null) ? bodyReq.email() : null;

        tenantService.verifyTenant(resolvedTenantId, resolvedEmail, resolvedCode);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Account verified successfully! Daily request limit upgraded to 1,000."
        ));
    }

    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resend(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String email,
            @RequestBody(required = false) VerifyTenantRequest bodyReq) {

        String resolvedTenantId = (bodyReq != null && bodyReq.tenantId() != null) ? bodyReq.tenantId() : tenantId;
        String resolvedEmail = (bodyReq != null && bodyReq.email() != null) ? bodyReq.email() : email;

        tenantService.resendVerificationCode(resolvedTenantId, resolvedEmail);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Verification code resent successfully."
        ));
    }
}
