package edu.ucsb.cs156.diningcachingproxy.services;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ucsb.cs156.diningcachingproxy.jobs.ResolveHostnameJob;
import edu.ucsb.cs156.jobs.entities.Job;
import edu.ucsb.cs156.jobs.services.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ResolveHostnameJobFactoryTests {

  @Test
  public void launch_builds_the_job_and_delegates_to_jobService() {
    HostTrackingService hostTrackingService = mock(HostTrackingService.class);
    JobService jobService = mock(JobService.class);
    Job expectedJob = Job.builder().build();
    when(jobService.runAsJob(any(ResolveHostnameJob.class))).thenReturn(expectedJob);

    ResolveHostnameJobFactory factory = new ResolveHostnameJobFactory();
    ReflectionTestUtils.setField(factory, "hostTrackingService", hostTrackingService);
    ReflectionTestUtils.setField(factory, "jobService", jobService);

    Job result = factory.launch("10.0.0.5");

    assertSame(expectedJob, result);
    verify(jobService, times(1)).runAsJob(any(ResolveHostnameJob.class));
  }
}
