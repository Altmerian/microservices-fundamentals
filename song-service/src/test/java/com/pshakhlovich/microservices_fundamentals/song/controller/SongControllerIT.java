package com.pshakhlovich.microservices_fundamentals.song.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SongControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private SongController songController;

  @Autowired private ObjectMapper objectMapper;

  private static final String SONG_BASE_PATH = "/songs";
  private static final Integer SONG_ID = 1;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(songController).build();
  }

  @Test
  void createSongMetadata() throws Exception {
    // given
    var songMetadata =
        SongMetadata.builder()
            .name("Song name")
            .album("album")
            .artist("artist")
            .resourceId(1)
            .year(2022)
            .build();

    // when
    var response =
        mockMvc.perform(
            post(SONG_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(songMetadata)));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids").value(SONG_ID));
  }
}
