package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = DummyController.class)
public class DummyControllerTests extends ControllerTestCase {

  @Test
  @WithMockUser
  public void getById_returns_value_when_found() throws Exception {
    mockMvc
        .perform(get("/dummycontroller").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(content().string("String1"));
  }

  @Test
  @WithMockUser
  public void getById_returns_404_when_not_found() throws Exception {
    mockMvc
        .perform(get("/dummycontroller").param("id", "2"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("EntityNotFoundException"));
  }

  @Test
  @WithMockUser
  public void forbidden_returns_403() throws Exception {
    mockMvc
        .perform(get("/dummycontroller/forbidden"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.type").value("ForbiddenException"))
        .andExpect(jsonPath("$.message").value("nope"));
  }

  @Test
  @WithMockUser
  public void unsupported_returns_403() throws Exception {
    mockMvc
        .perform(get("/dummycontroller/unsupported"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("not supported"));
  }

  @Test
  @WithMockUser
  public void illegalArgument_returns_400() throws Exception {
    mockMvc
        .perform(get("/dummycontroller/illegalArgument"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("IllegalArgumentException"))
        .andExpect(jsonPath("$.message").value("bad argument"));
  }
}
