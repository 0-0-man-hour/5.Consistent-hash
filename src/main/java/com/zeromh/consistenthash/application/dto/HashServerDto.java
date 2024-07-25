package com.zeromh.consistenthash.application.dto;

import com.zeromh.consistenthash.domain.model.server.HashServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HashServerDto {
    HashServer server;
    HashServer failureServer;

    public HashServerDto(HashServer server) {
        this.server = server;
    }
}
