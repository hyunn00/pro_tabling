package com.yi.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final SecurityExceptionDto exceptionDto =
            new SecurityExceptionDto(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase());

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        System.out.println( accessDeniedException );

        try (
             PrintWriter out = response.getWriter();
        ) {
            out.println("<html><head><title>Access Denied</title></head><body>");
            out.println("<h1>Access Denied</h1>");
            out.println("<div>Unauthorized access. You'll be redirected to home page in a few seconds...</div>");

            ObjectMapper objectMapper = new ObjectMapper();
            out.println( objectMapper.writeValueAsString(exceptionDto));

            out.println("<script>");
            out.println("setTimeout(function() { window.location.href = '/home'; }, 2500);");
            out.println("</script></body></html>");
            out.flush();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class SecurityExceptionDto {
        private int statusCode;
        private String msg;

        public SecurityExceptionDto(int statusCode, String msg) {
            this.statusCode = statusCode;
            this.msg = msg;
        }
    }
}