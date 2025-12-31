package com.example.urlshortener.repository;

import com.example.urlshortener.model.UrlMapping;

/**
 * Write operations for URL mappings. This interface defines the contract for write operations,
 * enabling future separation of read and write data sources.
 */
public interface UrlMappingWriteRepository {
  UrlMapping persist(UrlMapping urlMapping);
}
