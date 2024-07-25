package com.zeromh.consistenthash.interfaces;

import com.zeromh.consistenthash.application.KeyManageUseCase;
import com.zeromh.consistenthash.application.ServerManageUseCase;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConsistentHashService {
    private final ServerManageUseCase serverManageUseCase;
    private final KeyManageUseCase keyManageUseCase;

    public ServerStatus addServer(HashServer server) {
        return serverManageUseCase.addServer(server);
    }

    public ServerStatus delServer(String serverName) {
        return serverManageUseCase.deleteServer(HashServer.builder().name(serverName).build());
    }

    public HashServer getServer(HashKey key) {
        return serverManageUseCase.getServer(key);
    }

//    public ServerStatus getServerStatus() {
//        return serverManageUseCase.ge
//    }

    public HashServer addKey(HashKey key) {
        return keyManageUseCase.addKey(key);
    }

    public HashServer delKey(HashKey key) {
        return keyManageUseCase.deleteKey(key);
    }

    public HashKey getKey(HashKey key) {
        return keyManageUseCase.getKey(key).getHashKey();
    }
}
