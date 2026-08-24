package edu.ucsb.cs156.diningcachingproxy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ucsb.cs156.diningcachingproxy.collections.StatsSnapshot;
import edu.ucsb.cs156.diningcachingproxy.collections.StatsSnapshotRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class StatsServiceTests {

  @Test
  public void starts_at_zero_with_zero_hit_rate_when_no_snapshot_exists() {
    StatsService statsService = new StatsService(emptyRepository());
    assertEquals(0, statsService.getTotalRequests());
    assertEquals(0, statsService.getCacheHits());
    assertEquals(0, statsService.getCacheMisses());
    assertEquals(0.0, statsService.getHitRatePercentage());
  }

  @Test
  public void initializes_counters_from_saved_snapshot() {
    StatsSnapshotRepository repository = mock(StatsSnapshotRepository.class);
    when(repository.findById(StatsService.SNAPSHOT_ID))
        .thenReturn(Optional.of(new StatsSnapshot(StatsService.SNAPSHOT_ID, 10L, 7L, 3L)));

    StatsService statsService = new StatsService(repository);

    assertEquals(10, statsService.getTotalRequests());
    assertEquals(7, statsService.getCacheHits());
    assertEquals(3, statsService.getCacheMisses());
    assertEquals(70.0, statsService.getHitRatePercentage());
  }

  @Test
  public void recordHit_increments_and_persists_total_and_hits() {
    StatsSnapshotRepository repository = emptyRepository();
    StatsService statsService = new StatsService(repository);
    statsService.recordHit();
    assertEquals(1, statsService.getTotalRequests());
    assertEquals(1, statsService.getCacheHits());
    assertEquals(0, statsService.getCacheMisses());
    assertSavedSnapshot(repository, 1L, 1L, 0L);
  }

  @Test
  public void recordMiss_increments_and_persists_total_and_misses() {
    StatsSnapshotRepository repository = emptyRepository();
    StatsService statsService = new StatsService(repository);
    statsService.recordMiss();
    assertEquals(1, statsService.getTotalRequests());
    assertEquals(0, statsService.getCacheHits());
    assertEquals(1, statsService.getCacheMisses());
    assertSavedSnapshot(repository, 1L, 0L, 1L);
  }

  @Test
  public void hit_rate_percentage_is_computed_correctly() {
    StatsService statsService = new StatsService(emptyRepository());
    statsService.recordHit();
    statsService.recordHit();
    statsService.recordHit();
    statsService.recordMiss();
    assertEquals(4, statsService.getTotalRequests());
    assertEquals(3, statsService.getCacheHits());
    assertEquals(1, statsService.getCacheMisses());
    assertEquals(75.0, statsService.getHitRatePercentage());
  }

  @Test
  public void fresh_service_instance_restores_previously_recorded_totals() {
    AtomicReference<StatsSnapshot> storedSnapshot = new AtomicReference<>();
    StatsSnapshotRepository repository = mock(StatsSnapshotRepository.class);
    when(repository.findById(StatsService.SNAPSHOT_ID))
        .thenAnswer(invocation -> Optional.ofNullable(storedSnapshot.get()));
    when(repository.save(any(StatsSnapshot.class)))
        .thenAnswer(
            invocation -> {
              StatsSnapshot saved = invocation.getArgument(0);
              StatsSnapshot copy =
                  new StatsSnapshot(
                      saved.getId(),
                      saved.getTotalRequests(),
                      saved.getCacheHits(),
                      saved.getCacheMisses());
              storedSnapshot.set(copy);
              return saved;
            });

    StatsService firstService = new StatsService(repository);
    firstService.recordHit();
    firstService.recordMiss();

    StatsService restartedService = new StatsService(repository);

    assertEquals(2, restartedService.getTotalRequests());
    assertEquals(1, restartedService.getCacheHits());
    assertEquals(1, restartedService.getCacheMisses());
    assertEquals(50.0, restartedService.getHitRatePercentage());
  }

  private StatsSnapshotRepository emptyRepository() {
    StatsSnapshotRepository repository = mock(StatsSnapshotRepository.class);
    when(repository.findById(StatsService.SNAPSHOT_ID)).thenReturn(Optional.empty());
    return repository;
  }

  private void assertSavedSnapshot(
      StatsSnapshotRepository repository, long totalRequests, long cacheHits, long cacheMisses) {
    ArgumentCaptor<StatsSnapshot> captor = ArgumentCaptor.forClass(StatsSnapshot.class);
    verify(repository).save(captor.capture());
    StatsSnapshot snapshot = captor.getValue();
    assertEquals(StatsService.SNAPSHOT_ID, snapshot.getId());
    assertEquals(totalRequests, snapshot.getTotalRequests());
    assertEquals(cacheHits, snapshot.getCacheHits());
    assertEquals(cacheMisses, snapshot.getCacheMisses());
  }
}
