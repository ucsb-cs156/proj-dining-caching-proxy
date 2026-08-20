package edu.ucsb.cs156.diningcachingproxy.model;

import edu.ucsb.cs156.diningcachingproxy.entity.User;

/** Response model for user data with computed role flags. */
public record UserDTO(
    long id,
    String email,
    String googleSub,
    String pictureUrl,
    String fullName,
    String givenName,
    String familyName,
    boolean admin,
    boolean hostManager) {

  public UserDTO(User user, boolean admin, boolean hostManager) {
    this(
        user.getId(),
        user.getEmail(),
        user.getGoogleSub(),
        user.getPictureUrl(),
        user.getFullName(),
        user.getGivenName(),
        user.getFamilyName(),
        admin,
        hostManager);
  }
}
