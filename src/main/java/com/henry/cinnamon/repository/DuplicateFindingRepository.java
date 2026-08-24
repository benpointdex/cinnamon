package com.henry.cinnamon.repository;

import com.henry.cinnamon.model.DuplicateFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DuplicateFindingRepository extends JpaRepository<DuplicateFinding, UUID> {

    List<DuplicateFinding> findByTenantIdAndRepositoryOrderByFlaggedAtDesc(String tenantId, String repository);

    int countByTenantIdAndRepository(String tenantId, String repository);
}
