// MailProperties.java
package com.hnu.mail.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail")
@Getter
@Setter
public class MailProperties {
  private Pop3 pop3 = new Pop3();
  private Storage storage = new Storage();
  private String domain;
  private Integer maxAttachmentSize;

  @Getter
  @Setter
  public static class Pop3 {
    private String host;
    private Integer port;
    private Boolean sslEnabled;
  }

  @Getter
  @Setter
  public static class Storage {
    private String type;
    private String localPath;
  }
}