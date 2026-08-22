package edu.ucsb.cs156.diningcachingproxy.services;

import edu.ucsb.cs156.diningcachingproxy.jobs.ResolveHostnameJob;
import edu.ucsb.cs156.jobs.entities.Job;
import edu.ucsb.cs156.jobs.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Builds and launches a {@link ResolveHostnameJob} for one IP address. Used both to auto-trigger
 * resolution the first time an address is seen, and to manually retry it on demand - a single call
 * site for both so there's exactly one place that knows how to build this job.
 */
@Service
public class ResolveHostnameJobFactory {

  @Autowired private HostTrackingService hostTrackingService;
  @Autowired private JobService jobService;

  public Job launch(String ipAddress) {
    ResolveHostnameJob job =
        ResolveHostnameJob.builder()
            .ipAddress(ipAddress)
            .hostTrackingService(hostTrackingService)
            .build();
    return jobService.runAsJob(job);
  }
}
