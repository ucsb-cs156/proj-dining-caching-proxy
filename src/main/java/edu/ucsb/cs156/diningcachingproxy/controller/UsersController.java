package edu.ucsb.cs156.diningcachingproxy.controller;

import edu.ucsb.cs156.diningcachingproxy.entity.Admin;
import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import edu.ucsb.cs156.diningcachingproxy.entity.User;
import edu.ucsb.cs156.diningcachingproxy.errors.EntityNotFoundException;
import edu.ucsb.cs156.diningcachingproxy.model.UserDTO;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for listing users and toggling their Admin/HostManager status.
 *
 * <p>These endpoints are only accessible to users with the role "ROLE_ADMIN".
 */
@Tag(name = "User information (admin only)")
@RequestMapping("/api")
@RestController
public class UsersController extends ApiController {

  @Value("#{'${app.admin.emails}'.split(',')}")
  private final List<String> adminEmails = new ArrayList<>();

  @Autowired UserRepository userRepository;

  @Autowired AdminRepository adminRepository;

  @Autowired HostManagerRepository hostManagerRepository;

  @Operation(summary = "Get a list of all users")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @GetMapping("/admin/users")
  public List<UserDTO> users() {
    Iterable<User> users = userRepository.findAll();
    return StreamSupport.stream(users.spliterator(), false).map(this::userDTO).toList();
  }

  /**
   * Toggles whether a user's email appears in the admins table. Will not toggle emails baked into
   * ADMIN_EMAILS, since those are always admins regardless of the table's contents.
   */
  @Operation(summary = "Toggle whether a user is an admin")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("/admin/toggleAdmin")
  public UserDTO toggleAdminStatus(@RequestParam long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));

    if (!adminEmails.contains(user.getEmail())) {
      boolean isAdmin = adminRepository.existsByEmail(user.getEmail());
      if (isAdmin) {
        adminRepository.deleteById(user.getEmail());
      } else {
        adminRepository.save(new Admin(user.getEmail()));
      }
    }

    return userDTO(user);
  }

  @Operation(summary = "Toggle whether a user is a host manager")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("/admin/toggleHostManager")
  public UserDTO toggleHostManagerStatus(@RequestParam long id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(User.class, id));

    boolean isHostManager = hostManagerRepository.existsByEmail(user.getEmail());
    if (isHostManager) {
      hostManagerRepository.deleteById(user.getEmail());
    } else {
      hostManagerRepository.save(new HostManager(user.getEmail()));
    }

    return userDTO(user);
  }

  private UserDTO userDTO(User user) {
    String email = user.getEmail();
    boolean isAdmin = adminEmails.contains(email) || adminRepository.existsByEmail(email);
    boolean isHostManager = hostManagerRepository.existsByEmail(email);
    return new UserDTO(user, isAdmin, isHostManager);
  }
}
