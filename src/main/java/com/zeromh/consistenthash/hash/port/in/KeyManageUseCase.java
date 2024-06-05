package com.zeromh.consistenthash.hash.port.in;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;

public interface KeyManageUseCase {
    HashServer addKey(HashKey key);
    HashServer deleteKey(HashKey key);
    HashKey getKey(HashKey key);
}
