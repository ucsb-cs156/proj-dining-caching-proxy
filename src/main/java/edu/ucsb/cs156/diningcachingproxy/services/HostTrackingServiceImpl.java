package edu.ucsb.cs156.diningcachingproxy.services;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * In-memory implementation of {@link HostTrackingService}. Everything is keyed by the raw remote
 * address internally, since a hostname (if any) is only known once {@link #resolveHostname(String,
 * String)} is called - which may happen well after the first request from that address was already
 * counted, since resolution runs as an asynchronous job.
 */
@Service
public class HostTrackingServiceImpl extends HostTrackingService {

  private final Map<String, String> displayNames = new ConcurrentHashMap<>();
  private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();

  @Override
  public boolean recordRequest(String remoteAddress) {
    displayNames.putIfAbsent(remoteAddress, remoteAddress);
    AtomicLong counter = requestCounts.computeIfAbsent(remoteAddress, key -> new AtomicLong(0));
    return counter.getAndIncrement() == 0;
  }

  @Override
  public void resolveHostname(String remoteAddress, String hostname) {
    displayNames.put(remoteAddress, hostname);
  }

  @Override
  public List<HostCount> getHostCounts() {
    return requestCounts.entrySet().stream()
        .map(
            entry ->
                new HostCount(
                    displayNames.getOrDefault(entry.getKey(), entry.getKey()),
                    entry.getValue().get()))
        .sorted(Comparator.comparing(HostCount::host))
        .toList();
  }
}
