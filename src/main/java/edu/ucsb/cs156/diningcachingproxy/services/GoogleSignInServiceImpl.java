package edu.ucsb.cs156.diningcachingproxy.services;

import edu.ucsb.cs156.diningcachingproxy.entity.User;
import edu.ucsb.cs156.diningcachingproxy.repository.AdminRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.HostManagerRepository;
import edu.ucsb.cs156.diningcachingproxy.repository.UserRepository;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class GoogleSignInServiceImpl extends OidcUserService implements GoogleSignInService {

  @Autowired private UserRepository userRepository;
  @Autowired private AdminRepository adminRepository;
  @Autowired private HostManagerRepository hostManagerRepository;

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    OidcUser oidcUser = super.loadUser(userRequest);
    return processSignIn(oidcUser);
  }

  public void signInUser(OidcUser oidcUser) {
    processSignIn(oidcUser);
  }

  private OidcUser processSignIn(OidcUser oidcUser) {
    Optional<User> existing = userRepository.findByEmail(oidcUser.getEmail());
    Set<GrantedAuthority> authorities = new HashSet<>();

    if (adminRepository.existsByEmail(oidcUser.getEmail())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    } else if (hostManagerRepository.existsByEmail(oidcUser.getEmail())) {
      authorities.add(new SimpleGrantedAuthority("ROLE_HOSTMANAGER"));
    } else {
      authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    String fullName = oidcUser.getFullName();
    String givenName = oidcUser.getGivenName();
    String familyName = oidcUser.getFamilyName();
    String pictureUrl = oidcUser.getPicture();

    if (existing.isPresent()) {
      User user = existing.get();
      boolean changed = false;
      if (!Objects.equals(fullName, user.getFullName())) {
        user.setFullName(fullName);
        changed = true;
      }
      if (!Objects.equals(givenName, user.getGivenName())) {
        user.setGivenName(givenName);
        changed = true;
      }
      if (!Objects.equals(familyName, user.getFamilyName())) {
        user.setFamilyName(familyName);
        changed = true;
      }
      if (!Objects.equals(pictureUrl, user.getPictureUrl())) {
        user.setPictureUrl(pictureUrl);
        changed = true;
      }
      if (changed) {
        userRepository.save(user);
      }
    } else {
      User newUser =
          User.builder()
              .googleSub(oidcUser.getSubject())
              .email(oidcUser.getEmail())
              .fullName(fullName)
              .givenName(givenName)
              .familyName(familyName)
              .pictureUrl(pictureUrl)
              .build();
      userRepository.save(newUser);
    }

    authorities.addAll(oidcUser.getAuthorities());
    return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
  }
}
