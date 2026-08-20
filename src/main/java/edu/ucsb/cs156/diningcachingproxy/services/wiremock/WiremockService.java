package edu.ucsb.cs156.diningcachingproxy.services.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

public abstract class WiremockService {
  public abstract WireMockServer getWiremockServer();

  public abstract void init();
}
