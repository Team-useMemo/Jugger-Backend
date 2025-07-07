package com.usememo.jugger.global.security.token.domain.logOutResponse;

public record LogOutRequest(String refreshToken, String provider) {
}
