package edu.ucsb.cs156.diningcachingproxy.jobs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.jobs.entities.Job;
import edu.ucsb.cs156.jobs.services.JobContext;
import org.junit.jupiter.api.Test;

public class ResolveHostnameJobTests {

  @Test
  public void resolves_a_loopback_address_and_updates_the_host_tracking_service() throws Exception {
    HostTrackingService hostTrackingService = mock(HostTrackingService.class);
    ResolveHostnameJob job =
        ResolveHostnameJob.builder()
            .ipAddress("127.0.0.1")
            .hostTrackingService(hostTrackingService)
            .build();
    JobContext ctx = new JobContext(null, Job.builder().build());

    job.accept(ctx);

    // 127.0.0.1 reliably reverse-resolves to some form of "localhost" on every platform this
    // test runs on, so this is a real (not mocked) resolution.
    verify(hostTrackingService)
        .resolveHostname(eq("127.0.0.1"), argThat(h -> h.toLowerCase().contains("localhost")));
    assertTrue(ctx.getJob().getLog().contains("Resolving hostname for 127.0.0.1"));
  }

  @Test
  public void falls_back_to_the_raw_address_when_a_valid_ip_has_no_reverse_dns_entry()
      throws Exception {
    HostTrackingService hostTrackingService = mock(HostTrackingService.class);
    // 192.0.2.1 is in the TEST-NET-1 block (RFC 5737), reserved for documentation - it is a
    // syntactically valid IP (so InetAddress.getByName never throws) but is guaranteed to have
    // no PTR record anywhere, so getCanonicalHostName() silently returns the address itself
    // rather than throwing.
    String testNetAddress = "192.0.2.1";
    ResolveHostnameJob job =
        ResolveHostnameJob.builder()
            .ipAddress(testNetAddress)
            .hostTrackingService(hostTrackingService)
            .build();
    JobContext ctx = new JobContext(null, Job.builder().build());

    job.accept(ctx);

    verify(hostTrackingService).resolveHostname(testNetAddress, testNetAddress);
  }

  @Test
  public void falls_back_to_the_raw_address_when_reverse_dns_resolution_fails() throws Exception {
    HostTrackingService hostTrackingService = mock(HostTrackingService.class);
    // Not a syntactically valid IP literal, so InetAddress.getByName attempts (and fails) a
    // forward hostname lookup, exercising the UnknownHostException fallback path.
    String bogusAddress = "not-a-real-host.invalid";
    ResolveHostnameJob job =
        ResolveHostnameJob.builder()
            .ipAddress(bogusAddress)
            .hostTrackingService(hostTrackingService)
            .build();
    JobContext ctx = new JobContext(null, Job.builder().build());

    job.accept(ctx);

    verify(hostTrackingService).resolveHostname(bogusAddress, bogusAddress);
  }
}
