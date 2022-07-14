package com.pshakhlovich.microservices_fundamentals.resource.cucumber;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import com.pshakhlovich.microservices_fundamentals.resource.cucumber.config.TestConfig;
import com.pshakhlovich.microservices_fundamentals.resource.event.EventPublisher;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.AwsS3Client;
import com.pshakhlovich.microservices_fundamentals.resource.repository.ResourceRepository;
import com.pshakhlovich.microservices_fundamentals.resource.testcontainer.ContainerBase;
import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@CucumberContextConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TestConfig.class)
@ActiveProfiles("test")
public class CucumberSpringConfig extends ContainerBase {

  @MockBean
  private EventPublisher eventPublisher;

  @Autowired
  private Mp3BucketProperties mp3BucketProperties;

  @Autowired
  private ResourceRepository resourceRepository;

  AwsS3Client awsS3Client;

  @BeforeAll
  void setUp() {
    try (var s3Client =
        S3Client.builder()
            .endpointOverride(localstack.getEndpointOverride(S3))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        localstack.getAccessKey(), localstack.getSecretKey())))
            .region(Region.of(localstack.getRegion()))
            .build(); ) {
      awsS3Client = new AwsS3Client(mp3BucketProperties, s3Client);
      awsS3Client.createBucketIfNotExists();
    }
  }
}
