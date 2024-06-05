package com.zeromh.consistenthash.hash.adapter.function;

import com.zeromh.consistenthash.hash.port.out.HashFunction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "hash.function", havingValue = "custom")
public class CustomHashFunction implements HashFunction {

    private final int MOD = 101;

    public long hash(String val) {
        return val.hashCode() % MOD;
    }

    public long getMod() {
        return MOD;
    }
}
