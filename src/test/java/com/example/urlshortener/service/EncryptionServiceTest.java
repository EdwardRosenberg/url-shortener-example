package com.example.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EncryptionServiceTest {

  @Test
  void testEncryptDecrypt_Success() {
    EncryptionService service = new EncryptionService("");
    String plaintext = "https://example.com/test?user=john&id=12345";

    String encrypted = service.encrypt(plaintext);
    String decrypted = service.decrypt(encrypted);

    assertNotEquals(plaintext, encrypted);
    assertEquals(plaintext, decrypted);
  }

  @Test
  void testEncrypt_ProducesDifferentCiphertexts() {
    EncryptionService service = new EncryptionService("");
    String plaintext = "https://example.com/test";

    String encrypted1 = service.encrypt(plaintext);
    String encrypted2 = service.encrypt(plaintext);

    // Same plaintext should produce different ciphertexts due to random IV
    assertNotEquals(encrypted1, encrypted2);

    // Both should decrypt to the same plaintext
    assertEquals(plaintext, service.decrypt(encrypted1));
    assertEquals(plaintext, service.decrypt(encrypted2));
  }

  @Test
  void testEncrypt_HandlesSpecialCharacters() {
    EncryptionService service = new EncryptionService("");
    String plaintext = "https://example.com/test?q=hello+world&foo=bar&special=!@#$%^&*()";

    String encrypted = service.encrypt(plaintext);
    String decrypted = service.decrypt(encrypted);

    assertEquals(plaintext, decrypted);
  }

  @Test
  void testEncrypt_HandlesLongUrls() {
    EncryptionService service = new EncryptionService("");
    StringBuilder sb = new StringBuilder("https://example.com/");
    for (int i = 0; i < 1000; i++) {
      sb.append("a");
    }
    String plaintext = sb.toString();

    String encrypted = service.encrypt(plaintext);
    String decrypted = service.decrypt(encrypted);

    assertEquals(plaintext, decrypted);
  }

  @Test
  void testDecrypt_InvalidData_ThrowsException() {
    EncryptionService service = new EncryptionService("");

    assertThrows(RuntimeException.class, () -> service.decrypt("invalid-data"));
  }

  @Test
  void testEncryptionService_WithProvidedKey() {
    // Use a properly generated test key (32 bytes)
    String key = "dGVzdGtleWZvcmVuY3J5cHRpb250ZXN0aW5nMTIzNDU="; // base64 of 32-byte string
    EncryptionService service = new EncryptionService(key);

    String plaintext = "https://example.com/test";
    String encrypted = service.encrypt(plaintext);
    String decrypted = service.decrypt(encrypted);

    assertEquals(plaintext, decrypted);
  }

  @Test
  void testEncryptionService_InvalidKeyLength_ThrowsException() {
    String invalidKey = "short";

    assertThrows(IllegalArgumentException.class, () -> new EncryptionService(invalidKey));
  }
}
