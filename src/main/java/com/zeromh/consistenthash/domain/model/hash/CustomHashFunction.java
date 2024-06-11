package com.zeromh.consistenthash.domain.model.hash;

import com.zeromh.consistenthash.domain.model.hash.HashFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "hash.function", havingValue = "custom")
public class CustomHashFunction implements HashFunction {

    private final int MOD = 101;

    public long hash(String val) {

        if (val.length() > 10) {
            return val.hashCode()  * -1 % MOD;
        }
        return Long.parseLong(val) % MOD;
    }

}
