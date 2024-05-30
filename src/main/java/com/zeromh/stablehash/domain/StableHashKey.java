package com.zeromh.stablehash.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StableHashKey {
    private int id;

    @Override
    public int hashCode() {
        return id;
    }
}
