package com.infilos.spring.config

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.SimpleModule
import com.infilos.relax.Json4s
import com.infilos.relax.json.JsonException
import com.infilos.utils.Datetimes
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation._
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

@Configuration
@AutoConfigureAfter(Array(classOf[WebMvcAutoConfiguration]))
class JsonConfigure {

  @Bean
  def jackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter =
    new MappingJackson2HttpMessageConverter(objectMapper())

  @Bean
  def objectMapper(): ObjectMapper = {
    Json4s.underMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    Json4s.underMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    Json4s.underMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    Json4s.underMapper.registerModule(customTimeModule)

    Json4s.underMapper()
  }

  private def customTimeModule: SimpleModule = {
    val module = new SimpleModule()

    module.addSerializer(classOf[LocalDate], new JsonConfigure.LocalDateSerializer)
    module.addSerializer(classOf[LocalDateTime], new JsonConfigure.LocalDatetimeSerializer)
    module.addSerializer(classOf[ZonedDateTime], new JsonConfigure.ZonedDatetimeSerializer)
    module.addSerializer(classOf[Timestamp], new JsonConfigure.TimestampSerializer)

    module.addDeserializer(classOf[LocalDate], new JsonConfigure.LocalDateDeserializer)
    module.addDeserializer(classOf[LocalDateTime], new JsonConfigure.LocalDatetimeDeserializer)
    module.addDeserializer(classOf[ZonedDateTime], new JsonConfigure.ZonedDatetimeDeserializer)
    module.addDeserializer(classOf[Timestamp], new JsonConfigure.TimestampDeserializer)

    module
  }
}

object JsonConfigure {

  def mapper: ObjectMapper = Json4s.underMapper()

  final private class LocalDateSerializer extends JsonSerializer[LocalDate] {

    override def serialize(value: LocalDate, gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeString(value.format(Datetimes.Patterns.AtDays))
    }
  }

  final private class LocalDatetimeSerializer extends JsonSerializer[LocalDateTime] {

    override def serialize(value: LocalDateTime, gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeString(value.format(Datetimes.Patterns.AtSeconds))
    }
  }

  final private class ZonedDatetimeSerializer extends JsonSerializer[ZonedDateTime] {

    override def serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider): Unit = {
      gen.writeString(value.format(Datetimes.Patterns.AtSeconds))
    }
  }

  final private class TimestampSerializer extends JsonSerializer[Timestamp] {

    override def serialize(value: Timestamp, gen: JsonGenerator, serializerProvider: SerializerProvider): Unit = {
      gen.writeString(Datetimes.from(value.getTime).format(Datetimes.Patterns.AtSeconds))
    }
  }

  final private class LocalDateDeserializer extends JsonDeserializer[LocalDate] {

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDate = {
      val node: JsonNode = p.getCodec.readTree(p)
      if (node.isTextual && StringUtils.isNotBlank(node.textValue)) {
        return LocalDate.parse(node.textValue, Datetimes.Patterns.AtDays)
      }

      throw JsonException.of("Deserialize LocalDate failed: " + node.toString)
    }
  }

  final private class LocalDatetimeDeserializer extends JsonDeserializer[LocalDateTime] {

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime = {
      val node: JsonNode = p.getCodec.readTree(p)
      if (node.isTextual && StringUtils.isNotBlank(node.textValue)) {
        return LocalDateTime.parse(node.textValue, Datetimes.Patterns.AtSeconds)
      }
      throw JsonException.of("Deserialize LocalDateTime failed: " + node.toString)
    }
  }

  final private class ZonedDatetimeDeserializer extends JsonDeserializer[ZonedDateTime] {

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime = {
      val node: JsonNode = p.getCodec.readTree(p)
      if (node.isTextual && StringUtils.isNotBlank(node.textValue)) {
        return ZonedDateTime.parse(node.textValue, Datetimes.Patterns.AtSeconds)
      }
      throw JsonException.of("Deserialize ZonedDateTime failed: " + node.toString)
    }
  }

  final private class TimestampDeserializer extends JsonDeserializer[Timestamp] {

    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Timestamp = {
      val node: JsonNode = p.getCodec.readTree(p)
      if (node.isTextual && StringUtils.isNotBlank(node.textValue)) {
        return Timestamp.valueOf(LocalDateTime.parse(node.textValue, Datetimes.Patterns.AtSeconds))
      }
      throw JsonException.of("Deserialize Timestamp failed: " + node.toString)
    }
  }

}
