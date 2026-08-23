package edu.ucsb.cs156.diningcachingproxy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class HostTrackingServiceImplTests {

  @Test
  public void starts_with_no_hosts() {
    HostTrackingServiceImpl service = new HostTrackingServiceImpl();
    assertEquals(List.of(), service.getHostCounts());
  }

  @Test
  public void recordRequest_returns_true_only_the_first_time_an_address_is_seen() {
    HostTrackingServiceImpl service = new HostTrackingServiceImpl();

    assertTrue(service.recordRequest("10.0.0.5"));
    assertFalse(service.recordRequest("10.0.0.5"));
    assertFalse(service.recordRequest("10.0.0.5"));

    List<HostCount> counts = service.getHostCounts();
    assertEquals(1, counts.size());
    assertEquals("10.0.0.5", counts.get(0).host());
    assertEquals(3, counts.get(0).count());
  }

  @Test
  public void a_new_address_displays_as_itself_until_resolved() {
    HostTrackingServiceImpl service = new HostTrackingServiceImpl();

    service.recordRequest("10.0.0.5");

    List<HostCount> counts = service.getHostCounts();
    assertEquals(1, counts.size());
    assertEquals("10.0.0.5", counts.get(0).host());
  }

  @Test
  public void resolveHostname_updates_the_display_name_without_losing_the_count() {
    HostTrackingServiceImpl service = new HostTrackingServiceImpl();

    service.recordRequest("10.0.0.5");
    service.recordRequest("10.0.0.5");
    service.resolveHostname("10.0.0.5", "dining-qa.dokku-00.cs.ucsb.edu");
    service.recordRequest("10.0.0.5");

    List<HostCount> counts = service.getHostCounts();
    assertEquals(1, counts.size());
    assertEquals("dining-qa.dokku-00.cs.ucsb.edu", counts.get(0).host());
    assertEquals(3, counts.get(0).count());
  }

  @Test
  public void counts_are_sorted_by_host() {
    HostTrackingServiceImpl service = new HostTrackingServiceImpl();

    service.recordRequest("zzz.example.edu");
    service.recordRequest("aaa.example.edu");
    service.recordRequest("mmm.example.edu");

    List<String> hosts = service.getHostCounts().stream().map(HostCount::host).toList();
    assertEquals(List.of("aaa.example.edu", "mmm.example.edu", "zzz.example.edu"), hosts);
  }
}
