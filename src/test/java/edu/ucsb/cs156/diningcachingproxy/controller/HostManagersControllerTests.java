package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HostManagersController.class)
public class HostManagersControllerTests extends ControllerTestCase {

  @MockitoBean HostManagerRepository hostManagerRepository;

  // Tests for the POST endpoint

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/admin/hostmanagers/post?email=test@ucsb.edu"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/admin/hostmanagers/post?email=test@ucsb.edu"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admins_can_post() throws Exception {
    HostManager hostManager = HostManager.builder().email("hm@ucsb.edu").build();
    when(hostManagerRepository.save(eq(hostManager))).thenReturn(hostManager);

    MvcResult response =
        mockMvc
            .perform(post("/api/admin/hostmanagers/post?email=hm@ucsb.edu").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(hostManagerRepository, times(1)).save(eq(hostManager));
    String expectedJson = mapper.writeValueAsString(hostManager);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admins_can_post_and_email_is_sanitized() throws Exception {
    HostManager hostManager = HostManager.builder().email("hm@ucsb.edu").build();
    when(hostManagerRepository.save(eq(hostManager))).thenReturn(hostManager);

    MvcResult response =
        mockMvc
            .perform(post("/api/admin/hostmanagers/post?email= hm@ucsb.edu ").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(hostManagerRepository, times(1)).save(eq(hostManager));
    String expectedJson = mapper.writeValueAsString(hostManager);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for the GET endpoint

  @Test
  public void logged_out_users_cannot_get() throws Exception {
    mockMvc.perform(get("/api/admin/hostmanagers/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_get() throws Exception {
    mockMvc.perform(get("/api/admin/hostmanagers/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admins_can_get() throws Exception {
    HostManager hostManager = HostManager.builder().email("hm@ucsb.edu").build();
    ArrayList<HostManager> expectedHostManagers = new ArrayList<>(Arrays.asList(hostManager));
    when(hostManagerRepository.findAll()).thenReturn(expectedHostManagers);

    MvcResult response =
        mockMvc.perform(get("/api/admin/hostmanagers/all")).andExpect(status().isOk()).andReturn();

    verify(hostManagerRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedHostManagers);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for the DELETE endpoint

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/admin/hostmanagers/delete").param("email", "test@ucsb.edu"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/admin/hostmanagers/delete").param("email", "test@ucsb.edu"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void logged_in_admins_can_delete() throws Exception {
    HostManager hostManager = HostManager.builder().email("hm@ucsb.edu").build();
    when(hostManagerRepository.findByEmail("hm@ucsb.edu")).thenReturn(Optional.of(hostManager));

    MvcResult response =
        mockMvc
            .perform(
                delete("/api/admin/hostmanagers/delete").param("email", "hm@ucsb.edu").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(hostManagerRepository, times(1)).findByEmail("hm@ucsb.edu");
    verify(hostManagerRepository, times(1)).delete(hostManager);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HostManager with id hm@ucsb.edu deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN"})
  @Test
  public void admin_tries_to_delete_a_hostmanager_not_found() throws Exception {
    String email = "nonexistent@ucsb.edu";
    when(hostManagerRepository.findByEmail(email)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/admin/hostmanagers/delete").param("email", email).with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(hostManagerRepository, times(1)).findByEmail(email);
    verify(hostManagerRepository, times(0)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("HostManager with id nonexistent@ucsb.edu not found", json.get("message"));
  }
}
