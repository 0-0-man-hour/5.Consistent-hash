package com.zeromh.consistenthash.domain.service.hash.function;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@ConditionalOnProperty(name = "hash.function", havingValue = "md5")
public class MD5HashFunction implements HashFunction {

    MessageDigest instance;

    public MD5HashFunction() {
        try {
            instance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("no algorythm found");
        }
    }

    @Override
    public long hash(String key) {
        instance.reset();
        instance.update(key.getBytes());
        byte[] digest = instance.digest();
        long h = 0;
        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= (digest[i]) & 0xFF;
        }
        return h;
    }
}
