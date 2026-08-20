package edu.ucsb.cs156.diningcachingproxy.services;

import edu.ucsb.cs156.diningcachingproxy.entity.User;
import edu.ucsb.cs156.diningcachingproxy.model.CurrentUser;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public abstract class CurrentUserService {

  public abstract User getUser();

  public abstract CurrentUser getCurrentUser();

  public abstract Collection<? extends GrantedAuthority> getRoles();

  public final boolean isLoggedIn() {
    return getUser() != null;
  }
}
