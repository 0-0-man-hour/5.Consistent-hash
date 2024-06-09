package com.zeromh.consistenthash.application.dto;

import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeyServerDto {
    HashKey hashKey;
    HashServer server;
}
