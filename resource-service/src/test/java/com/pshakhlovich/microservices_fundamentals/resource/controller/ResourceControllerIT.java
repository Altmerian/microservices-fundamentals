package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import software.amazon.awssdk.services.s3.S3Client;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ResourceControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ResourceController resourceController;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private S3Client s3Client;

  @MockBean private EventPublisher eventPublisher;

  private static final String RESOURCE_BASE_PATH = "/resources";
  private static final String TEST_MP3_FILE_PATH = "test_data/file_example_MP3_5MG.mp3";
  private static final String TEST_FILE_NAME = "file_example_MP3_5MG.mp3";
  private static final Integer RESOURCE_ID = 1;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(resourceController).build();
  }

  @Test
  void uploadResource() throws Exception {
    // given
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    var mockMultipartFile =
        new MockMultipartFile(
            "file",
            TEST_FILE_NAME,
            "audio/mpeg",
            classPathResource.getInputStream().readAllBytes());

    // when
    var response =
        mockMvc.perform(
            MockMvcRequestBuilders.multipart(RESOURCE_BASE_PATH).file(mockMultipartFile));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids").value(RESOURCE_ID));
  }
}
