package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Caching proxy for every UCSB API endpoint under {@code /dining/**}. Unlike a controller per known
 * endpoint, this one requires no code change when proj-dining starts calling a new UCSB dining path
 * — the request path (and query string, if any) it actually receives becomes the cache key,
 * discovered at runtime rather than hardcoded here.
 */
@Tag(name = "UCSB Dining Proxy")
@RestController
public class DiningProxyController {

  @Autowired private DiningProxyService diningProxyService;

  @Operation(summary = "Cached proxy for any UCSB dining API endpoint")
  @GetMapping("/dining/**")
  public ResponseEntity<String> proxy(
      HttpServletRequest request,
      @RequestHeader(value = "ucsb-api-key", required = false) String apiKey,
      @RequestHeader(value = "ucsb-api-version", required = false) String apiVersion) {
    String requestPath = request.getRequestURI();
    String queryString = request.getQueryString();
    if (queryString != null) {
      requestPath = requestPath + "?" + queryString;
    }
    return diningProxyService.proxyGet(requestPath, apiKey, apiVersion);
  }
}
