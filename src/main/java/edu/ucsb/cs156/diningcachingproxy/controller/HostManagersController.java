package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import edu.ucsb.cs156.diningcachingproxy.errors.EntityNotFoundException;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import edu.ucsb.cs156.diningcachingproxy.utilities.CanonicalFormConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HostManagers")
@RequestMapping("/api/admin/hostmanagers")
@RestController
@Slf4j
public class HostManagersController extends ApiController {

  @Autowired HostManagerRepository hostManagerRepository;

  @Operation(summary = "Create a new HostManager")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HostManager postHostManager(@Parameter(name = "email") @RequestParam String email) {
    String convertedEmail = CanonicalFormConverter.convertToValidEmail(email).strip();
    HostManager hostManager = new HostManager(convertedEmail);
    return hostManagerRepository.save(hostManager);
  }

  @Operation(summary = "List all HostManagers")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/all")
  public Iterable<HostManager> allHostManagers() {
    return hostManagerRepository.findAll();
  }

  @Operation(summary = "Delete a HostManager")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("/delete")
  public Object deleteHostManager(@Parameter(name = "email") @RequestParam String email) {
    HostManager hostManager =
        hostManagerRepository
            .findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(HostManager.class, email));
    hostManagerRepository.delete(hostManager);
    return genericMessage("HostManager with id %s deleted".formatted(email));
  }
}
