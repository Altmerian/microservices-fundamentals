package com.pshakhlovich.microservices_fundamentals.resource.validator;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

public class Mp3FileValidator {

  public void checkContentIsValidMp3File(MultipartFile multipartFile) {
    Objects.requireNonNull(multipartFile.getContentType(), "Wrong content type");
    if (!multipartFile.getContentType().equalsIgnoreCase("audio/mpeg")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong file content type. 'audio/mpeg' is only acceptable");
    }
  }
}
