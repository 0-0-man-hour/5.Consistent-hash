package com.zeromh.stablehash.application;

import com.zeromh.stablehash.domain.ServerStatus;
import com.zeromh.stablehash.domain.StableHashKey;
import com.zeromh.stablehash.domain.StableHashServer;
import com.zeromh.stablehash.port.HashServicePort;
import com.zeromh.stablehash.port.KeyManageUseCase;
import com.zeromh.stablehash.port.ServerPort;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.ServerInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyManageService implements KeyManageUseCase {

    private final HashServicePort hashServicePort;
    private final ServerPort serverPort;

    @Override
    public StableHashServer addKey(StableHashKey key) {
        ServerStatus serverInfo = serverPort.getServerStatus();
        int targetServerIndex = hashServicePort.getIndex(key, serverInfo);

        int serverNums = serverInfo.getServerNums();


        return null;
    }

    @Override
    public StableHashServer deleteKey(StableHashKey key) {
        return null;
    }


}
