package com.zeromh.stablehash.port;

import com.zeromh.stablehash.domain.ServerStatus;
import com.zeromh.stablehash.domain.StableHashKey;

public interface HashServicePort {

    Integer getIndex(StableHashKey key, ServerStatus serverStatus);
    Integer getIndexVirtual(StableHashKey key, ServerStatus serverStatus);
}
