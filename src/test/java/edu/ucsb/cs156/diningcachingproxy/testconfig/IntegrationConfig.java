package edu.ucsb.cs156.diningcachingproxy.testconfig;

import edu.ucsb.cs156.diningcachingproxy.config.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(SecurityConfig.class)
public class IntegrationConfig {}
