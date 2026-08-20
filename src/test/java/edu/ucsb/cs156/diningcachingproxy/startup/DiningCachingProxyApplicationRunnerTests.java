package edu.ucsb.cs156.diningcachingproxy.startup;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

class DiningCachingProxyApplicationRunnerTests {

  private DiningCachingProxyApplicationRunner applicationRunner;

  @Mock private DiningCachingProxyStartup startup;
  @Mock private ApplicationArguments mockArgs;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    applicationRunner = new DiningCachingProxyApplicationRunner();
    applicationRunner.startup = startup;
  }

  @Test
  void run_calls_alwaysRunOnStartup() throws Exception {
    applicationRunner.run(mockArgs);
    verify(startup, times(1)).alwaysRunOnStartup();
  }
}
