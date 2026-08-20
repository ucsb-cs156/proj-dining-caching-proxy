package edu.ucsb.cs156.diningcachingproxy;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import edu.ucsb.cs156.diningcachingproxy.entity.Admin;
import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import edu.ucsb.cs156.diningcachingproxy.entity.User;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.UserRepository;
import edu.ucsb.cs156.diningcachingproxy.services.wiremock.WiremockServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.extension.jwt.JwtExtensionFactory;

@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class WebTestCase {

  @Autowired UserRepository userRepository;
  @Autowired AdminRepository adminRepository;
  @Autowired HostManagerRepository hostManagerRepository;

  @LocalServerPort private int port;

  @Value("${app.playwright.headless:true}")
  private boolean runHeadless;

  private static WireMockServer wireMockServer;

  protected Browser browser;
  protected Page page;

  @BeforeAll
  public static void setupWireMock() {
    wireMockServer =
        new WireMockServer(
            options().port(8090).globalTemplating(true).extensions(new JwtExtensionFactory()));
    WiremockServiceImpl.setupOauthMocks(wireMockServer, "cgaucho@ucsb.edu");
    wireMockServer.start();
  }

  @AfterAll
  public static void teardownWiremock() {
    wireMockServer.stop();
  }

  @AfterEach
  public void teardown() {
    browser.close();
  }

  public void setupRegularUser() {
    setupUser(false, false);
  }

  public void setupAdminUser() {
    setupUser(true, false);
  }

  public void setupHostManagerUser() {
    setupUser(false, true);
  }

  @SuppressWarnings("null")
  private void setupUser(boolean isAdmin, boolean isHostManager) {
    String email = isHostManager ? "hostmanager@ucsb.edu" : "cgaucho@ucsb.edu";
    email = isAdmin ? "admin@ucsb.edu" : email;

    WiremockServiceImpl.setupOauthMocks(wireMockServer, email);

    User user =
        User.builder()
            .email(email)
            .familyName("Gaucho")
            .givenName("Chris")
            .fullName("Chris Gaucho")
            .googleSub("123456789")
            .pictureUrl("")
            .build();

    userRepository.save(user);
    if (isHostManager) {
      hostManagerRepository.save(HostManager.builder().email(email).build());
    }
    if (isAdmin) {
      adminRepository.save(Admin.builder().email(email).build());
    }

    browser =
        Playwright.create()
            .chromium()
            .launch(new BrowserType.LaunchOptions().setHeadless(runHeadless));

    BrowserContext context = browser.newContext();
    page = context.newPage();

    String url = String.format("http://localhost:%d/oauth2/authorization/my-oauth-provider", port);
    page.navigate(url);

    page.locator("#username").fill(email);
    page.locator("#password").fill("password");
    page.locator("#submit").click();
  }
}
