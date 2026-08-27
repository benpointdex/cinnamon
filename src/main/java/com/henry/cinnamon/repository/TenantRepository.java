package com.henry.cinnamon.repository;

import com.henry.cinnamon.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    Optional<Tenant> findByApiKeyHash(String apiKeyHash);

    Optional<Tenant> findByEmail(String email);
}
