package com.zeromh.consistenthash.hash.adapter;

import com.zeromh.consistenthash.domain.ServerUpdateInfo;
import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.hash.port.out.HashFunction;
import com.zeromh.consistenthash.hash.port.out.HashServicePort;
import com.zeromh.consistenthash.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

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
        long hashKey = hashFunction.hash(key.getKey());
        key.setHashVal((int) (hashKey % serverNums));
        return serverMap.get((int) (hashKey % serverNums));
    }

    @Override
    public ServerUpdateInfo addServerInfo(ServerStatus serverStatus) {
        setServer(serverStatus);
        serverNums++;
        HashServer server = HashServer.builder()
                .name(DateUtil.getNowDate())
                .hashValues(List.of((long) serverNums))
                .build();
        serverMap.put(serverNums, server);

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
