package edu.ucsb.cs156.diningcachingproxy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StatsServiceTests {

  @Test
  public void starts_at_zero_with_zero_hit_rate() {
    StatsService statsService = new StatsService();
    assertEquals(0, statsService.getTotalRequests());
    assertEquals(0, statsService.getCacheHits());
    assertEquals(0, statsService.getCacheMisses());
    assertEquals(0.0, statsService.getHitRatePercentage());
  }

  @Test
  public void recordHit_increments_total_and_hits() {
    StatsService statsService = new StatsService();
    statsService.recordHit();
    assertEquals(1, statsService.getTotalRequests());
    assertEquals(1, statsService.getCacheHits());
    assertEquals(0, statsService.getCacheMisses());
  }

  @Test
  public void recordMiss_increments_total_and_misses() {
    StatsService statsService = new StatsService();
    statsService.recordMiss();
    assertEquals(1, statsService.getTotalRequests());
    assertEquals(0, statsService.getCacheHits());
    assertEquals(1, statsService.getCacheMisses());
  }

  @Test
  public void hit_rate_percentage_is_computed_correctly() {
    StatsService statsService = new StatsService();
    statsService.recordHit();
    statsService.recordHit();
    statsService.recordHit();
    statsService.recordMiss();
    assertEquals(4, statsService.getTotalRequests());
    assertEquals(3, statsService.getCacheHits());
    assertEquals(1, statsService.getCacheMisses());
    assertEquals(75.0, statsService.getHitRatePercentage());
  }
}
