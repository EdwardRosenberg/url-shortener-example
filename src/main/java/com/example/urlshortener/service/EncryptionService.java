package com.example.urlshortener.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private final SecretKey secretKey;
  private final SecureRandom secureRandom;

  public EncryptionService(@Value("${encryption.key:}") String encryptionKey) {
    this.secureRandom = new SecureRandom();

    if (encryptionKey == null || encryptionKey.isEmpty()) {
      // Generate a random key for development/testing
      byte[] keyBytes = new byte[32]; // 256-bit key
      secureRandom.nextBytes(keyBytes);
      this.secretKey = new SecretKeySpec(keyBytes, "AES");
    } else {
      // Use provided key (must be base64-encoded 32 bytes)
      byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
      if (keyBytes.length != 32) {
        throw new IllegalArgumentException("Encryption key must be 32 bytes (256 bits)");
      }
      this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }
  }

  public String encrypt(String plaintext) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      // Combine IV and ciphertext
      ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
      byteBuffer.put(iv);
      byteBuffer.put(ciphertext);

      return Base64.getEncoder().encodeToString(byteBuffer.array());
    } catch (Exception e) {
      throw new RuntimeException(
          "Encryption failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
    }
  }

  public String decrypt(String encryptedText) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

      ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);

      byte[] iv = new byte[GCM_IV_LENGTH];
      byteBuffer.get(iv);

      byte[] ciphertext = new byte[byteBuffer.remaining()];
      byteBuffer.get(ciphertext);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      byte[] plaintext = cipher.doFinal(ciphertext);

      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(
          "Decryption failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
    }
  }
}
