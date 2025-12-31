package com.example.urlshortener.model;

import java.time.Instant;

public class UrlMapping {
  private final String shortCode;
  private final String longUrl;
  private final Instant createdAt;
  private final Instant expiry;
  private boolean disabled;

  public UrlMapping(String shortCode, String longUrl, Instant expiry) {
    this.shortCode = shortCode;
    this.longUrl = longUrl;
    this.createdAt = Instant.now();
    this.expiry = expiry;
    this.disabled = false;
  }

  public String getShortCode() {
    return shortCode;
  }

  public String getLongUrl() {
    return longUrl;
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
