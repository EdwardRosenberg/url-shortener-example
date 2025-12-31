package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;
import java.util.Optional;

/** Read-only operations for URL mappings. */
public interface UrlMappingReadRepository {
  Optional<UrlMapping> findByShortCode(String shortCode);

  boolean existsByShortCode(String shortCode);
}
