package edu.ucsb.cs156.diningcachingproxy.collections;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Mongo repository for the persisted cache performance statistics. */
@Repository
public interface StatsSnapshotRepository extends MongoRepository<StatsSnapshot, String> {}
