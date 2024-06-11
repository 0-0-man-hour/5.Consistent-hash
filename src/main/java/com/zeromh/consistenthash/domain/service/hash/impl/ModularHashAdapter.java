package com.zeromh.consistenthash.domain.service.hash.impl;

import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.model.hash.HashFunction;
import com.zeromh.consistenthash.domain.service.hash.HashServicePort;
import com.zeromh.consistenthash.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hash.consistent", havingValue = "false")
public class ModularHashAdapter implements HashServicePort {

    private final HashFunction hashFunction;
    private Map<Integer, HashServer> serverMap;
    private int serverNums;

    @Override
    public void setServer(ServerStatus serverStatus) {
        serverMap = new HashMap<>();
        List<HashServer> servers = serverStatus.getServerList();
        serverNums = servers.size();
        for (int i = 0; i < serverNums; i++) {
            serverMap.put(i, servers.get(i));
            servers.get(i).setHashValues(List.of((long) i));

        }
    }

    @Override
    public HashServer getServer(HashKey key) {
        if (serverNums == 0) return null;
        long hashKey = hashFunction.hash(key.getKey());
        key.setHashVal((int) (hashKey % serverNums));
        return serverMap.get((int) (hashKey % serverNums));
    }

    @Override
    public ServerUpdateInfo addServerInfo(ServerStatus serverStatus, String serverName) {
        setServer(serverStatus);
        serverNums++;
        HashServer server = HashServer.builder()
                .name(serverName == null ? DateUtil.getNowDate() : serverName)
                .hashValues(List.of((long) serverNums))
                .build();
        serverMap.put(serverNums-1, server);

        return ServerUpdateInfo
                .builder()
                .newServer(server)
                .rehashServer(new ArrayList<>(serverMap.values()))
                .build();

    }

    @Override
    public ServerUpdateInfo deleteServerInfo(ServerStatus serverStatus, HashServer delServer) {
        List<HashServer> rehashServers = new ArrayList<>(serverStatus.getServerList());
        serverStatus.getServerList().removeIf(server -> server.getName().equals(delServer.getName()));
        setServer(serverStatus);

        return ServerUpdateInfo.builder()
                .rehashServer(rehashServers)
                .build();
    }


}
