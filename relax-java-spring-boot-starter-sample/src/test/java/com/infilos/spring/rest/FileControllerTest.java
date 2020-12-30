package com.infilos.spring.rest;

import com.infilos.spring.model.User;
import com.infilos.spring.utils.Respond;
import com.infilos.utils.Resource;
import kong.unirest.GenericType;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileControllerTest {

    @Test
    public void test() throws URISyntaxException {
        HttpResponse<Respond<String>> resp1 = Unirest.post("http://localhost:8080/file/upload")
            .header("Accept", "application/json")
            .field("file", Resource.readAsFile("/file.txt"))
            .asObject(new GenericType<Respond<String>>() {
            });

        assertTrue(resp1.isSuccess());
        System.out.println(resp1.getBody().toString());

        HttpResponse<Respond<User>> resp2 = Unirest.post("http://localhost:8080/file/json")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .queryString("key", "value")
            .body(new User("id", "name"))
            .asObject(new GenericType<Respond<User>>() {
            });

        assertTrue(resp2.isSuccess());
        System.out.println(resp2.getBody().toString());

        HttpResponse<Respond<String>> resp3 = Unirest.post("http://localhost:8080/file/form")
            .header("Accept", "application/json")
            .field("k1", "v1")
            .field("k2", "v2")
            .asObject(new GenericType<Respond<String>>() {
            });

        assertTrue(resp3.isSuccess());
        System.out.println(resp3.getBody().toString());

        HttpResponse<Respond<String>> resp4 = Unirest.post("http://localhost:8080/file/mixed")
            .header("Accept", "application/json")
            .field("request", new User("id", "name"))
            .field("files", Resource.readAsFile("/file.txt"))
            .asObject(new GenericType<Respond<String>>() {
            });

        assertTrue(resp4.isSuccess());
        System.out.println(resp4.getBody().toString());
    }
}