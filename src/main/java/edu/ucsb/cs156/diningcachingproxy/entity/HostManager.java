package edu.ucsb.cs156.diningcachingproxy.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name = "hostmanagers")
public class HostManager {
  @Id private String email;
}
