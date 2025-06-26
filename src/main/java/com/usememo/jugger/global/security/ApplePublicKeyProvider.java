package com.usememo.jugger.global.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ApplePublicKeyProvider {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    private final Map<String, JWK> keyCache = new ConcurrentHashMap<>();

    public JWK getKeyById(String keyId) {
        try {
            // 이미 캐시에 있다면 재사용
            if (keyCache.containsKey(keyId)) {
                return keyCache.get(keyId);
            }

            // Apple 서버에서 공개키 가져오기
            JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
            for (JWK key : jwkSet.getKeys()) {
                keyCache.put(key.getKeyID(), key);
            }

            JWK foundKey = keyCache.get(keyId);
            if (foundKey == null) {
                throw new IllegalArgumentException("Apple 공개키에서 keyId를 찾을 수 없습니다: " + keyId);
            }

            return foundKey;
        } catch (Exception e) {
            log.error("Apple 공개키 로딩 실패", e);
            throw new IllegalArgumentException("Apple 공개키 로딩 실패", e);
        }
    }
}