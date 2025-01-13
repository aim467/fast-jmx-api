package com.fast.jmx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fast.jmx")
public class FastJmxProperties {

    private String username;

    private String password;

    private String secretKey;
}
