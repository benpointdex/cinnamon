package com.henry.cinnamon.controller;

import com.henry.cinnamon.model.CreateTenantRequest;
import com.henry.cinnamon.model.TenantCreatedResponse;
import com.henry.cinnamon.services.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
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
    public ResponseEntity<String> verify(@RequestParam String tenantId, @RequestParam String code) {
        tenantService.verifyTenant(tenantId, code);
        return ResponseEntity.ok("Account verified successfully! Daily request limit upgraded to 1,000.");
    }
}
