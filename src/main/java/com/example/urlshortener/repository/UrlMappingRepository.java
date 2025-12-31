package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Combined repository interface for URL mappings. Extends both read and write repository interfaces
 * to support future read/write splitting while currently using a single database.
 */
@Repository
public interface UrlMappingRepository
    extends JpaRepository<UrlMapping, String>, UrlMappingReadRepository, UrlMappingWriteRepository {

  @Override
  default Optional<UrlMapping> findByShortCode(String shortCode) {
    return findById(shortCode);
  }

  @Override
  default boolean existsByShortCode(String shortCode) {
    return existsById(shortCode);
  }

  @Override
  default UrlMapping persist(UrlMapping urlMapping) {
    return save(urlMapping);
  }
}
