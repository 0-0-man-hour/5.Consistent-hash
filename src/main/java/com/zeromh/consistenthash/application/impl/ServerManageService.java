package com.zeromh.consistenthash.application.impl;

import com.zeromh.consistenthash.application.ServerManageUseCase;
import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.service.hash.HashServicePort;
import com.zeromh.consistenthash.domain.model.server.ServerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServerManageService implements ServerManageUseCase {

    private final ServerPort serverPort;
    private final HashServicePort hashServicePort;

    @Override
    public ServerStatus addServer(String serverName) {
        ServerStatus serverStatus = serverPort.getServerStatus();

        ServerUpdateInfo updateInfo = hashServicePort.addServerInfo(serverStatus, serverName);
        serverStatus = serverPort.addServer(updateInfo.getNewServer());

        if(updateInfo.getRehashServer() != null) {
            rehashServerAll(updateInfo.getRehashServer());
        }

        return serverStatus;
    }

    @Override
    public ServerStatus deleteServer(HashServer hashServer) {
        ServerStatus serverStatus = serverPort.getServerStatus();
        if (serverStatus.getServerNums() < 2) throw new RuntimeException("서버가 1개 이하이므로 삭제할 수 없습니다.");

        ServerUpdateInfo updateInfo = hashServicePort.deleteServerInfo(serverStatus, hashServer);

        rehashServerAll(updateInfo.getRehashServer());
        serverStatus = serverPort.deleteServer(hashServer);


        return serverStatus;
    }


    private void rehashServer(List<HashServer> rehashServers) {
        for(var fromServer : rehashServers) {
            Map<HashServer, List<HashKey>> serverMap = serverPort.getAllServerData(fromServer)
                    .stream()
                    .collect(Collectors.groupingBy(hashServicePort::getServer));

            for (var toServer : serverMap.keySet()) {
                if (toServer.getName().equals(fromServer.getName())) {
                    continue;
                }

                var targetKeyList = serverMap.get(toServer);
                serverPort.addDataList(toServer, targetKeyList);
                serverPort.delDataList(fromServer, targetKeyList);
            }
        }
    }

    private void rehashServerAll(List<HashServer> rehashServers) {
        List<HashKey> keys = new ArrayList<>();
        for (var fromSever : rehashServers) {
            var rehashKeys = serverPort.getAllServerData(fromSever);
            if (rehashKeys != null) {
                serverPort.delDataList(fromSever, rehashKeys);
                keys.addAll(rehashKeys);
            }
        }

        var serverMap = keys.stream().collect(Collectors.groupingBy(hashServicePort::getServer));

        for (var toServer : serverMap.keySet()) {
            var targetKeyList = serverMap.get(toServer);
            serverPort.addDataList(toServer, targetKeyList);

        }

    }
//    private void rehashKeys(List<StableHashServer> rehashServers) {
//        for(var fromServer : rehashServers) {
//            serverPort.getAllServerData(fromServer).stream()
//                    .filter(key -> hashServicePort.getServer(key).)
//        }
//    }


}
