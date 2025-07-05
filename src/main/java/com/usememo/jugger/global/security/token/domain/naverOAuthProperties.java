package com.usememo.jugger.global.security.token.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "naver")
@Getter
@Setter

public class naverOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
