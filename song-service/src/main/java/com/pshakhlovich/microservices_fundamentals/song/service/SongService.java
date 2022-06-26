package com.pshakhlovich.microservices_fundamentals.song.service;

import com.pshakhlovich.microservices_fundamentals.song.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongService {

  private final SongRepository songRepository;

  public SongMetadata fetchMetadata(Integer id) {
    return songRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, String.format("Song with id=%d not found", id)));
  }

  public Integer createMetadata(SongMetadata songMetadata) {
    return songRepository.saveAndFlush(songMetadata).getId();
  }

  public IdWrapper<List<Integer>> delete(List<Integer> ids) {
    List<SongMetadata> songsToDelete = songRepository.findAllById(ids);
    if (songsToDelete.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "There are no songs with provided ids: " + ids);
    }
    var idsToDelete = songsToDelete.stream().map(SongMetadata::getId).collect(Collectors.toList());
    songRepository.deleteAllById(idsToDelete);
    return new IdWrapper<>(idsToDelete);
  }
}
