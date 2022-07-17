package com.pshakhlovich.microservices_fundamentals.resource.cucumber.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.cucumber.client.ResourceClient;
import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.repository.ResourceRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class StepDefinitions {

  private static final String IMPORT_FILES_BASE_PATH = "test_data/";

  private final ResourceClient resourceClient;

  private final ResourceRepository resourceRepository;

  private final ObjectMapper objectMapper;

  private MockMvcResponse response;

  private IdWrapper<?> appResponseBody;

  private final List<Integer> createdResourcesIds = new ArrayList<>();

  @When("User uploads file {string}")
  public void userUploadsFile(String filePath) {
    try (InputStream is = new ClassPathResource(IMPORT_FILES_BASE_PATH + filePath).getInputStream()) {
      response = resourceClient.uploadResource(is, FilenameUtils.getName(filePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
      appResponseBody = response.as(new TypeRef<IdWrapper<Integer>>() {});
      createdResourcesIds.add((int) appResponseBody.ids());
    }
  }

  @Then("Application response status is {int}")
  public void applicationResponseStatusIs(int responseStatus) {
    assertThat(response.getStatusCode()).isEqualTo(responseStatus);
  }

  @And("Response contains:")
  public void responseContains(String jsonResponse) throws JsonProcessingException {
    var expectedContent =
        objectMapper.readValue(jsonResponse, new TypeReference<IdWrapper<Integer>>() {});
    assertThat(appResponseBody).isEqualTo(expectedContent);
  }

  @And("There are the following resources")
  public void theFollowingResourceWasPersistedInTheSystem(List<ResourceMetadata> resources) {
    resources.forEach(
        resourceMetadata ->
            assertThat(resourceRepository.existsById(resourceMetadata.getId())).isTrue());
  }

  @Given("The following Resources exist in the system:")
  public void theFollowingResourceExistsInTheSystem(List<ResourceMetadata> resources) {
    resources.forEach(
        resourceMetadata -> {
          userUploadsFile(resourceMetadata.getFileName());
          assertThat(createdResourcesIds).contains(resourceMetadata.getId());
        });
  }

  @When("User downloads resource with id={int}")
  public void userDownloadsResourceWithId(int resourceId) {
    response = resourceClient.downloadResource(resourceId);
  }

  @And("Response content type is {string}")
  public void responseContentTypeIs(String contentType) {
    assertThat(response.getContentType()).isEqualTo(contentType);
  }

  @And("Response contains file with size in bytes {long}")
  public void responseContainsFile(long fileSize) {
    assertThat(response.asByteArray().length).isEqualTo(fileSize);
  }

  @When("User deletes resource with id={int}")
  public void userDeletesResourceWithId(int resourceId) {
    response = resourceClient.deleteResource(resourceId);
  }
}
