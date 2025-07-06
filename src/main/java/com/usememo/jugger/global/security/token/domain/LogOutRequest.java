package com.usememo.jugger.global.security.token.domain;

public record LogOutRequest(String refreshToken, String provider) {
}
