package com.example.urlshortener.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.urlshortener.dto.ShortenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void testShortenUrl_Success() throws Exception {
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/test");

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl").exists())
        .andExpect(jsonPath("$.shortUrl").value(startsWith("http://")));
  }

  @Test
  void testShortenUrl_WithCustomAlias() throws Exception {
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/test");
    request.setCustomAlias("myalias");

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl").value(startsWith("http://")))
        .andExpect(jsonPath("$.shortUrl").value(org.hamcrest.Matchers.containsString("myalias")));
  }

  @Test
  void testShortenUrl_WithExpiry() throws Exception {
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/test");
    request.setExpiry(Instant.now().plus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl").exists());
  }

  @Test
  void testShortenUrl_InvalidUrl_ReturnsBadRequest() throws Exception {
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("javascript:alert('xss')");

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testShortenUrl_DuplicateAlias_ReturnsBadRequest() throws Exception {
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/test");
    request.setCustomAlias("duplicate123");

    // First request should succeed
    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Second request with same alias should fail
    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testRedirect_Success() throws Exception {
    // First, create a short URL
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/redirect-test");
    request.setCustomAlias("redirect123");

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Then, test the redirect
    mockMvc
        .perform(get("/redirect123"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/redirect-test"));
  }

  @Test
  void testRedirect_NotFound() throws Exception {
    mockMvc.perform(get("/nonexistent")).andExpect(status().isNotFound());
  }

  @Test
  void testRedirect_Expired_ReturnsGone() throws Exception {
    // Create a URL that's already expired
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/expired-test");
    request.setCustomAlias("expired123");
    request.setExpiry(Instant.now().minus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Try to access the expired URL
    mockMvc.perform(get("/expired123")).andExpect(status().isGone());
  }

  @Test
  void testHeadMetadata_Success() throws Exception {
    // Create a short URL
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/metadata-test");
    request.setCustomAlias("metadata123");

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Get metadata
    mockMvc.perform(head("/metadata123")).andExpect(status().isOk());
  }

  @Test
  void testHeadMetadata_NotFound() throws Exception {
    mockMvc.perform(head("/nonexistent")).andExpect(status().isNotFound());
  }

  @Test
  void testHeadMetadata_Expired_ReturnsGone() throws Exception {
    // Create an expired URL
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/expired-metadata");
    request.setCustomAlias("expiredmeta123");
    request.setExpiry(Instant.now().minus(1, ChronoUnit.HOURS));

    mockMvc
        .perform(
            post("/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Try to get metadata for expired URL
    mockMvc.perform(head("/expiredmeta123")).andExpect(status().isGone());
  }

  @Test
  void testFullFlow_ShortenAndRedirect() throws Exception {
    // Step 1: Shorten a URL
    ShortenRequest request = new ShortenRequest();
    request.setLongUrl("https://example.com/full-flow-test");

    MvcResult result =
        mockMvc
            .perform(
                post("/shorten")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortUrl").exists())
            .andReturn();

    // Extract the short code from the response
    String responseBody = result.getResponse().getContentAsString();
    String shortUrl = objectMapper.readTree(responseBody).get("shortUrl").asText();
    String shortCode = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

    // Step 2: Use the short code to redirect
    mockMvc
        .perform(get("/" + shortCode))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/full-flow-test"));
  }
}
