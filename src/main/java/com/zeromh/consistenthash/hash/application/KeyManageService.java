package com.zeromh.consistenthash.hash.application;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.hash.dto.KeyServerDto;
import com.zeromh.consistenthash.hash.port.out.HashServicePort;
import com.zeromh.consistenthash.hash.port.in.KeyManageUseCase;
import com.zeromh.consistenthash.server.port.out.ServerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyManageService implements KeyManageUseCase {

    private final HashServicePort hashServicePort;
    private final ServerPort serverPort;

    @Override
    public HashServer addKey(HashKey key) {
        HashServer targetServer = getSever(key);

        serverPort.addData(key, targetServer);
        return targetServer;
    }

    @Override
    public HashServer deleteKey(HashKey key) {
        HashServer targetServer = getSever(key);

        serverPort.deleteData(key, targetServer);
        return targetServer;
    }

    @Override
    public KeyServerDto getKey(HashKey key) {
        HashServer targetServer = getSever(key);
        return KeyServerDto.builder()
                .server(targetServer)
                .hashKey(serverPort.getKey(key, targetServer))
                .build();
    }

    private HashServer getSever(HashKey key) {
        HashServer targetServer = hashServicePort.getServer(key);
        if (targetServer == null) {
            hashServicePort.setServer(serverPort.getServerStatus());
            targetServer = hashServicePort.getServer(key);
        }
        return targetServer;
    }

}
