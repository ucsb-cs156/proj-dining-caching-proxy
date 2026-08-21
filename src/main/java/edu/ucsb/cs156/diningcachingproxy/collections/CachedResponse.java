package edu.ucsb.cs156.diningcachingproxy.collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A MongoDB document holding the cached response for a single upstream UCSB API request, keyed by
 * the resolved request path (no query parameters are used by any of the endpoints this proxy
 * currently serves).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "cached_responses")
public class CachedResponse {

  @Id private String id;

  @Indexed(unique = true)
  private String requestPath;

  private int responseStatus;
  private String responseBody;
}
