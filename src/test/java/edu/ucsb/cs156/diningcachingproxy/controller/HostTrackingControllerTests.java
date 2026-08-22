package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.services.HostCount;
import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.diningcachingproxy.services.ResolveHostnameJobFactory;
import edu.ucsb.cs156.jobs.entities.Job;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HostTrackingController.class)
public class HostTrackingControllerTests extends ControllerTestCase {

  @MockitoBean HostTrackingService hostTrackingService;
  @MockitoBean ResolveHostnameJobFactory resolveHostnameJobFactory;

  @Test
  public void logged_out_users_cannot_get_hosts() throws Exception {
    mockMvc.perform(get("/api/admin/hosts")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_get_hosts() throws Exception {
    mockMvc.perform(get("/api/admin/hosts")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_list_hosts_with_counts() throws Exception {
    List<HostCount> hostCounts =
        List.of(new HostCount("dining-qa.dokku-00.cs.ucsb.edu", 42), new HostCount("10.0.0.5", 3));
    when(hostTrackingService.getHostCounts()).thenReturn(hostCounts);

    MvcResult response =
        mockMvc.perform(get("/api/admin/hosts")).andExpect(status().isOk()).andReturn();

    String expectedJson = mapper.writeValueAsString(hostCounts);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_users_cannot_retry_resolution() throws Exception {
    mockMvc
        .perform(post("/api/admin/hosts/resolve?ip=10.0.0.5").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_retry_resolution() throws Exception {
    mockMvc
        .perform(post("/api/admin/hosts/resolve?ip=10.0.0.5").with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_can_retry_resolution_for_an_ip() throws Exception {
    Job launchedJob = Job.builder().id(7).build();
    when(resolveHostnameJobFactory.launch(eq("10.0.0.5"))).thenReturn(launchedJob);

    MvcResult response =
        mockMvc
            .perform(post("/api/admin/hosts/resolve?ip=10.0.0.5").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(resolveHostnameJobFactory, times(1)).launch("10.0.0.5");
    String expectedJson = mapper.writeValueAsString(launchedJob);
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }
}
