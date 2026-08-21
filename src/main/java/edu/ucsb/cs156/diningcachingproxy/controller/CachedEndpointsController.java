package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.collections.CachedResponse;
import edu.ucsb.cs156.diningcachingproxy.collections.CachedResponseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets an admin see every API endpoint the proxy has cached so far, with a hit count for each — the
 * observation-only groundwork for per-endpoint TTL policy (see issue #2).
 */
@Tag(name = "Admin - Cached Endpoints")
@RequestMapping("/api/admin/endpoints")
@RestController
public class CachedEndpointsController {

  @Autowired private CachedResponseRepository cachedResponseRepository;

  public record CachedEndpointDTO(String requestPath, long hitCount) {
    public CachedEndpointDTO(CachedResponse cachedResponse) {
      this(cachedResponse.getRequestPath(), cachedResponse.getHitCount());
    }
  }

  @Operation(summary = "List all API endpoints the proxy has cached, with hit counts")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("")
  public List<CachedEndpointDTO> allEndpoints() {
    return cachedResponseRepository.findAll().stream().map(CachedEndpointDTO::new).toList();
  }
}
