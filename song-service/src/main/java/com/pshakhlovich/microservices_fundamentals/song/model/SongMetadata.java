package com.pshakhlovich.microservices_fundamentals.song.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import lombok.*;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.Duration;

@Entity
@Table(name = "song_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@TypeDef(typeClass = PostgreSQLIntervalType.class, defaultForType = Duration.class)
public class SongMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Integer id;

  @Column private String name;
  @Column private String artist;
  @Column private String album;

  @Column(columnDefinition = "interval")
  private Duration length;

  @Column private Integer resourceId;
  @Column private Integer year;
}
