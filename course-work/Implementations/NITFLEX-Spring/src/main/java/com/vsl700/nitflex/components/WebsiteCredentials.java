package com.vsl700.nitflex.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:application.credentials.properties")
@Getter
@Setter
public abstract class WebsiteCredentials {
    private String username;
    private String password;

    @Component
    @ConfigurationProperties("zamunda")
    public static class Zamunda extends WebsiteCredentials {}
}
