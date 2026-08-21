package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.services.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTests extends ControllerTestCase {

  @MockitoBean StatsService statsService;

  @Test
  public void logged_out_users_cannot_get_stats() throws Exception {
    mockMvc.perform(get("/api/stats")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_stats() throws Exception {
    when(statsService.getTotalRequests()).thenReturn(10L);
    when(statsService.getCacheHits()).thenReturn(7L);
    when(statsService.getCacheMisses()).thenReturn(3L);
    when(statsService.getHitRatePercentage()).thenReturn(70.0);

    MvcResult response = mockMvc.perform(get("/api/stats")).andExpect(status().isOk()).andReturn();

    String expectedJson =
        mapper.writeValueAsString(new StatsController.StatsDTO(10L, 7L, 3L, 70.0));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admin_can_get_stats_via_role_hierarchy() throws Exception {
    mockMvc.perform(get("/api/stats")).andExpect(status().isOk());
  }
}
