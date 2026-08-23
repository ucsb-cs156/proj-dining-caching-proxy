package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.diningcachingproxy.services.ResolveHostnameJobFactory;
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
  @Autowired private HostTrackingService hostTrackingService;
  @Autowired private ResolveHostnameJobFactory resolveHostnameJobFactory;

  @Operation(summary = "Cached proxy for any UCSB dining API endpoint")
  @GetMapping("/dining/**")
  public ResponseEntity<String> proxy(
      HttpServletRequest request,
      @RequestHeader(value = "ucsb-api-key", required = false) String apiKey,
      @RequestHeader(value = "ucsb-api-version", required = false) String apiVersion,
      @RequestHeader(value = "X-Requesting-App", required = false) String requestingApp) {
    String clientAddress = extractClientAddress(request);
    // A caller that self-identifies is tracked by that name instead of its IP - callers on the
    // same Dokku host as this proxy all share Docker's internal bridge network, so the IP alone
    // can't distinguish one calling app from another and never resolves via reverse DNS anyway.
    String identifier =
        (requestingApp != null && !requestingApp.isBlank()) ? requestingApp : clientAddress;
    boolean isNewAddress = hostTrackingService.recordRequest(identifier);
    if (isNewAddress && identifier.equals(clientAddress)) {
      resolveHostnameJobFactory.launch(clientAddress);
    }

    String requestPath = request.getRequestURI();
    String queryString = request.getQueryString();
    if (queryString != null) {
      requestPath = requestPath + "?" + queryString;
    }
    return diningProxyService.proxyGet(requestPath, apiKey, apiVersion);
  }

  // This app runs behind Dokku's reverse proxy, so request.getRemoteAddr() would only ever
  // report nginx's own internal address - the true caller's IP is in X-Forwarded-For.
  private String extractClientAddress(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
