package com.zeromh.consistenthash.application;

import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.application.dto.KeyServerDto;

public interface KeyManageUseCase {
    HashServer addKey(HashKey key);
    HashServer deleteKey(HashKey key);
    KeyServerDto getKey(HashKey key);
}
