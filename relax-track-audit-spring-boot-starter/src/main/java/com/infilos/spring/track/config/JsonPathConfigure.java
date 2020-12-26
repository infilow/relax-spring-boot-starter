package com.infilos.spring.track.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

@Configuration
public class JsonPathConfigure {

  @Autowired(required = false)
  private Optional<ObjectMapper> objectMapper;

  /** Setup json-path configuration with the same jackson mapper with spring binded. */
  @PostConstruct
  public void setupJsonPathMapper() {
    objectMapper.ifPresent(
        mapper -> {
          com.jayway.jsonpath.Configuration.setDefaults(
              new com.jayway.jsonpath.Configuration.Defaults() {

                @Override
                public JsonProvider jsonProvider() {
                  // return new JacksonJsonProvider(mapper);
                  return new JacksonJsonNodeJsonProvider(mapper);
                }

                @Override
                public Set<Option> options() {
                  return EnumSet.noneOf(Option.class);
                }

                @Override
                public MappingProvider mappingProvider() {
                  return new JacksonMappingProvider(mapper);
                }
              });
        });
  }
}
