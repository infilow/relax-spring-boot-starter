package com.infilos.spring.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.infilos.relax.Json;
import com.infilos.spring.model.Attach;
import com.infilos.spring.utils.Respond;
import com.infilos.utils.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Tag(name = "文件管理")
@RestController
public class FileController implements Loggable {
    private static final Logger log = Loggable.logger(FileController.class);

    private static final AtomicReference<Attach> LastSingleFile = new AtomicReference<>();

    @PostMapping("/file/form")
    public Respond<String> submitForm(String k1, String k2) {
        return Respond.succed(k1 + ", " + k2);
    }

    @PostMapping("/file/json")
    public Respond<JsonNode> submitJson(@RequestBody Object object, @RequestParam String key) {
        System.out.println("key: " + key);
        return Respond.succed(Json.from(object).asJsonNode());
    }

    @Operation(summary = "上传文件")
    @PostMapping("/file/upload")
    public Respond<String> uploadSingleFile(MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null) {
            return Respond.failed("File is null.");
        }
        if (StringUtils.isBlank(file.getOriginalFilename())) {
            return Respond.failed("Filename is blank.");
        }

        Optional<String> suffix = detectSuffix(file.getOriginalFilename());
        if (!suffix.isPresent()) {
            return Respond.failed("File suffix cannot detect.");
        }

        Either<String, File> temporary = createTemporary(suffix.get());
        if (temporary.isLeft()) {
            return Respond.failed("Create temporal file failed, " + temporary.getLeft());
        }

        Either<String, File> copied = copyFileBytes(file, temporary.get());
        if (copied.isLeft()) {
            return Respond.failed("Copy uploaded file to temporary failed, " + copied.getLeft());
        }

        Optional<String> format = detectFormat(copied.get());
        if (!format.isPresent()) {
            return Respond.failed("File format cannot detect.");
        }

        Either<String, byte[]> bytes = readFileBytes(copied.get());
        if (bytes.isLeft() || bytes.get().length == 0) {
            return Respond.failed("Read uploaded file bytes failed, " + bytes.getLeft());
        }

        LastSingleFile.set(new Attach(file.getOriginalFilename(), suffix.get(), format.get(), bytes.get()));
        
        if (temporary.isRight()) {
            cleanTemporary(temporary.get());
        }

        return Respond.succed(LastSingleFile.get().getName());
    }

    @Operation(summary = "下载文件")
    @GetMapping("/file/download")
    public ResponseEntity<byte[]> downloadSingleFile() {
        if (LastSingleFile.get() == null) {
            return Respond.failed("No file uploaded").asBytesResponse(Json.underMapper());
        }

        Attach attach = LastSingleFile.get();
        return Respond.ofFileBytes(attach.getName(), attach.getFormat(), attach.getBinaries());
    }

    @Operation(summary = "文件+数据")
    @PostMapping("/file/mixed")
    public Respond<String> uploadFilesWithJsonPayload(String request, MultipartFile[] files) {
        return Respond.succed(String.format("request: %s, attaches: %s", request, files.length));
    }

    private static Optional<String> detectSuffix(String filename) {
        return Optional.ofNullable(FilenameUtils.getExtension(filename));
    }

    private static final Tika TIKA = new Tika();

    private static Optional<String> detectFormat(File file) {
        try {
            return Optional.ofNullable(TIKA.detect(file));
        } catch (IOException e) {
            log.error("Tika detect format failed", e);
            return Optional.empty();
        }
    }

    public static Either<String, File> createTemporary(String suffix) {
        try {
            return Either.right(File.createTempFile(UUID.randomUUID().toString(), suffix));
        } catch (IOException e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }

    public static void cleanTemporary(File temporary) {
        try {
            if (temporary != null && temporary.isFile() && temporary.exists()) {
                temporary.delete();
            }
        } catch (Throwable ignore) {
        }
    }

    public static Either<String, File> copyFileBytes(MultipartFile source, File target) {
        try {
            FileUtils.copyInputStreamToFile(source.getInputStream(), target);
            return Either.right(target);
        } catch (IOException e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }

    public static Either<String, byte[]> readFileBytes(File file) {
        try {
            return Either.right(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            e.printStackTrace();
            return Either.left(e.getMessage());
        }
    }
}
