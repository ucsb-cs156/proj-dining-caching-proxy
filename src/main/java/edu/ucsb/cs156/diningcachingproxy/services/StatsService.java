package edu.ucsb.cs156.diningcachingproxy.services;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * In-memory counters tracking cache performance since the proxy was started up. Reset on restart —
 * durable persistence of these stats may come later.
 */
@Service
public class StatsService {

  private final AtomicLong totalRequests = new AtomicLong(0);
  private final AtomicLong cacheHits = new AtomicLong(0);
  private final AtomicLong cacheMisses = new AtomicLong(0);

  public void recordHit() {
    totalRequests.incrementAndGet();
    cacheHits.incrementAndGet();
  }

  public void recordMiss() {
    totalRequests.incrementAndGet();
    cacheMisses.incrementAndGet();
  }

  public long getTotalRequests() {
    return totalRequests.get();
  }

  public long getCacheHits() {
    return cacheHits.get();
  }

  public long getCacheMisses() {
    return cacheMisses.get();
  }

  public double getHitRatePercentage() {
    long total = totalRequests.get();
    if (total == 0) {
      return 0.0;
    }
    return 100.0 * cacheHits.get() / total;
  }
}
