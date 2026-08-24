package com.henry.cinnamon.repository;

import com.henry.cinnamon.model.IngestionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngestionJobRepository extends JpaRepository<IngestionJob, UUID> {
}
