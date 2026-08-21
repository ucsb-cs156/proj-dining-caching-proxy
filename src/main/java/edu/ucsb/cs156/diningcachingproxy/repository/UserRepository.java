package edu.ucsb.cs156.diningcachingproxy.repository;

import edu.ucsb.cs156.diningcachingproxy.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
