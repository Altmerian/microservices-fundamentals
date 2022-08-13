package com.pshakhlovich.microservices_fundamentals.storage.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "storage_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StorageMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Integer id;

  @Column private StorageType storageType;
  @Column private String bucket;
  @Column private String path;
}
