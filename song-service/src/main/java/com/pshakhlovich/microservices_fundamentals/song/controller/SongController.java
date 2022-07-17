package com.pshakhlovich.microservices_fundamentals.song.controller;

import com.pshakhlovich.microservices_fundamentals.song.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.song.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

  private final SongService songService;

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public SongMetadata getSongMetadata(@PathVariable Integer id) {
    return songService.fetchMetadata(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public IdWrapper<Integer> createSongMetadata(@RequestBody SongMetadata songMetadata) {
    return new IdWrapper<>(songService.createMetadata(songMetadata));
  }

  @DeleteMapping(value = "/{ids}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IdWrapper<List<Integer>>> deleteSongMetadata(
      @PathVariable @NotBlank @Size(max = 200) List<Integer> ids) {
    IdWrapper<List<Integer>> deletedIds = songService.delete(ids);
    if (ids.size() > deletedIds.ids().size()) {
      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
    return ResponseEntity.ok(deletedIds);
  }
}
