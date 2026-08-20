package edu.ucsb.cs156.diningcachingproxy.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiningCachingProxyApplicationRunner implements ApplicationRunner {

  @Autowired DiningCachingProxyStartup startup;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("DiningCachingProxyApplicationRunner.run called");
    startup.alwaysRunOnStartup();
  }
}
