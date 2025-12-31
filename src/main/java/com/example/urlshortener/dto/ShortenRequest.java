package com.example.urlshortener.dto;

import java.time.Instant;

public class ShortenRequest {
  private String longUrl;
  private String customAlias;
  private Instant expiry;

  public String getLongUrl() {
    return longUrl;
  }

  public void setLongUrl(String longUrl) {
    this.longUrl = longUrl;
  }

  public String getCustomAlias() {
    return customAlias;
  }

  public void setCustomAlias(String customAlias) {
    this.customAlias = customAlias;
  }

  public Instant getExpiry() {
    return expiry;
  }

  public void setExpiry(Instant expiry) {
    this.expiry = expiry;
  }
}
