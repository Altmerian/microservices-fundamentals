package com.pshakhlovich.microservices_fundamentals.song.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.song.service.SongService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SongControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private SongController songController;

  @Autowired private SongService songService;

  @Autowired private ObjectMapper objectMapper;

  private static final String SONG_NAME = "Song name";
  private static final String SONG_BASE_PATH = "/songs";

  private final AtomicInteger nextId = new AtomicInteger(1);

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(songController).build();
  }

  @AfterEach
  void tearDown() {
    nextId.incrementAndGet();
  }

  @Test
  void createSongMetadata() throws Exception {
    // given
    SongMetadata songMetadata = getTestSongMetadata();

    // when
    var response =
        mockMvc.perform(
            post(SONG_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(songMetadata)));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids").value(nextId.get()));
  }

  @Test
  void getSongMetadata() throws Exception {
    // given
    SongMetadata songMetadata = getTestSongMetadata();
    Integer metadataId = songService.createMetadata(songMetadata);

    // when
    var response = mockMvc.perform(get(SONG_BASE_PATH + "/{id}", metadataId));

    // then
    response
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(metadataId))
        .andExpect(jsonPath("$.name").value(SONG_NAME));
  }

  @Test
  void deleteSongMetadata() throws Exception {
    // given
    SongMetadata songMetadata = getTestSongMetadata();
    Integer metadataId = songService.createMetadata(songMetadata);

    // when
    var response = mockMvc.perform(delete(SONG_BASE_PATH + "/{ids}", metadataId));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids.[0]").value(metadataId));
  }

  private SongMetadata getTestSongMetadata() {
    return SongMetadata.builder()
        .name(SONG_NAME)
        .album("album")
        .artist("artist")
        .resourceId(1)
        .year(2022)
        .build();
  }
}
