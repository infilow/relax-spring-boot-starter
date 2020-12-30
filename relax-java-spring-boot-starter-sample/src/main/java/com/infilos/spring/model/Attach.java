package com.infilos.spring.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Attach {
    private String name;
    private String suffix;
    private String format;
    private byte[] binaries;
}
