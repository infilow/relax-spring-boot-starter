package com.infilos.spring.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.infilos.relax.Json;
import com.infilos.relax.json.JsonException;
import com.infilos.spring.utils.CodeBasedEnum;
import com.infilos.spring.utils.NameBasedEnum;
import com.infilos.utils.Datetimes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class JsonConfigure {
    private static final ConcurrentHashMap<Class<?>, JsonDeserializer<?>> ENUM_DESER_CACHE = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        Json.underMapper().disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        Json.underMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        Json.underMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        Json.underMapper().registerModule(customTimeModule());
        Json.underMapper().registerModule(customEnumModule());

        return Json.underMapper();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(objectMapper());
    }

    /**
     * Serialize and deserialize date/datetime with standard format.
     */
    private static SimpleModule customTimeModule() {
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

    private static Module customEnumModule() {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            @SuppressWarnings("unchecked")
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config,
                                                              JavaType type,
                                                              BeanDescription beanDescr,
                                                              JsonDeserializer<?> deserializer) {
                if (beanDescr.getBeanClass().isEnum()) {
                    if (CodeBasedEnum.class.isAssignableFrom(beanDescr.getBeanClass())) {
                        ENUM_DESER_CACHE.computeIfAbsent(
                            beanDescr.getBeanClass(),
                            k -> new CodeBasedEnumDeserializer<>((Class<CodeBasedEnum<?>>) beanDescr.getBeanClass())
                        );
                        return ENUM_DESER_CACHE.get(beanDescr.getBeanClass());
//                        return new CodeBasedEnumDeserializer<>((Class<CodeBasedEnum<?>>) beanDescr.getBeanClass());
                    } else if (NameBasedEnum.class.isAssignableFrom(beanDescr.getBeanClass())) {
                        ENUM_DESER_CACHE.computeIfAbsent(
                            beanDescr.getBeanClass(),
                            k -> new NameBasedEnumDeserializer<>((Class<NameBasedEnum<?>>) beanDescr.getBeanClass())
                        );
                        return ENUM_DESER_CACHE.get(beanDescr.getBeanClass());
//                        return new NameBasedEnumDeserializer<>((Class<NameBasedEnum<?>>) beanDescr.getBeanClass());
                    }
                }

                return deserializer;
            }
        });

        return module;
    }

    private static class CodeBasedEnumDeserializer<T extends CodeBasedEnum<E>, E extends Enum<E>> extends JsonDeserializer<T> {
        private final Class<CodeBasedEnum<?>> enumClass;

        public CodeBasedEnumDeserializer(Class<CodeBasedEnum<?>> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            TreeNode node = jsonParser.getCodec().readTree(jsonParser);
            Integer code = null;
            try {
                if (node.isValueNode()) {
                    ValueNode valueNode = (ValueNode) node;
                    code = Integer.parseInt(valueNode.asText());

                    return CodeBasedEnum.fromCode((Class<T>) enumClass, code);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("%s cannot match the code: %s", enumClass.getSimpleName(), code));
            }

            return null;
        }
    }

    private static class NameBasedEnumDeserializer<T extends NameBasedEnum<E>, E extends Enum<E>> extends JsonDeserializer<T> {
        private final Class<NameBasedEnum<?>> enumClass;

        public NameBasedEnumDeserializer(Class<NameBasedEnum<?>> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            TreeNode node = jsonParser.getCodec().readTree(jsonParser);
            String name = null;
            try {
                if (node.isValueNode()) {
                    ValueNode valueNode = (ValueNode) node;
                    name = valueNode.asText();

                    return NameBasedEnum.fromName((Class<T>) enumClass, name);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("%s cannot match the name: %s", enumClass.getSimpleName(), name));
            }

            return null;
        }
    }
}
