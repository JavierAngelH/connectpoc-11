package com.edgedx.connectpoc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.security.Security;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
           return application.sources(ConnectpocApplication.class);
    }

}
