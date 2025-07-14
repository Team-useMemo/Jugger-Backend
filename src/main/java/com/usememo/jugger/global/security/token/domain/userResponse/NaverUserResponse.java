package com.usememo.jugger.global.security.token.domain.userResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class NaverUserResponse {
    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private String id;
        private String email;
        private String nickname;
    }
}

