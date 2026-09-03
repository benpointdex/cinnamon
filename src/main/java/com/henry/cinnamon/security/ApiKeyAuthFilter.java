package com.henry.cinnamon.security;

import com.henry.cinnamon.model.Tenant;
import com.henry.cinnamon.services.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TenantService tenantService;

    public ApiKeyAuthFilter(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 1. Bypass authentication for public signup/verification, actuator endpoints, and OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || requestURI.startsWith("/api/tenants")
                || requestURI.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
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
            TenantContext.set(tenantOpt.get().getTenantId());
            filterChain.doFilter(request, response);
        } finally {
            // Always clear ThreadLocal to prevent memory leaks across pooled threads
            TenantContext.clear();
        }
    }
}
