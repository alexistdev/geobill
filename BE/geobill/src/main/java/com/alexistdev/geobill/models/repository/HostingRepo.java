package com.alexistdev.geobill.models.repository;

import com.alexistdev.geobill.models.entity.Hosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HostingRepo extends JpaRepository<Hosting, UUID> {
}
