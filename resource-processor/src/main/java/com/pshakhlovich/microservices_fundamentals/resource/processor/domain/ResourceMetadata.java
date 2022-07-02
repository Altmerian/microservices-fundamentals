package com.pshakhlovich.microservices_fundamentals.resource.processor.domain;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetadata implements Serializable {

  private Integer id;
  private LocalDateTime creationTime;
  private String fileName;
  private Long sizeInBytes;
  private String fileExtension;
}
