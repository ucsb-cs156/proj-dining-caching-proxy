package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.diningcachingproxy.ControllerTestCase;
import edu.ucsb.cs156.diningcachingproxy.services.DiningProxyService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = DiningCommonsController.class)
public class DiningCommonsControllerTests extends ControllerTestCase {

  @MockitoBean DiningProxyService diningProxyService;

  @Test
  public void anonymous_caller_can_use_the_proxy_and_headers_are_forwarded() throws Exception {
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
  public void works_without_any_headers() throws Exception {
    when(diningProxyService.proxyGet(eq("/dining/commons/v1/"), isNull(), isNull()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("[]"));

    mockMvc.perform(get("/dining/commons/v1/")).andExpect(status().isOk());

    verify(diningProxyService, times(1)).proxyGet("/dining/commons/v1/", null, null);
  }
}
