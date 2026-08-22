package edu.ucsb.cs156.diningcachingproxy.services;

import java.util.List;

/**
 * Tracks which hosts have sent requests through the proxy, and how many times each has done so.
 * Abstracted behind this base class so the storage mechanism (in-memory today) can later be swapped
 * for a persisted one (Mongo/SQL) without changing callers.
 *
 * <p>Every address is tracked under its own raw form (e.g. an IP address) until a hostname is
 * resolved for it - resolution happens out-of-band (see {@code ResolveHostnameJob}), since it can
 * be asynchronous.
 */
public abstract class HostTrackingService {

  /**
   * Records a single request from the given remote address (the caller's true IP, already resolved
   * from X-Forwarded-For where applicable).
   *
   * @return true if this is the first time this exact address has been seen
   */
  public abstract boolean recordRequest(String remoteAddress);

  /**
   * Updates the display name shown for a previously-seen address - called once a hostname
   * resolution attempt for it completes (successfully or not).
   */
  public abstract void resolveHostname(String remoteAddress, String hostname);

  /**
   * Every host seen so far with its request count, sorted by host (hostname, or IP if unresolved).
   */
  public abstract List<HostCount> getHostCounts();
}
