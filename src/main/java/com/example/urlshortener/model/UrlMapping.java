package com.example.urlshortener.model;

import java.time.Instant;

public class UrlMapping {
  private final String shortCode;
  private final String encryptedLongUrl;
  private final Instant createdAt;
  private final Instant expiry;
  private boolean disabled;

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
