package edu.ucsb.cs156.diningcachingproxy.jobs;

import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.jobs.services.JobContext;
import edu.ucsb.cs156.jobs.services.JobContextConsumer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Builder;

/**
 * Attempts a reverse DNS lookup for one IP address, updating {@link HostTrackingService} with the
 * result (the resolved hostname, or the address itself if resolution fails). Runs asynchronously
 * via {@code JobService} - reverse DNS is a blocking network call with no built-in timeout, so it
 * must never run inline in the proxy's request path.
 */
@Builder
public class ResolveHostnameJob implements JobContextConsumer {

  private String ipAddress;
  private HostTrackingService hostTrackingService;

  @Override
  public void accept(JobContext ctx) throws Exception {
    ctx.log("Resolving hostname for " + ipAddress);
    String hostname = resolveHostname(ipAddress);
    hostTrackingService.resolveHostname(ipAddress, hostname);
    ctx.log("Resolved " + ipAddress + " to " + hostname);
  }

  private String resolveHostname(String remoteAddress) {
    try {
      String hostname = InetAddress.getByName(remoteAddress).getCanonicalHostName();
      // getCanonicalHostName() doesn't throw when reverse DNS fails - it falls back to
      // returning the address's own textual form, which we detect here.
      return hostname.equals(remoteAddress) ? remoteAddress : hostname;
    } catch (UnknownHostException e) {
      return remoteAddress;
    }
  }
}
