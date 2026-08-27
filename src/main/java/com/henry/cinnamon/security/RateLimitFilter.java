package com.henry.cinnamon.security;

import com.henry.cinnamon.model.Tenant;
import com.henry.cinnamon.repository.TenantRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE +1)
public class RateLimitFilter extends OncePerRequestFilter {


    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();


@Autowired
    private  TenantRepository tenantRepository;





    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {


        String path = req.getRequestURI();
        if (path.startsWith("/api/tenants") || path.startsWith("/actuator")) {
            chain.doFilter(req, res);
            return;
        }
        try{
            String tenantId= TenantContext.get();
            Bucket bucket = buckets.computeIfAbsent(tenantId,  this::createBucketForTenant);

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if(probe.isConsumed()){
                chain.doFilter(req,res);
            }else {

                long waitForRefillSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

                long retryAfter = Math.max(1, waitForRefillSeconds);
                res.setStatus(429);
                res.setHeader("Retry-After", String.valueOf(retryAfter));
                res.setContentType("application/json");
                res.getWriter().write(String.format(
                        "{\"error\": \"Daily rate limit exceeded. Try again in %d seconds. Verify your account for higher limits.\"}",
                        retryAfter
                ));
            }
        } catch (IllegalStateException e){
            chain.doFilter(req,res);
        }
    }


    private Bucket createBucketForTenant(String tenantId) {
        int dailyLimit = tenantRepository.findById(tenantId)
                .map(Tenant::getDailyRequestLimit)
                .orElse(50);
        Bandwidth limit = Bandwidth.builder()
                .capacity(dailyLimit)
                .refillIntervally(dailyLimit, Duration.ofDays(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
