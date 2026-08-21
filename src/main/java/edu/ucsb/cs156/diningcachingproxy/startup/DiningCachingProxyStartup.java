package edu.ucsb.cs156.diningcachingproxy.startup;

import edu.ucsb.cs156.diningcachingproxy.entity.Admin;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiningCachingProxyStartup {

  @Value("#{'${app.admin.emails}'.split(',')}")
  List<String> adminEmails;

  @Autowired AdminRepository adminRepository;

  public void alwaysRunOnStartup() {
    log.info("DiningCachingProxyStartup.alwaysRunOnStartup called");

    try {
      adminEmails.forEach(
          (email) -> {
            Admin admin = new Admin(email.strip());
            adminRepository.save(admin);
          });
    } catch (Exception e) {
      log.error("Error loading ADMIN_EMAILS into admins table:", e);
    }
  }
}
