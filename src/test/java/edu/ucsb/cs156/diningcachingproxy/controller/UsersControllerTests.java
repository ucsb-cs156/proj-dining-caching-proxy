package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.entity.Admin;
import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import edu.ucsb.cs156.diningcachingproxy.entity.User;
import edu.ucsb.cs156.diningcachingproxy.model.UserDTO;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UsersController.class)
@TestPropertySource(properties = {"app.admin.emails=superadmin@example.org"})
public class UsersControllerTests extends ControllerTestCase {

  @MockitoBean UserRepository userRepository;
  @MockitoBean AdminRepository adminRepository;
  @MockitoBean HostManagerRepository hostManagerRepository;

  @Test
  public void users__logged_out() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void users__user_logged_in() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void users__admin_logged_in() throws Exception {
    // arrange
    User u1 = User.builder().id(1L).email("user1@example.org").build();
    User u2 = User.builder().id(2L).email("user2@example.org").build();

    when(userRepository.findAll()).thenReturn(List.of(u1, u2));
    when(adminRepository.existsByEmail(u1.getEmail())).thenReturn(false);
    when(hostManagerRepository.existsByEmail(u1.getEmail())).thenReturn(false);
    when(adminRepository.existsByEmail(u2.getEmail())).thenReturn(true);
    when(hostManagerRepository.existsByEmail(u2.getEmail())).thenReturn(false);

    List<UserDTO> expectedUserDTOs =
        List.of(new UserDTO(u1, false, false), new UserDTO(u2, true, false));
    String expectedJson = mapper.writeValueAsString(expectedUserDTOs);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk()).andReturn();

    // assert
    verify(userRepository, times(1)).findAll();
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_cannot_toggle_admin_of_nonexistent_user() throws Exception {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleAdmin").param("id", "1").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(userRepository, times(1)).findById(1L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("User with id 1 not found", json.get("message"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_can_add_user_to_admin_table() throws Exception {
    User user = User.builder().id(7L).email("user@example.org").build();
    Admin admin = new Admin("user@example.org");

    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    when(adminRepository.existsByEmail("user@example.org")).thenReturn(false, true);
    when(hostManagerRepository.existsByEmail("user@example.org")).thenReturn(false);

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleAdmin").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(userRepository, times(1)).findById(7L);
    verify(adminRepository, times(2)).existsByEmail("user@example.org");
    verify(adminRepository, times(1)).save(admin);
    String expectedJson = mapper.writeValueAsString(new UserDTO(user, true, false));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_can_remove_user_from_admin_table() throws Exception {
    User user = User.builder().id(7L).email("user@example.org").build();

    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    when(adminRepository.existsByEmail("user@example.org")).thenReturn(true, false);
    when(hostManagerRepository.existsByEmail("user@example.org")).thenReturn(false);

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleAdmin").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(userRepository, times(1)).findById(7L);
    verify(adminRepository, times(2)).existsByEmail("user@example.org");
    verify(adminRepository, times(1)).deleteById("user@example.org");
    String expectedJson = mapper.writeValueAsString(new UserDTO(user, false, false));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_cannot_toggle_admin_of_super_admin() throws Exception {
    User user = User.builder().id(7L).email("superadmin@example.org").build();

    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    when(hostManagerRepository.existsByEmail("superadmin@example.org")).thenReturn(false);

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleAdmin").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(userRepository, times(1)).findById(7L);
    String expectedJson = mapper.writeValueAsString(new UserDTO(user, true, false));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_cannot_toggle_hostmanager_of_nonexistent_user() throws Exception {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleHostManager").param("id", "1").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(userRepository, times(1)).findById(1L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("User with id 1 not found", json.get("message"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_can_add_user_to_hostmanager_table() throws Exception {
    User user = User.builder().id(7L).email("user@example.org").build();
    HostManager hostManager = new HostManager("user@example.org");

    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    when(adminRepository.existsByEmail("user@example.org")).thenReturn(false);
    when(hostManagerRepository.existsByEmail("user@example.org")).thenReturn(false, true);

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleHostManager").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(userRepository, times(1)).findById(7L);
    verify(hostManagerRepository, times(2)).existsByEmail("user@example.org");
    verify(hostManagerRepository, times(1)).save(hostManager);
    String expectedJson = mapper.writeValueAsString(new UserDTO(user, false, true));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void admin_can_remove_user_from_hostmanager_table() throws Exception {
    User user = User.builder().id(7L).email("user@example.org").build();

    when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    when(adminRepository.existsByEmail("user@example.org")).thenReturn(false);
    when(hostManagerRepository.existsByEmail("user@example.org")).thenReturn(true, false);

    MvcResult response =
        mockMvc
            .perform(put("/api/admin/toggleHostManager").param("id", "7").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(userRepository, times(1)).findById(7L);
    verify(hostManagerRepository, times(2)).existsByEmail("user@example.org");
    verify(hostManagerRepository, times(1)).deleteById("user@example.org");
    String expectedJson = mapper.writeValueAsString(new UserDTO(user, false, false));
    assertEquals(expectedJson, response.getResponse().getContentAsString());
  }
}
