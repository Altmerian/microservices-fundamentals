package com.pshakhlovich.microservices_fundamentals.resource.processor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongMetadata {

  private Integer id;
  private String name;
  private String artist;
  private String album;
  private String length;
  private Integer resourceId;
  private Integer year;
}
