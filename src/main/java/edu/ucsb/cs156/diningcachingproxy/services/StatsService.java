package edu.ucsb.cs156.diningcachingproxy.services;

import edu.ucsb.cs156.diningcachingproxy.collections.StatsSnapshot;
import edu.ucsb.cs156.diningcachingproxy.collections.StatsSnapshotRepository;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Counters tracking cache performance, persisted to MongoDB so they survive application restarts.
 */
@Service
public class StatsService {

  static final String SNAPSHOT_ID = "global";

  private final StatsSnapshotRepository statsSnapshotRepository;
  private final AtomicLong totalRequests;
  private final AtomicLong cacheHits;
  private final AtomicLong cacheMisses;

  public StatsService(StatsSnapshotRepository statsSnapshotRepository) {
    this.statsSnapshotRepository = statsSnapshotRepository;
    StatsSnapshot snapshot =
        statsSnapshotRepository
            .findById(SNAPSHOT_ID)
            .orElseGet(() -> new StatsSnapshot(SNAPSHOT_ID, 0, 0, 0));
    totalRequests = new AtomicLong(snapshot.getTotalRequests());
    cacheHits = new AtomicLong(snapshot.getCacheHits());
    cacheMisses = new AtomicLong(snapshot.getCacheMisses());
  }

  public synchronized void recordHit() {
    totalRequests.incrementAndGet();
    cacheHits.incrementAndGet();
    saveSnapshot();
  }

  public synchronized void recordMiss() {
    totalRequests.incrementAndGet();
    cacheMisses.incrementAndGet();
    saveSnapshot();
  }

  private void saveSnapshot() {
    statsSnapshotRepository.save(
        new StatsSnapshot(SNAPSHOT_ID, totalRequests.get(), cacheHits.get(), cacheMisses.get()));
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
