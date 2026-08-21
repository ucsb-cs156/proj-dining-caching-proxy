package edu.ucsb.cs156.diningcachingproxy.collections;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Mongo repository ("collection") for {@link CachedResponse} documents. */
@Repository
public interface CachedResponseRepository extends MongoRepository<CachedResponse, String> {
  Optional<CachedResponse> findByRequestPath(String requestPath);
}
