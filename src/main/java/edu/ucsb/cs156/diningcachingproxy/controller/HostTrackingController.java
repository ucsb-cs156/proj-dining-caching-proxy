package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.services.HostCount;
import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.diningcachingproxy.services.ResolveHostnameJobFactory;
import edu.ucsb.cs156.jobs.entities.Job;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Lets an admin see every host that has sent requests through the proxy, with a count for each. */
@Tag(name = "Admin - Requesting Hosts")
@RequestMapping("/api/admin/hosts")
@RestController
public class HostTrackingController {

  @Autowired private HostTrackingService hostTrackingService;
  @Autowired private ResolveHostnameJobFactory resolveHostnameJobFactory;

  @Operation(summary = "List all hosts that have sent requests through the proxy, with counts")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("")
  public List<HostCount> allHosts() {
    return hostTrackingService.getHostCounts();
  }

  @Operation(summary = "Retry hostname resolution for one IP address")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/resolve")
  public Job resolveHostname(@Parameter(name = "ip") @RequestParam String ip) {
    return resolveHostnameJobFactory.launch(ip);
  }
}
