package com.usememo.jugger.global.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class ApplePublicKeyProvider {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final Duration CACHE_DURATION = Duration.ofDays(15);

    private final Map<String, JWK> keyCache = new ConcurrentHashMap<>();
    private volatile Instant lastCacheTime = Instant.MIN;
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    public JWK getKeyById(String keyId) {

        try {
            // 캐시 확인 및 만료 체크
            cacheLock.readLock().lock();
            try {
                if (keyCache.containsKey(keyId) && !isCacheExpired()) {
                    return keyCache.get(keyId);
                }
            } finally {
                cacheLock.readLock().unlock();
            }

            // 캐시 갱신
            cacheLock.writeLock().lock();
            try {
                // 다른 스레드가 갱신했는지 재확인
                if (keyCache.containsKey(keyId) && !isCacheExpired()) {
                    return keyCache.get(keyId);
                }
                // Apple 서버에서 공개키 가져오기
                JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
                for (JWK key : jwkSet.getKeys()) {
                    keyCache.put(key.getKeyID(), key);
                }
                lastCacheTime = Instant.now();
            } finally {
                cacheLock.writeLock().unlock();
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

    private boolean isCacheExpired() {
        return Duration.between(lastCacheTime, Instant.now()).compareTo(CACHE_DURATION) > 0;
    }
}