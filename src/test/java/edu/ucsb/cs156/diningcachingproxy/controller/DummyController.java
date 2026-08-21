package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.errors.EntityNotFoundException;
import edu.ucsb.cs156.diningcachingproxy.errors.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Used to test ApiController exception handlers and RoleUpdateInterceptor. */
@RequestMapping("/dummycontroller")
@RestController
public class DummyController extends ApiController {

  @GetMapping("")
  public String getById(@RequestParam Long id) {
    if (id == 1) return "String1";
    throw new EntityNotFoundException(String.class, id);
  }

  @GetMapping("/interceptorTest")
  public ResponseEntity<String> interceptorTest() {
    return ResponseEntity.ok("OK");
  }

  @GetMapping("/forbidden")
  public String forbidden() {
    throw new ForbiddenException("nope");
  }

  @GetMapping("/unsupported")
  public String unsupported() {
    throw new UnsupportedOperationException("not supported");
  }

  @GetMapping("/illegalArgument")
  public String illegalArgument() {
    throw new IllegalArgumentException("bad argument");
  }
}
