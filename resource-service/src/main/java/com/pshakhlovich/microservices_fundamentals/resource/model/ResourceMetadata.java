package com.pshakhlovich.microservices_fundamentals.resource.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ResourceMetadata {

  @Id
  @GeneratedValue(generator = "resource_metadata_seq")
  @SequenceGenerator(
          name = "resource_metadata_seq",
          sequenceName = "resource_metadata_seq",
          allocationSize = 1)
  @Column(nullable = false)
  private Integer id;

  @Column private LocalDateTime creationTime;

  @Column(unique = true)
  private String fileName;

  @Column private Long sizeInBytes;

  @Column private String fileExtension;
}
