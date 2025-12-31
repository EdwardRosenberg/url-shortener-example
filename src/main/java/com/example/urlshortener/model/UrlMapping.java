package com.example.urlshortener.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "links_core")
public class UrlMapping {
  @Id
  @Column(name = "code", nullable = false)
  private String shortCode;

  @Column(name = "target_url", nullable = false, columnDefinition = "TEXT")
  private String encryptedLongUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expiry_ts")
  private Instant expiry;

  @Column(name = "is_disabled", nullable = false)
  private boolean disabled;

  protected UrlMapping() {
    // JPA requires a no-arg constructor
  }

  public UrlMapping(String shortCode, String encryptedLongUrl, Instant expiry) {
    this.shortCode = shortCode;
    this.encryptedLongUrl = encryptedLongUrl;
    this.createdAt = Instant.now();
    this.expiry = expiry;
    this.disabled = false;
  }

  public String getShortCode() {
    return shortCode;
  }

  public String getEncryptedLongUrl() {
    return encryptedLongUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiry() {
    return expiry;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public boolean isExpired() {
    return expiry != null && Instant.now().isAfter(expiry);
  }
}
