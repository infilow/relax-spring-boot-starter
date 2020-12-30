package com.infilos.spring.track.config;

import com.infilos.spring.track.wrapper.SpringRequestWrapper;
import com.infilos.spring.track.wrapper.SpringRespondWrapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class WrappedExchangeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        filterChain.doFilter(
            SpringRequestWrapper.create(request),
            SpringRespondWrapper.create(response)
        );
    }
}
