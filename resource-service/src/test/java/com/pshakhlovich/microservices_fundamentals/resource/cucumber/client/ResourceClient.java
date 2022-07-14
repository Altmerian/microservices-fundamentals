package com.pshakhlovich.microservices_fundamentals.resource.cucumber.client;

import com.pshakhlovich.microservices_fundamentals.resource.controller.ResourceController;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;

import static com.pshakhlovich.microservices_fundamentals.resource.util.Constants.AUDIO_CONTENT_TYPE;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResourceClient {

  private static final String RESOURCES_BASE_PATH = "/resources";

  @Autowired private ResourceService resourceService;

  @Autowired private Environment environment;

  @PostConstruct
  private void init() {
    RestAssuredMockMvc.standaloneSetup(new ResourceController(resourceService, environment));
  }

  public MockMvcResponse uploadResource(InputStream inputStream, String fileName) {
    return given()
        .multiPart("file", fileName, inputStream, AUDIO_CONTENT_TYPE)
        .post(RESOURCES_BASE_PATH);
  }

  public MockMvcResponse downloadResource(int resourceId) {
    return given().get(RESOURCES_BASE_PATH + "/{id}", resourceId);
  }
}
