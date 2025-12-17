// CorsProperties.java
package com.hnu.mail.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {
  private String[] allowedOrigins;
  private String[] allowedMethods;
  private String[] allowedHeaders;
  private Boolean allowCredentials;
}