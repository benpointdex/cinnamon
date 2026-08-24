package com.henry.cinnamon.repository;

import com.henry.cinnamon.model.CodeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeUnitRepository extends JpaRepository<CodeUnit, UUID> {

    // 1. Tier 1: Exact / Normalized content hash match
    Optional<CodeUnit> findByTenantIdAndRepositoryAndContentHash(
            String tenantId, String repository, String contentHash);

    boolean existsByTenantIdAndRepositoryAndContentHash(
            String tenantId, String repository, String contentHash);

    // 2. Lifecycle: Clean up old function records when a file is modified/re-indexed
    @Transactional
    @Modifying
    @Query("DELETE FROM CodeUnit c WHERE c.tenantId = :tenantId AND c.repository = :repository AND c.filePath = :filePath")
    void deleteStaleFileUnits(
            @Param("tenantId") String tenantId,
            @Param("repository") String repository,
            @Param("filePath") String filePath);

    // 3. Tier 2: Nearest Neighbors Cosine Distance Query (<=> is cosine distance in pgvector)
    @Query(value = """
        SELECT * FROM code_units
        WHERE tenant_id = :tenantId AND repository = :repository
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<CodeUnit> findNearestNeighbors(
            @Param("tenantId") String tenantId,
            @Param("repository") String repository,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK);
}
