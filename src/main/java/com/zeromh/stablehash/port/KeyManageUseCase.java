package com.zeromh.stablehash.port;

import com.zeromh.stablehash.domain.StableHashKey;
import com.zeromh.stablehash.domain.StableHashServer;

public interface KeyManageUseCase {
    StableHashServer addKey(StableHashKey key);
    StableHashServer deleteKey(StableHashKey key);
}
