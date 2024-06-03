package com.vsl700.nitflex.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("nitflex")
@Getter
@Setter
public class SharedProperties {
    private String moviesFolder;
    private int downloadInterval;
    private String movieRequestPrivilege;
    private int movieSizeLimit;
    private boolean transcodingEnabled;
    private String[] frontEndUrls;
}
