package com.pshakhlovich.microservices_fundamentals.resource.infrastructure;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = Mp3BucketProperties.class)
@EnableConfigurationProperties(Mp3BucketProperties.class)
public class AwsS3ClientIT {

  private static final String TEST_MP3_FILE_PATH = "test_data/file_example_MP3_5MG.mp3";
  private static final String TEST_FILE_NAME = "file_example_MP3_5MG.mp3";

  @Container
  private static final LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.14.5"))
          .withServices(S3);

  private static S3Client s3Client;

  private AwsS3Client awsS3Client;

  @Autowired Mp3BucketProperties mp3BucketProperties;

  @BeforeAll
  static void beforeAll() {
    s3Client =
        S3Client.builder()
            .endpointOverride(localstack.getEndpointOverride(S3))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        localstack.getAccessKey(), localstack.getSecretKey())))
            .region(Region.of(localstack.getRegion()))
            .build();
  }

  @AfterAll
  static void tearDown() {
    s3Client.close();
  }

  @BeforeEach
  void setUp() {
    awsS3Client = new AwsS3Client(mp3BucketProperties, s3Client);
    awsS3Client.createBucketIfNotExists();
  }

  @Test
  void uploadToS3Test() throws IOException {
    // given
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    var mockMultipartFile =
        new MockMultipartFile(
            "file",
            TEST_FILE_NAME,
            "audio/mpeg",
            classPathResource.getInputStream().readAllBytes());

    // when
    awsS3Client.uploadFile(mockMultipartFile);

    // then
    var getObjectRequest =
        GetObjectRequest.builder()
            .key(TEST_FILE_NAME)
            .bucket(mp3BucketProperties.getBucketName())
            .build();
    int actualFileSize = s3Client.getObject(getObjectRequest).readAllBytes().length;
    assertThat(actualFileSize).isEqualTo(mockMultipartFile.getSize());
  }

  @Test
  void downloadFromS3Test() throws IOException {
    // given
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    var mockMultipartFile =
            new MockMultipartFile(
                    "file",
                    TEST_FILE_NAME,
                    "audio/mpeg",
                    classPathResource.getInputStream().readAllBytes());

    s3Client.putObject(
            PutObjectRequest.builder()
                    .bucket(mp3BucketProperties.getBucketName())
                    .key(TEST_FILE_NAME)
                    .build(),
            RequestBody.fromInputStream(mockMultipartFile.getInputStream(), mockMultipartFile.getSize()));

    // when
    byte[] actualFile = awsS3Client.downloadFile(TEST_FILE_NAME);

    // then
    assertThat(actualFile.length).isEqualTo(mockMultipartFile.getSize());
  }

  @Test
  void deleteFromS3Test() throws IOException {
    // given
    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    var mockMultipartFile =
            new MockMultipartFile(
                    "file",
                    TEST_FILE_NAME,
                    "audio/mpeg",
                    classPathResource.getInputStream().readAllBytes());

    s3Client.putObject(
            PutObjectRequest.builder()
                    .bucket(mp3BucketProperties.getBucketName())
                    .key(TEST_FILE_NAME)
                    .build(),
            RequestBody.fromInputStream(mockMultipartFile.getInputStream(), mockMultipartFile.getSize()));

    // when
    List<String> removedObjectKeys = awsS3Client.removeFiles(Set.of(TEST_FILE_NAME));

    // then
    assertThat(removedObjectKeys).containsExactly(TEST_FILE_NAME);
  }
}
