package com.pshakhlovich.microservices_fundamentals.song.repository;

import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<SongMetadata, Integer> {}
