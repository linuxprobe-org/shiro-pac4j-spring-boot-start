package org.linuxprobe.shiro.spring.boot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.linuxprobe.shiro.config.ShiroPac4jConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shiro")
public class ShiroPac4jProperties extends ShiroPac4jConfig {
}
