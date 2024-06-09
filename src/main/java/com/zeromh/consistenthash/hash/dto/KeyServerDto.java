package com.zeromh.consistenthash.hash.dto;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeyServerDto {
    HashKey hashKey;
    HashServer server;
}
