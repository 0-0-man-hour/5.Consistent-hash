package com.zeromh.consistenthash.domain.service.hash.impl;

import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.model.hash.HashFunction;
import com.zeromh.consistenthash.domain.service.hash.HashServicePort;
import com.zeromh.consistenthash.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@ConditionalOnProperty(name = "hash.consistent", havingValue = "true")
@RequiredArgsConstructor
public class ConsistentHashAdapter implements HashServicePort {

    private final HashFunction hashFunction;

    @Value("${hash.node-nums}")
    private int numsOfReplicas;
    SortedMap<Long, HashServer> ring = new TreeMap<>();

    @Override
    public void setServer(ServerStatus serverStatus) {
        this.ring =  serverStatus.getServerList().stream()
                .flatMap(server -> IntStream.range(0, numsOfReplicas)
                        .mapToObj(i -> new AbstractMap.SimpleEntry<>(
                                hashFunction.hash(server.getName() + i),
                                server)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (k1, k2) -> k2,
                        TreeMap::new
                ));
//        this.ring = new TreeMap<>();
//        for (var server : serverStatus.getServerList()) {
//            for(var severValue : server.getHashValues()) {
//                ring.put(severValue, server);
//            }
//        }
    }
    
    public HashServer getServer(HashKey key) {
        if (ring.isEmpty()) {
            return null;
        }

        long hash = hashFunction.hash(key.getKey());
        key.setHashVal(hash);

        if (!ring.containsKey(hash)) {
            SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }
        return ring.get(hash);
    }

    public ServerUpdateInfo addServerInfo(ServerStatus serverStatus, String serverName) {
        setServer(serverStatus);
        if (serverName == null) {
            serverName = DateUtil.getNowDate();
        }

        String finalServerName = serverName;
        List<Long> newServerHashes = IntStream.range(0,numsOfReplicas)
                .mapToObj(i -> hashFunction.hash(finalServerName + i))
                .toList();


        Set<HashServer> rehashServers = new HashSet<>();
        if (!ring.isEmpty()) {
            for (var hash : newServerHashes) {
                if (!ring.containsKey(hash)) {
                    SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
                    hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
                }
                rehashServers.add(ring.get(hash));
            }
        }


        HashServer newServer = HashServer.builder()
                .name(serverName)
                .hashValues(newServerHashes)
                .build();

        for (var hash : newServerHashes) {
            ring.put(hash, newServer);
        }

        return ServerUpdateInfo
                .builder()
                .newServer(newServer)
                .rehashServer(rehashServers.stream().toList())
                .build();
    }

    @Override
    public ServerUpdateInfo deleteServerInfo(ServerStatus serverStatus, HashServer server) {
        setServer(serverStatus);
        for (int i = 0; i < numsOfReplicas; i++) {
            ring.remove(hashFunction.hash(server.getName()+i));
        }

        return ServerUpdateInfo
                .builder()
                .rehashServer(new ArrayList<>(List.of(server)))
                .build();
    }

}
