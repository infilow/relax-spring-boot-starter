package com.infilos.spring.track.wrapper;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class SpringRespondWrapper extends HttpServletResponseWrapper {
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private OutputStreamWrapper copier;

    public SpringRespondWrapper(HttpServletResponse response) {
        super(response);
    }

    public static SpringRespondWrapper create(HttpServletResponse response) throws IOException {
        if (response instanceof SpringRespondWrapper) {
            return (SpringRespondWrapper) response;
        }

        return new SpringRespondWrapper(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter has already been called on this respond.");
        }
        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new OutputStreamWrapper(outputStream);
        }

        return copier;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream has already been called on this respond.");
        }

        if (writer == null) {
            copier = new OutputStreamWrapper(getResponse().getOutputStream());
            writer =
                new PrintWriter(
                    new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
        }

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            copier.flush();
        }
    }

    public byte[] getContentAsByteArray() {
        if (copier != null) {
            return copier.getCopied();
        } else {
            return new byte[0];
        }
    }

    public Map<String, String> getAllHeaders() {
        final Map<String, String> headers = new HashMap<>();
        getHeaderNames().forEach(it -> headers.put(it, getHeader(it)));
        return headers;
    }

    public String getBodyString() throws IOException {
        return IOUtils.toString(getContentAsByteArray(), getCharacterEncoding());
    }
}
