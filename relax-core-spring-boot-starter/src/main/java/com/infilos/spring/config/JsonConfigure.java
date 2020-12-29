package com.infilos.spring.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.infilos.relax.Json;
import com.infilos.relax.json.JsonException;
import com.infilos.utils.Datetimes;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class JsonConfigure {

  /** Serialize and deserialize date/datetime with standard format. */
  static SimpleModule customTimeModule() {
    SimpleModule module = new SimpleModule();

    module.addSerializer(LocalDate.class, new LocalDateSerializer());
    module.addSerializer(LocalDateTime.class, new LocalDatetimeSerializer());
    module.addSerializer(ZonedDateTime.class, new ZonedDatetimeSerializer());
    module.addSerializer(Timestamp.class, new TimestampSerializer());

    module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
    module.addDeserializer(LocalDateTime.class, new LocalDatetimeDeserializer());
    module.addDeserializer(ZonedDateTime.class, new ZonedDatetimeDeserializer());
    module.addDeserializer(Timestamp.class, new TimestampDeserializer());

    return module;
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    Json.underMapper().disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    Json.underMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    Json.underMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    Json.underMapper().registerModule(customTimeModule());

    return Json.underMapper();
  }

  @Bean
  public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
    return new MappingJackson2HttpMessageConverter(objectMapper());
  }

  private static final class LocalDateSerializer extends JsonSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(value.format(Datetimes.Patterns.AtDays));
    }
  }

  private static final class LocalDatetimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(value.format(Datetimes.Patterns.AtSeconds));
    }
  }

  private static final class ZonedDatetimeSerializer extends JsonSerializer<ZonedDateTime> {

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(value.format(Datetimes.Patterns.AtSeconds));
    }
  }

  private static final class TimestampSerializer extends JsonSerializer<Timestamp> {

    @Override
    public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializerProvider)
        throws IOException {
      gen.writeString(Datetimes.from(value.getTime()).format(Datetimes.Patterns.AtSeconds));
    }
  }

  private static final class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isTextual() && StringUtils.isNotBlank(node.textValue())) {
        return LocalDate.parse(node.textValue(), Datetimes.Patterns.AtDays);
      }

      throw JsonException.of("Deserialize LocalDate failed: " + node.toString());
    }
  }

  private static final class LocalDatetimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isTextual() && StringUtils.isNotBlank(node.textValue())) {
        return LocalDateTime.parse(node.textValue(), Datetimes.Patterns.AtSeconds);
      }

      throw JsonException.of("Deserialize LocalDateTime failed: " + node.toString());
    }
  }

  private static final class ZonedDatetimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isTextual() && StringUtils.isNotBlank(node.textValue())) {
        return ZonedDateTime.parse(node.textValue(), Datetimes.Patterns.AtSeconds);
      }

      throw JsonException.of("Deserialize ZonedDateTime failed: " + node.toString());
    }
  }

  private static final class TimestampDeserializer extends JsonDeserializer<Timestamp> {

    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isTextual() && StringUtils.isNotBlank(node.textValue())) {
        return Timestamp.valueOf(
            LocalDateTime.parse(node.textValue(), Datetimes.Patterns.AtSeconds));
      }

      throw JsonException.of("Deserialize Timestamp failed: " + node.toString());
    }
  }
}
