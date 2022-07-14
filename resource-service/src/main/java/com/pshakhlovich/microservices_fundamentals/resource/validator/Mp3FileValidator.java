package com.pshakhlovich.microservices_fundamentals.resource.validator;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static com.pshakhlovich.microservices_fundamentals.resource.util.Constants.AUDIO_CONTENT_TYPE;

public class Mp3FileValidator {

  private static final String INVALID_FILE_CONTENT_TYPE = "Invalid file content type";

  public void checkContentIsValidMp3File(MultipartFile multipartFile) {
    Objects.requireNonNull(multipartFile.getContentType(), INVALID_FILE_CONTENT_TYPE);
    if (!multipartFile.getContentType().equalsIgnoreCase(AUDIO_CONTENT_TYPE)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_FILE_CONTENT_TYPE);
    }
  }
}
