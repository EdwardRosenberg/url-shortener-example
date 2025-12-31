package com.example.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.urlshortener.model.UrlMapping;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UrlShortenerServiceTest {

  private UrlShortenerService service;
  private EncryptionService encryptionService;

  @BeforeEach
  void setUp() {
    encryptionService = new EncryptionService("");
    service = new UrlShortenerService(encryptionService);
  }

  @Test
  void testShortenUrl_Success() {
    String longUrl = "https://example.com/very/long/url";

    UrlMapping mapping = service.shorten(longUrl, null, null);

    assertNotNull(mapping);
    assertNotNull(mapping.getShortCode());
    // URL is encrypted, so we decrypt to verify
    assertEquals(longUrl, service.decryptUrl(mapping.getEncryptedLongUrl()));
    assertFalse(mapping.isDisabled());
    assertFalse(mapping.isExpired());
  }

  @Test
  void testShortenUrl_WithCustomAlias() {
    String longUrl = "https://example.com/test";
    String customAlias = "myalias";

    UrlMapping mapping = service.shorten(longUrl, customAlias, null);

    assertEquals(customAlias, mapping.getShortCode());
    assertEquals(longUrl, service.decryptUrl(mapping.getEncryptedLongUrl()));
  }

  @Test
  void testShortenUrl_CustomAliasDuplicate_ThrowsException() {
    String longUrl = "https://example.com/test";
    String customAlias = "duplicate";

    service.shorten(longUrl, customAlias, null);

    assertThrows(IllegalArgumentException.class, () -> service.shorten(longUrl, customAlias, null));
  }

  @Test
  void testShortenUrl_WithExpiry() {
    String longUrl = "https://example.com/test";
    Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

    UrlMapping mapping = service.shorten(longUrl, null, expiry);

    assertEquals(expiry, mapping.getExpiry());
    assertFalse(mapping.isExpired());
  }

  @Test
  void testFindByCode_Success() {
    String longUrl = "https://example.com/test";
    UrlMapping mapping = service.shorten(longUrl, null, null);

    Optional<UrlMapping> found = service.findByCode(mapping.getShortCode());

    assertTrue(found.isPresent());
    assertEquals(mapping.getShortCode(), found.get().getShortCode());
    assertEquals(longUrl, service.decryptUrl(found.get().getEncryptedLongUrl()));
  }

  @Test
  void testFindByCode_NotFound() {
    Optional<UrlMapping> found = service.findByCode("nonexistent");

    assertFalse(found.isPresent());
  }

  @Test
  void testValidateUrl_NullUrl_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> service.shorten(null, null, null));
  }

  @Test
  void testValidateUrl_EmptyUrl_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> service.shorten("", null, null));
  }

  @Test
  void testValidateUrl_JavascriptUrl_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.shorten("javascript:alert('xss')", null, null));
  }

  @Test
  void testValidateUrl_FileUrl_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> service.shorten("file:///etc/passwd", null, null));
  }

  @Test
  void testValidateUrl_NoScheme_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> service.shorten("example.com/test", null, null));
  }

  @Test
  void testValidateUrl_InvalidScheme_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.shorten("ftp://example.com/test", null, null));
  }

  @Test
  void testValidateUrl_HttpUrl_Success() {
    String longUrl = "http://example.com/test";

    UrlMapping mapping = service.shorten(longUrl, null, null);

    assertNotNull(mapping);
    assertEquals(longUrl, service.decryptUrl(mapping.getEncryptedLongUrl()));
  }

  @Test
  void testValidateUrl_HttpsUrl_Success() {
    String longUrl = "https://example.com/test";

    UrlMapping mapping = service.shorten(longUrl, null, null);

    assertNotNull(mapping);
    assertEquals(longUrl, service.decryptUrl(mapping.getEncryptedLongUrl()));
  }

  @Test
  void testIsExpired_ExpiredUrl() {
    String longUrl = "https://example.com/test";
    Instant expiry = Instant.now().minus(1, ChronoUnit.HOURS);

    UrlMapping mapping = service.shorten(longUrl, null, expiry);

    assertTrue(mapping.isExpired());
  }

  @Test
  void testIsDisabled_DisabledUrl() {
    String longUrl = "https://example.com/test";
    UrlMapping mapping = service.shorten(longUrl, null, null);

    assertFalse(mapping.isDisabled());

    mapping.setDisabled(true);

    assertTrue(mapping.isDisabled());
  }

  @Test
  void testEncryptionDecryption() {
    String originalUrl = "https://example.com/test?user=john&id=12345";

    UrlMapping mapping = service.shorten(originalUrl, null, null);

    // Verify the stored URL is encrypted (not equal to original)
    assertNotEquals(originalUrl, mapping.getEncryptedLongUrl());

    // Verify decryption returns the original URL
    String decryptedUrl = service.decryptUrl(mapping.getEncryptedLongUrl());
    assertEquals(originalUrl, decryptedUrl);
  }
}
