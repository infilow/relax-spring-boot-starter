package com.infilos.spring.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infilos.relax.Json;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.springframework.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Respond<T> {
    private static final Integer SuccedCode = 0;
    private static final Integer FailedCode = -1;
    private static final String BlankMessage = "";

    private final Integer code;
    private final T data;
    private final String message;

    @JsonCreator
    public Respond(
        @JsonProperty("code") int code,
        @JsonProperty("data") T data,
        @JsonProperty("message") String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Respond<T> succed() {
        return new Respond<>(SuccedCode, null, BlankMessage);
    }

    public static <T> Respond<T> succed(T data) {
        return new Respond<>(SuccedCode, data, BlankMessage);
    }

    public static <T> Respond<T> succed(int code, T data) {
        return new Respond<>(code, data, BlankMessage);
    }

    public static <T> Respond<T> succed(int code) {
        return new Respond<>(code, null, BlankMessage);
    }

    public static <T> Respond<T> failed(String message) {
        return new Respond<>(FailedCode, null, message);
    }

    public static <T> Respond<T> failed(int code, String message) {
        return new Respond<>(code, null, message);
    }

    public static <T> Respond<T> failed(RespondEnum<?> error) {
        return new Respond<>(error.getCode(), null, error.getMessage());
    }

    public static <T> Respond<T> failed(RespondEnum<?> error, String message) {
        return new Respond<>(error.getCode(), null, message);
    }

    public static <T> Respond<T> failed(RespondEnum<?> error, Object... messageTemplateArgs) {
        return new Respond<>(error.getCode(), null, String.format(error.getMessage(), messageTemplateArgs));
    }

    public static <T> Respond<T> failed(Throwable cause) {
        return new Respond<>(
            FailedCode,
            null,
            String.format(
                "%s(%s)",
                cause.getClass().getSimpleName(),
                Objects.isNull(cause.getMessage()) ? "" : cause.getMessage()));
    }

    public static <T> Respond<T> failed(int code, Throwable cause) {
        return new Respond<>(
            code,
            null,
            String.format(
                "%s(%s)",
                cause.getClass().getSimpleName(),
                Objects.isNull(cause.getMessage()) ? "" : cause.getMessage()));
    }

    public static <T> Respond<T> failed() {
        return new Respond<>(FailedCode, null, "Unknown failure");
    }

    public static <T> Respond<T> of(Either<?, T> either) {
        if (either.isLeft()) {
            Object error = either.getLeft();
            if (error instanceof String) return failed((String) error);
            else if (error instanceof Throwable) return failed((Throwable) error);
            else return failed(error.toString());
        } else {
            return succed(either.get());
        }
    }

    public static ResponseEntity<byte[]> ofBytes(
        HttpStatus status, HttpHeaders headers, byte[] bytes) {
        return new ResponseEntity<>(bytes, headers, status);
    }

    public static ResponseEntity<byte[]> ofFileBytes(String name, byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition
            .attachment()
            .filename(name, StandardCharsets.UTF_8)
            .build()
            .toString()
        );
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ofBytes(HttpStatus.OK, headers, bytes);
    }

    public static ResponseEntity<byte[]> ofFileBytes(String name, String format, byte[] bytes) {
        String mediaType =
            Try.of(() -> MediaType.parseMediaType(format))
                .map(MediaType::toString)
                .toJavaOptional()
                .orElse(
                    MediaTypeFactory.getMediaType(name)
                        .map(MediaType::toString)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        String contentDisposition = ContentDisposition
            .attachment()
            .filename(name, StandardCharsets.UTF_8)
            .build()
            .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, mediaType);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

        return ofBytes(HttpStatus.OK, headers, bytes);
    }

    @JsonProperty
    public Integer getCode() {
        return code;
    }

    @JsonProperty
    public T getData() {
        return data;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Respond(code=%d, message=%s, data=%s)", code, message, data);
    }

    @JsonIgnore
    public boolean isSucced() {
        return SuccedCode.equals(code);
    }

    @JsonIgnore
    public boolean isSuccedWithData() {
        return isSucced() && Objects.nonNull(data);
    }

    /**
     * Convert this Respond to json ResponseEntity.
     */
    @JsonIgnore
    public ResponseEntity<byte[]> asBytesResponse(ObjectMapper objectMapper) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            bytes = String.format(
                "{\"code\":-1,\"message\":\"Serialize %s failed: %s\"}",
                this, e.getMessage()
            ).getBytes();
        }

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /**
     * Convert this Respond to json ResponseEntity.
     */
    @JsonIgnore
    public ResponseEntity<byte[]> asBytesResponse() {
        return asBytesResponse(Json.underMapper());
    }
}
