package edu.ucsb.cs156.diningcachingproxy.startup;

import static org.mockito.Mockito.*;

import edu.ucsb.cs156.diningcachingproxy.entity.Admin;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DiningCachingProxyStartupTests {

  @Mock private AdminRepository adminRepository;

  private DiningCachingProxyStartup startup;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    startup = new DiningCachingProxyStartup();
    startup.adminRepository = adminRepository;
    startup.adminEmails = List.of("phtcon@ucsb.edu", "admin2@ucsb.edu");
  }

  @Test
  void alwaysRunOnStartup_saves_all_admin_emails() {
    startup.alwaysRunOnStartup();

    verify(adminRepository).save(new Admin("phtcon@ucsb.edu"));
    verify(adminRepository).save(new Admin("admin2@ucsb.edu"));
    verifyNoMoreInteractions(adminRepository);
  }

  @Test
  void alwaysRunOnStartup_handles_repository_exception_gracefully() {
    doThrow(new RuntimeException("DB error")).when(adminRepository).save(any(Admin.class));

    // Should not throw — exception is caught and logged
    startup.alwaysRunOnStartup();

    verify(adminRepository, times(1)).save(any(Admin.class));
  }
}
