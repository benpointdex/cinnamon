package com.henry.cinnamon.services;

import com.henry.cinnamon.model.CreateTenantRequest;
import com.henry.cinnamon.model.Tenant;
import com.henry.cinnamon.model.TenantCreatedResponse;
import com.henry.cinnamon.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TenantService tenantService;

    private Tenant sampleTenant;

    @BeforeEach
    void setUp() {
        sampleTenant = new Tenant();
        sampleTenant.setTenantId("tenant-123");
        sampleTenant.setName("Test Dev");
        sampleTenant.setEmail("dev@example.com");
        sampleTenant.setVerificationCode("654321");
        sampleTenant.setVerificationCodeExpiresAt(Instant.now().plus(Duration.ofHours(24)));
        sampleTenant.setDailyRequestLimit(50);
        sampleTenant.setEmailVerified(false);
    }

    @Test
    void createTenant_generatesKeyAndSendsVerificationOtp() {
        CreateTenantRequest req = new CreateTenantRequest("Test Dev", "dev@example.com");
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantCreatedResponse resp = tenantService.createTenant(req);

        assertNotNull(resp);
        assertNotNull(resp.tenantId());
        assertNotNull(resp.rawApiKey());
        assertTrue(resp.rawApiKey().startsWith("dc"));

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant saved = captor.getValue();
        assertEquals(50, saved.getDailyRequestLimit());
        assertFalse(saved.isEmailVerified());
        assertNotNull(saved.getVerificationCode());
        assertEquals(6, saved.getVerificationCode().length());

        verify(emailService).sendVerificationCode(eq("dev@example.com"), eq(saved.getVerificationCode()));
    }

    @Test
    void verifyTenant_withValidOtp_upgradesTo1000Requests() {
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(sampleTenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tenantService.verifyTenant("tenant-123", "654321");

        assertTrue(sampleTenant.isEmailVerified());
        assertEquals(1000, sampleTenant.getDailyRequestLimit());
        verify(tenantRepository).save(sampleTenant);
    }

    @Test
    void verifyTenant_withInvalidOtp_throwsBadRequest() {
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(sampleTenant));

        assertThrows(ResponseStatusException.class, () -> {
            tenantService.verifyTenant("tenant-123", "000000");
        });
        assertFalse(sampleTenant.isEmailVerified());
        assertEquals(50, sampleTenant.getDailyRequestLimit());
    }

    @Test
    void verifyTenant_byEmail_worksWhenTenantIdMissing() {
        when(tenantRepository.findByEmail("dev@example.com")).thenReturn(Optional.of(sampleTenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tenantService.verifyTenant(null, "dev@example.com", "654321");

        assertTrue(sampleTenant.isEmailVerified());
        assertEquals(1000, sampleTenant.getDailyRequestLimit());
    }

    @Test
    void resendVerificationCode_generatesNewCodeAndEmails() {
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(sampleTenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tenantService.resendVerificationCode("tenant-123", null);

        verify(tenantRepository).save(sampleTenant);
        verify(emailService).sendVerificationCode(eq("dev@example.com"), anyString());
    }
}
