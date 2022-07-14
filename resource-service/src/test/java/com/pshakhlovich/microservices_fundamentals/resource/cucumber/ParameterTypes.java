package com.pshakhlovich.microservices_fundamentals.resource.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import io.cucumber.java.DataTableType;
import io.cucumber.java.DefaultDataTableCellTransformer;
import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.DefaultParameterTransformer;

import java.lang.reflect.Type;
import java.util.Map;

import static com.pshakhlovich.microservices_fundamentals.resource.util.NullableValueSetter.checkNonNullAndApply;

public class ParameterTypes {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @DefaultParameterTransformer
  @DefaultDataTableEntryTransformer
  @DefaultDataTableCellTransformer
  public Object transformer(Object fromValue, Type toValueType) {
    return objectMapper.convertValue(fromValue, objectMapper.constructType(toValueType));
  }

  @DataTableType
  public ResourceMetadata resourceMetadataEntry(Map<String, String> entry) {
    return ResourceMetadata.builder()
        .id(Integer.valueOf(entry.get("id")))
        .fileName(entry.get("fileName"))
        .sizeInBytes(checkNonNullAndApply(entry.get("sizeInBytes"), Long::valueOf))
        .build();
  }
}
