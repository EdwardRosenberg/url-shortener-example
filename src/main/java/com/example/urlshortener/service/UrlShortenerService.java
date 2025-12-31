package com.example.urlshortener.service;

import com.example.urlshortener.model.UrlMapping;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UrlShortenerService {
  private static final String BASE62_CHARS =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final int SHORT_CODE_LENGTH = 6;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final ConcurrentHashMap<String, UrlMapping> urlStore = new ConcurrentHashMap<>();
  private final EncryptionService encryptionService;

  public UrlShortenerService(EncryptionService encryptionService) {
    this.encryptionService = encryptionService;
  }

  public UrlMapping shorten(String longUrl, String customAlias, Instant expiry) {
    validateUrl(longUrl);

    // Encrypt the URL before storing
    String encryptedUrl = encryptionService.encrypt(longUrl);

    String shortCode;
    if (customAlias != null && !customAlias.isEmpty()) {
      if (urlStore.containsKey(customAlias)) {
        throw new IllegalArgumentException("Custom alias already exists");
      }
      shortCode = customAlias;
    } else {
      shortCode = generateShortCode();
    }

    UrlMapping mapping = new UrlMapping(shortCode, encryptedUrl, expiry);
    urlStore.put(shortCode, mapping);
    return mapping;
  }

  public Optional<UrlMapping> findByCode(String code) {
    return Optional.ofNullable(urlStore.get(code));
  }

  public String decryptUrl(String encryptedUrl) {
    return encryptionService.decrypt(encryptedUrl);
  }

  private String generateShortCode() {
    int maxAttempts = 10;
    for (int i = 0; i < maxAttempts; i++) {
      String code = generateRandomCode();
      if (!urlStore.containsKey(code)) {
        return code;
      }
    }
    throw new IllegalStateException(
        "Failed to generate unique short code after " + maxAttempts + " attempts");
  }

  private String generateRandomCode() {
    StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
    for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
      sb.append(BASE62_CHARS.charAt(RANDOM.nextInt(BASE62_CHARS.length())));
    }
    return sb.toString();
  }

  private void validateUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be empty");
    }

    // Check for javascript: protocol
    if (url.toLowerCase().startsWith("javascript:")) {
      throw new IllegalArgumentException("javascript: URLs are not allowed");
    }

    // Check for local file paths
    if (url.toLowerCase().startsWith("file:")) {
      throw new IllegalArgumentException("file: URLs are not allowed");
    }

    // Validate URL format
    try {
      URI uri = new URI(url);
      uri.toURL();

      // Must have a scheme
      if (uri.getScheme() == null) {
        throw new IllegalArgumentException("URL must have a valid scheme (http/https)");
      }

      // Must be http or https
      String scheme = uri.getScheme().toLowerCase();
      if (!scheme.equals("http") && !scheme.equals("https")) {
        throw new IllegalArgumentException("Only http and https schemes are allowed");
      }

    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
    }
  }
}
