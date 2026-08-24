package edu.ucsb.cs156.diningcachingproxy.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** A MongoDB document holding the proxy's persisted cache performance statistics. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "stats_snapshots")
public class StatsSnapshot {

  @Id private String id;

  private long totalRequests;
  private long cacheHits;
  private long cacheMisses;
}
