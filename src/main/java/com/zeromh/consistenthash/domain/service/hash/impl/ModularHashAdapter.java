package com.zeromh.consistenthash.domain.service.hash.impl;

import com.zeromh.consistenthash.application.dto.HashServerDto;
import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.service.hash.function.HashFunction;
import com.zeromh.consistenthash.domain.service.hash.HashServicePort;
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
    public void setServer(List<HashServer> serverList) {
        serverMap = new HashMap<>();
        serverNums = serverList.size();
        for (int i = 0; i < serverNums; i++) {
            serverMap.put(i, serverList.get(i));
            serverList.get(i).setHashValues(List.of((long) i));
        }
    }

    @Override
    public List<Long> getServerHashes(HashServer hashServer) {
        return null;
    }

    @Override
    public long getNodeHash(HashKey key) {
        return 0;
    }

    @Override
    public ServerStatus getServerStatus() {
        return null;
    }

    @Override
    public HashServer getServer(HashKey key) {
        if (serverNums == 0) return null;
        long hashKey = hashFunction.hash(key.getKey());
        key.setHashVal((int) (hashKey % serverNums));
        return serverMap.get((int) (hashKey % serverNums));
    }

    @Override
    public ServerUpdateInfo addServerInfo(HashServer server) {
        serverNums++;
        serverMap.put(serverNums-1, server);

        return ServerUpdateInfo
                .builder()
                .newServer(server)
                .rehashServer(new ArrayList<>(serverMap.values()))
                .build();

    }

    @Override
    public ServerUpdateInfo deleteServerInfo(HashServer delServer) {
        ServerStatus serverStatus = getServerStatus();
        List<HashServer> rehashServers = new ArrayList<>(serverStatus.getServerList());
        serverStatus.getServerList().removeIf(server -> server.getName().equals(delServer.getName()));
        setServer(rehashServers);

        return ServerUpdateInfo.builder()
                .rehashServer(rehashServers)
                .build();
    }

    @Override
    public List<HashServer> getServers(HashKey key, int n) {
        return null;
    }

    @Override
    public List<HashServerDto> getAliveServers(HashKey key, int n) {
        return null;
    }

    @Override
    public List<HashServer> getServersFromHash(Long hash, int n) {
        return null;
    }


}
