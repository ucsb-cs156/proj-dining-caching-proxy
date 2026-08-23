package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import edu.ucsb.cs156.diningcachingproxy.services.HostTrackingService;
import edu.ucsb.cs156.diningcachingproxy.services.ResolveHostnameJobFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = DiningProxyController.class)
public class DiningProxyControllerTests extends ControllerTestCase {

  @MockitoBean DiningProxyService diningProxyService;
  @MockitoBean HostTrackingService hostTrackingService;
  @MockitoBean ResolveHostnameJobFactory resolveHostnameJobFactory;

  @Test
  public void proxies_the_dining_commons_list_endpoint() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), eq("key123"), eq("1.0")))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[\"DLG\"]"));

    MvcResult response =
        mockMvc
            .perform(
                get("/dining/commons/v1/")
                    .header("ucsb-api-key", "key123")
                    .header("ucsb-api-version", "1.0"))
            .andExpect(status().isOk())
            .andReturn();

    verify(diningProxyService, times(1)).proxyGet("/dining/commons/v1/", "key123", "1.0");
    assertEquals("[\"DLG\"]", response.getResponse().getContentAsString());
  }

  @Test
  public void proxies_a_two_segment_menu_path_with_no_code_change_needed() throws Exception {
    when(diningProxyService.proxyGet(
            eq("/dining/menu/v1/2024-08-16/carrillo"), eq("key123"), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("{\"menu\":true}"));

    MvcResult response =
        mockMvc
            .perform(get("/dining/menu/v1/2024-08-16/carrillo").header("ucsb-api-key", "key123"))
            .andExpect(status().isOk())
            .andReturn();

    verify(diningProxyService, times(1))
        .proxyGet("/dining/menu/v1/2024-08-16/carrillo", "key123", null);
    assertEquals("{\"menu\":true}", response.getResponse().getContentAsString());
  }

  @Test
  public void proxies_a_three_segment_menu_items_path_with_no_code_change_needed()
      throws Exception {
    when(diningProxyService.proxyGet(
            eq("/dining/menu/v1/2024-08-16/carrillo/lunch"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[{\"name\":\"Entree\"}]"));

    MvcResult response =
        mockMvc
            .perform(get("/dining/menu/v1/2024-08-16/carrillo/lunch"))
            .andExpect(status().isOk())
            .andReturn();

    verify(diningProxyService, times(1))
        .proxyGet("/dining/menu/v1/2024-08-16/carrillo/lunch", null, null);
    assertEquals("[{\"name\":\"Entree\"}]", response.getResponse().getContentAsString());
  }

  @Test
  public void proxies_a_hypothetical_future_endpoint_never_seen_before() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/allergens/v2/carrillo"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc.perform(get("/dining/allergens/v2/carrillo")).andExpect(status().isOk());

    verify(diningProxyService, times(1)).proxyGet("/dining/allergens/v2/carrillo", null, null);
  }

  @Test
  public void appends_the_query_string_to_the_request_path_when_present() throws Exception {
    when(diningProxyService.proxyGet(
            eq("/dining/commons/v1/?includeClosed=true"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc
        .perform(get("/dining/commons/v1/").queryParam("includeClosed", "true"))
        .andExpect(status().isOk());

    verify(diningProxyService, times(1))
        .proxyGet("/dining/commons/v1/?includeClosed=true", null, null);
  }

  @Test
  public void records_the_first_hop_of_a_multi_hop_x_forwarded_for_header() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc
        .perform(get("/dining/commons/v1/").header("X-Forwarded-For", "203.0.113.5, 10.0.0.1"))
        .andExpect(status().isOk());

    verify(hostTrackingService, times(1)).recordRequest("203.0.113.5");
  }

  @Test
  public void falls_back_to_the_remote_address_when_there_is_no_x_forwarded_for_header()
      throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc.perform(get("/dining/commons/v1/")).andExpect(status().isOk());

    verify(hostTrackingService, times(1)).recordRequest("127.0.0.1");
  }

  @Test
  public void falls_back_to_the_remote_address_when_x_forwarded_for_is_blank() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc
        .perform(get("/dining/commons/v1/").header("X-Forwarded-For", "   "))
        .andExpect(status().isOk());

    verify(hostTrackingService, times(1)).recordRequest("127.0.0.1");
  }

  @Test
  public void launches_a_resolution_job_when_the_address_is_new() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));
    when(hostTrackingService.recordRequest("203.0.113.5")).thenReturn(true);

    mockMvc
        .perform(get("/dining/commons/v1/").header("X-Forwarded-For", "203.0.113.5"))
        .andExpect(status().isOk());

    verify(resolveHostnameJobFactory, times(1)).launch("203.0.113.5");
  }

  @Test
  public void does_not_launch_a_resolution_job_when_the_address_has_been_seen_before()
      throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));
    when(hostTrackingService.recordRequest("203.0.113.5")).thenReturn(false);

    mockMvc
        .perform(get("/dining/commons/v1/").header("X-Forwarded-For", "203.0.113.5"))
        .andExpect(status().isOk());

    verify(resolveHostnameJobFactory, times(0)).launch("203.0.113.5");
  }

  @Test
  public void tracks_by_the_self_reported_app_name_when_present() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));
    when(hostTrackingService.recordRequest("dining-qa.dokku-00.cs.ucsb.edu")).thenReturn(true);

    mockMvc
        .perform(
            get("/dining/commons/v1/")
                .header("X-Forwarded-For", "203.0.113.5")
                .header("X-Requesting-App", "dining-qa.dokku-00.cs.ucsb.edu"))
        .andExpect(status().isOk());

    verify(hostTrackingService, times(1)).recordRequest("dining-qa.dokku-00.cs.ucsb.edu");
  }

  @Test
  public void does_not_launch_a_resolution_job_for_a_self_reported_app_name() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));
    when(hostTrackingService.recordRequest("dining-qa.dokku-00.cs.ucsb.edu")).thenReturn(true);

    mockMvc
        .perform(
            get("/dining/commons/v1/")
                .header("X-Forwarded-For", "203.0.113.5")
                .header("X-Requesting-App", "dining-qa.dokku-00.cs.ucsb.edu"))
        .andExpect(status().isOk());

    verify(resolveHostnameJobFactory, times(0)).launch(anyString());
  }

  @Test
  public void falls_back_to_the_address_when_the_requesting_app_header_is_blank() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc
        .perform(
            get("/dining/commons/v1/")
                .header("X-Forwarded-For", "203.0.113.5")
                .header("X-Requesting-App", "   "))
        .andExpect(status().isOk());

    verify(hostTrackingService, times(1)).recordRequest("203.0.113.5");
  }
}
