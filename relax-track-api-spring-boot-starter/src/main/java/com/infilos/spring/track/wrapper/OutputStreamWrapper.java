package com.infilos.spring.track.wrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWrapper extends ServletOutputStream {
    private final OutputStream outputStream;
    private final ByteArrayOutputStream copied;

    public OutputStreamWrapper(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.copied = new ByteArrayOutputStream();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        copied.write(b);
    }

    public byte[] getCopied() {
        return copied.toByteArray();
    }
}
