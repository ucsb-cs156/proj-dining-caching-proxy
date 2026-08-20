package edu.ucsb.cs156.diningcachingproxy.repository;

import edu.ucsb.cs156.diningcachingproxy.entity.HostManager;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostManagerRepository extends CrudRepository<HostManager, String> {
  Optional<HostManager> findByEmail(String email);

  boolean existsByEmail(String email);
}
