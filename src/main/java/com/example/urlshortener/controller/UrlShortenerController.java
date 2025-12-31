package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.dto.UrlMetadata;
import com.example.urlshortener.model.UrlMapping;
import com.example.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortenerController {

  private final UrlShortenerService urlShortenerService;

  public UrlShortenerController(UrlShortenerService urlShortenerService) {
    this.urlShortenerService = urlShortenerService;
  }

  @PostMapping("/shorten")
  public ResponseEntity<?> shortenUrl(
      @RequestBody ShortenRequest request, HttpServletRequest httpRequest) {
    try {
      UrlMapping mapping =
          urlShortenerService.shorten(
              request.getLongUrl(), request.getCustomAlias(), request.getExpiry());

      String baseUrl = getBaseUrl(httpRequest);
      String shortUrl = baseUrl + "/" + mapping.getShortCode();

      return ResponseEntity.ok(new ShortenResponse(shortUrl));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    return urlShortenerService
        .findByCode(code)
        .map(
            mapping -> {
              if (mapping.isDisabled()) {
                return ResponseEntity.status(HttpStatus.GONE).<Void>build();
              }
              if (mapping.isExpired()) {
                return ResponseEntity.status(HttpStatus.GONE).<Void>build();
              }
              String decryptedUrl = urlShortenerService.decryptUrl(mapping.getEncryptedLongUrl());
              return ResponseEntity.status(HttpStatus.FOUND)
                  .header("Location", decryptedUrl)
                  .<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @RequestMapping(value = "/{code}", method = RequestMethod.HEAD)
  public ResponseEntity<UrlMetadata> getMetadata(@PathVariable String code) {
    return urlShortenerService
        .findByCode(code)
        .map(
            mapping -> {
              if (mapping.isDisabled() || mapping.isExpired()) {
                return ResponseEntity.status(HttpStatus.GONE).<UrlMetadata>build();
              }
              String decryptedUrl = urlShortenerService.decryptUrl(mapping.getEncryptedLongUrl());
              UrlMetadata metadata =
                  new UrlMetadata(
                      mapping.getShortCode(),
                      decryptedUrl,
                      mapping.getCreatedAt(),
                      mapping.getExpiry(),
                      mapping.isDisabled(),
                      mapping.isExpired());
              return ResponseEntity.ok(metadata);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private String getBaseUrl(HttpServletRequest request) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();
    String contextPath = request.getContextPath();

    StringBuilder url = new StringBuilder();
    url.append(scheme).append("://").append(serverName);

    if ((scheme.equals("http") && serverPort != 80)
        || (scheme.equals("https") && serverPort != 443)) {
      url.append(":").append(serverPort);
    }

    url.append(contextPath);
    return url.toString();
  }
}
