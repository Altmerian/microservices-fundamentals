package com.pshakhlovich.microservices_fundamentals.song.controller;

import com.pshakhlovich.microservices_fundamentals.song.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.song.model.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.song.service.SongService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SongController.class)
public class SongContractTestBase {

  private static final int SONG_ID = 1;

  @Autowired private SongController songController;

  @MockBean private SongService songService;

  @BeforeEach
  void setup() {
    RestAssuredMockMvc.standaloneSetup(songController);
    when(songService.createMetadata(getSong())).thenReturn(SONG_ID);
    when(songService.fetchMetadata(SONG_ID)).thenReturn(getSong());
    when(songService.delete(List.of(SONG_ID))).thenReturn(new IdWrapper<>(List.of(SONG_ID)));
  }

  private SongMetadata getSong() {
    return SongMetadata.builder()
        .id(SONG_ID)
        .album("News of the world")
        .artist("Queen")
        .name("We are the champions")
        .length("2:59")
        .resourceId(1)
        .year(1977)
        .build();
  }
}
