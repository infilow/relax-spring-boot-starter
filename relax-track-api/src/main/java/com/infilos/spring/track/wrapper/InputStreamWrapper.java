package com.infilos.spring.track.wrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class InputStreamWrapper extends ServletInputStream {
    private final ByteArrayInputStream buffer;

    public InputStreamWrapper(byte[] bytes) {
        this.buffer = new ByteArrayInputStream(bytes);
    }

    @Override
    public int read() throws IOException {
        return buffer.read();
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        throw new RuntimeException("Not implemented");
    }
}
