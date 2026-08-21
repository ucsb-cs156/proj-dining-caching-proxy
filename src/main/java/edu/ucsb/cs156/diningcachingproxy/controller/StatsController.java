package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the four in-memory cache performance counters. */
@Tag(name = "Stats")
@RequestMapping("/api/stats")
@RestController
public class StatsController {

  @Autowired private StatsService statsService;

  public record StatsDTO(
      long totalRequests, long cacheHits, long cacheMisses, double hitRatePercentage) {}

  @Operation(summary = "Get cache performance statistics since the proxy started up")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public StatsDTO getStats() {
    return new StatsDTO(
        statsService.getTotalRequests(),
        statsService.getCacheHits(),
        statsService.getCacheMisses(),
        statsService.getHitRatePercentage());
  }
}
