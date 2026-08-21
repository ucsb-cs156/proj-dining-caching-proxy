package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Caching proxy for the UCSB dining commons list endpoint. */
@Tag(name = "UCSB Dining Commons Proxy")
@RestController
public class DiningCommonsController {

  public static final String ENDPOINT = "/dining/commons/v1/";

  @Autowired private DiningProxyService diningProxyService;

  @Operation(summary = "Cached proxy for the list of dining commons")
  @GetMapping(ENDPOINT)
  public ResponseEntity<String> diningCommons(
      @RequestHeader(value = "ucsb-api-key", required = false) String apiKey,
      @RequestHeader(value = "ucsb-api-version", required = false) String apiVersion) {
    return diningProxyService.proxyGet(ENDPOINT, apiKey, apiVersion);
  }
}
