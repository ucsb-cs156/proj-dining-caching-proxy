package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Caching proxy for the UCSB "menu items at a dining common/meal" endpoint. */
@Tag(name = "UCSB Dining Menu Items Proxy")
@RestController
public class UCSBDiningMenuItemsController {

  @Autowired private DiningProxyService diningProxyService;

  @Operation(summary = "Cached proxy for menu items at a dining common, date, and meal")
  @GetMapping("/dining/menu/v1/{date-time}/{dining-common-code}/{meal-code}")
  public ResponseEntity<String> diningMenuItems(
      @PathVariable("date-time") String dateTime,
      @PathVariable("dining-common-code") String diningCommonCode,
      @PathVariable("meal-code") String mealCode,
      @RequestHeader(value = "ucsb-api-key", required = false) String apiKey,
      @RequestHeader(value = "ucsb-api-version", required = false) String apiVersion) {
    String requestPath = "/dining/menu/v1/%s/%s/%s".formatted(dateTime, diningCommonCode, mealCode);
    return diningProxyService.proxyGet(requestPath, apiKey, apiVersion);
  }
}
