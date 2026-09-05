package com.henry.cinnamon.security;

import com.henry.cinnamon.model.Tenant;
import com.henry.cinnamon.services.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantService tenantService;

    @Value("${cinnamon.security.actuator-secret:${ACTUATOR_SECRET:}}")
    private String actuatorSecret;

    public ApiKeyAuthFilter(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. Bypass authentication for public signup/verification, health probes, and OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || requestURI.startsWith("/api/tenants")
                || requestURI.equals("/actuator/health")
                || requestURI.startsWith("/actuator/health/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Protect non-health Actuator endpoints (e.g., /actuator/prometheus, /actuator/metrics)
        if (requestURI.startsWith("/actuator")) {
            if (isActuatorAuthorized(request)) {
                filterChain.doFilter(request, response);
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized: Missing or invalid Actuator secret token\"}");
                return;
            }
        }

        // 2. Extract X-Api-Key or Authorization: Bearer header
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey == null || apiKey.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                apiKey = authHeader.substring(7).trim();
            }
        }

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing required X-Api-Key or Authorization header\"}");
            return;
        }

        // 3. Fast database lookup by SHA-256 hash via TenantService
        Optional<Tenant> tenantOpt = tenantService.resolveByApiKey(apiKey);
        if (tenantOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or unknown API key\"}");
            return;
        }

        try {
            // Set ThreadLocal context for the duration of this request
             MDC.put("tenantId", tenantOpt.get().getTenantId());
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            TenantContext.set(tenantOpt.get().getTenantId());
            filterChain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal to prevent memory leaks across pooled threads
            TenantContext.clear();
              MDC.clear(); 
        }
    }

    private boolean isActuatorAuthorized(HttpServletRequest request) {
        // If no secret configured (e.g. local dev without env var), allow access
        if (actuatorSecret == null || actuatorSecret.isBlank()) {
            return true;
        }

        // 1. Check Authorization: Bearer <ACTUATOR_SECRET>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String token = authHeader.substring(7).trim();
            if (actuatorSecret.equals(token)) {
                return true;
            }
        }

        // 2. Check X-Actuator-Token: <ACTUATOR_SECRET>
        String customToken = request.getHeader("X-Actuator-Token");
        if (actuatorSecret.equals(customToken)) {
            return true;
        }

        // 3. Fallback: Allow authenticated Tenants to view metrics if API key provided
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return tenantService.resolveByApiKey(apiKey).isPresent();
        }

        return false;
    }
}
