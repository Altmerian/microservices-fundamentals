package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.event.EventPublisher;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.AwsS3Client;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private ResourceController resourceController;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ResourceService resourceService;

  @Autowired private JdbcTemplate jdbcTemplate;

  @MockBean private AwsS3Client awsS3Client;

  @MockBean private EventPublisher eventPublisher;

  private static final String RESOURCE_BASE_PATH = "/resources";
  private static final String TEST_MP3_FILE_PATH = "test_data/file_example_MP3_5MG.mp3";
  private static final String TEST_FILE_NAME = "file_example_MP3_5MG.mp3";

  private final AtomicInteger nextId = new AtomicInteger(0);

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(resourceController).build();
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_metadata");
  }

  @AfterEach
  void tearDown() {
    nextId.incrementAndGet();
  }

  @Test
  void uploadResource() throws Exception {
    // given
    var mockMultipartFile = getMockMultipartFile();

    // when
    var response =
        mockMvc.perform(
            MockMvcRequestBuilders.multipart(RESOURCE_BASE_PATH).file(mockMultipartFile));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids").value(nextId.get()));
  }

  @Test
  void downloadResource() throws Exception {
    // given
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    byte[] fileContent = classPathResource.getInputStream().readAllBytes();
    var mockMultipartFile =
        new MockMultipartFile("file", TEST_FILE_NAME, "audio/mpeg", fileContent);

    Integer resourceId = resourceService.upload(mockMultipartFile);
    when(awsS3Client.downloadFile(TEST_FILE_NAME)).thenReturn(fileContent);

    // when
    var response = mockMvc.perform(get(RESOURCE_BASE_PATH + "/{id}", resourceId));

    // then
    var mvcResult =
        response
            .andExpect(status().isOk())
            .andExpect(content().contentType("audio/mpeg"))
            .andReturn();

    assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(fileContent);
    nextId.getAndIncrement();
  }

  @Test
  void deleteResource() throws Exception {
    // given
    var mockMultipartFile = getMockMultipartFile();

    Integer resourceId = resourceService.upload(mockMultipartFile);
    when(awsS3Client.removeFiles(Set.of(TEST_FILE_NAME))).thenReturn(List.of(TEST_FILE_NAME));

    // when
    var response = mockMvc.perform(delete(RESOURCE_BASE_PATH + "/{ids}", resourceId));

    // then
    response.andExpect(status().isOk()).andExpect(jsonPath("$.ids").value(resourceId));
  }

  private MockMultipartFile getMockMultipartFile() throws IOException {
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    return new MockMultipartFile(
            "file",
            TEST_FILE_NAME,
            "audio/mpeg",
            classPathResource.getInputStream().readAllBytes());
  }
}
