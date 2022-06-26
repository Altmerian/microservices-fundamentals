package com.pshakhlovich.microservices_fundamentals.song.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "song_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SongMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Integer id;

  @Column private String name;
  @Column private String artist;
  @Column private String album;
  @Column private String length;
  @Column private Integer resourceId;
  @Column private Integer year;
}
