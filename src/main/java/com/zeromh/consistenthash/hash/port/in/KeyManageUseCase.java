package com.zeromh.consistenthash.hash.port.in;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.hash.dto.KeyServerDto;

public interface KeyManageUseCase {
    HashServer addKey(HashKey key);
    HashServer deleteKey(HashKey key);
    KeyServerDto getKey(HashKey key);
}
