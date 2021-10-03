package com.edgedx.connectpoc.utils;

import lombok.extern.java.Log;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Log
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
 
    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex)
      throws IOException {
        log.info("Request body: " + new String(reqBody, StandardCharsets.UTF_8));
        ClientHttpResponse response = ex.execute(req, reqBody);
        return response;
    }
}