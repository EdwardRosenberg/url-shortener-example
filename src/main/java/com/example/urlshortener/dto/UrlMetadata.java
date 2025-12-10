package com.example.urlshortener.dto;

import java.time.Instant;

public class UrlMetadata {
  private String shortCode;
  private String longUrl;
  private Instant createdAt;
  private Instant expiry;
  private boolean disabled;
  private boolean expired;

  public UrlMetadata() {}

  public UrlMetadata(
      String shortCode,
      String longUrl,
      Instant createdAt,
      Instant expiry,
      boolean disabled,
      boolean expired) {
    this.shortCode = shortCode;
    this.longUrl = longUrl;
    this.createdAt = createdAt;
    this.expiry = expiry;
    this.disabled = disabled;
    this.expired = expired;
  }

  public String getShortCode() {
    return shortCode;
  }

  public void setShortCode(String shortCode) {
    this.shortCode = shortCode;
  }

  public String getLongUrl() {
    return longUrl;
  }

  public void setLongUrl(String longUrl) {
    this.longUrl = longUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getExpiry() {
    return expiry;
  }

  public void setExpiry(Instant expiry) {
    this.expiry = expiry;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public boolean isExpired() {
    return expired;
  }

  public void setExpired(boolean expired) {
    this.expired = expired;
  }
}
