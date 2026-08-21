package edu.ucsb.cs156.diningcachingproxy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(controllers = UCSBDiningMenuController.class)
public class UCSBDiningMenuControllerTests extends ControllerTestCase {

  @MockitoBean DiningProxyService diningProxyService;

  @Test
  public void anonymous_caller_can_use_the_proxy_and_headers_are_forwarded() throws Exception {
    when(diningProxyService.proxyGet(
            eq("/dining/menu/v1/2024-08-16/carrillo"), eq("key123"), eq("1.0")))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body("{\"menu\":true}"));

    MvcResult response =
        mockMvc
            .perform(
                get("/dining/menu/v1/2024-08-16/carrillo")
                    .header("ucsb-api-key", "key123")
                    .header("ucsb-api-version", "1.0"))
            .andExpect(status().isOk())
            .andReturn();

    verify(diningProxyService, times(1))
        .proxyGet("/dining/menu/v1/2024-08-16/carrillo", "key123", "1.0");
    assertEquals("{\"menu\":true}", response.getResponse().getContentAsString());
  }
}
