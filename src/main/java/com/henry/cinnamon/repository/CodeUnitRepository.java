package com.henry.cinnamon.repository;

import com.henry.cinnamon.model.CodeUnit;
import com.henry.cinnamon.model.DuplicatePairProjection;
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
        WHERE tenant_id = :tenantId AND repository = :repository AND embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<CodeUnit> findNearestNeighbors(
            @Param("tenantId") String tenantId,
            @Param("repository") String repository,
            @Param("queryVector") String queryVector,
            @Param("topK") int topK);

    // 4. Server-Side Whole-Repository Vector Self-Join Query (< 50ms)
    @Query(value = """
        SELECT 
            c1.file_path AS filePathA,
            c1.function_name AS functionNameA,
            c2.file_path AS filePathB,
            c2.function_name AS functionNameB,
            c1.line_count AS lineCountA,
            c2.line_count AS lineCountB,
            CAST(1.0 - (c1.embedding <=> c2.embedding) AS DOUBLE PRECISION) AS similarityScore
        FROM code_units c1
        JOIN code_units c2 
          ON c1.tenant_id = c2.tenant_id 
         AND c1.repository = c2.repository
         AND c1.id < c2.id
         AND (c1.file_path != c2.file_path OR c1.function_name != c2.function_name)
        WHERE c1.tenant_id = :tenantId
          AND c1.repository = :repository
          AND c1.embedding IS NOT NULL
          AND c2.embedding IS NOT NULL
          AND (c1.embedding <=> c2.embedding) <= (1.0 - :minSimilarity)
          AND (:pathPrefix IS NULL OR :pathPrefix = '' OR c1.file_path LIKE CONCAT(:pathPrefix, '%') OR c2.file_path LIKE CONCAT(:pathPrefix, '%'))
        ORDER BY similarityScore DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<DuplicatePairProjection> scanRepositoryDuplicates(
            @Param("tenantId") String tenantId,
            @Param("repository") String repository,
            @Param("minSimilarity") double minSimilarity,
            @Param("pathPrefix") String pathPrefix,
            @Param("limit") int limit);
}
