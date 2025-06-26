package com.usememo.jugger.global.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final ApplePublicKeyProvider keyProvider;

    @Value("${apple.client-id}")
    private String clientId;

    public SignedJWT validate(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            JWSHeader header = jwt.getHeader();

            // 알고리즘 확인
            if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
                throw new IllegalArgumentException("Unexpected JWS algorithm: " + header.getAlgorithm());
            }

            // 공개키로 서명 검증
            var jwk = keyProvider.getKeyById(header.getKeyID());
            JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey());

            if (!jwt.verify(verifier)) {
                throw new IllegalArgumentException("Apple ID Token 서명 검증 실패");
            }

            // 클레임 검증
            var claims = jwt.getJWTClaimsSet();
            Date now = new Date();

            if (claims.getExpirationTime() == null || now.after(claims.getExpirationTime())) {
                throw new IllegalArgumentException("Apple ID Token 만료됨");
            }

            if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
                throw new IllegalArgumentException("잘못된 iss: " + claims.getIssuer());
            }

            if (!claims.getAudience().contains(clientId)) {
                throw new IllegalArgumentException("잘못된 aud: " + claims.getAudience());
            }

            return jwt;

        } catch (ParseException e) {
            throw new IllegalArgumentException("Apple ID Token 파싱 실패", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Apple ID Token 검증 실패", e);
        }
    }
}