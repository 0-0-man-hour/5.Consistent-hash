package com.zeromh.consistenthash.domain.service.hash.impl;

import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.service.hash.function.HashFunction;
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
    private int defaultNodeNums;
    SortedMap<Long, HashServer> ring = new TreeMap<>();

    @Override
    public void setServer(ServerStatus serverStatus) {
        this.ring =  serverStatus.getServerList().stream()
                .flatMap(server -> IntStream.range(0, defaultNodeNums)
                        .mapToObj(i -> new AbstractMap.SimpleEntry<>(
                                hashFunction.hash(server.getName() + i),
                                server)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (k1, k2) -> k2,
                        TreeMap::new
                ));
    }

    @Override
    public long getNodeHash(HashKey key) {
        if (ring.isEmpty()) {
            return -1L;
        }

        long hash = hashFunction.hash(key.getKey());
        key.setHashVal(hash);

        if (!ring.containsKey(hash)) {
            SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }

        return hash;
    }
    public ServerStatus getServerStatus() {
        List<HashServer> hashServerList = ring.values().stream().toList();
        return ServerStatus.builder()
                .serverNums(hashServerList.size())
                .serverList(hashServerList)
                .build();
    }

    @Override
    public HashServer getServer(HashKey key) {
        long hash = getNodeHash(key);
        if (hash == -1) {
            return null;
        }
        key.setServerHash(hash);
        return ring.get(hash);
    }

    public ServerUpdateInfo addServerInfo(HashServer newServer) {
        if (newServer.getName() == null) {
            newServer.setName(DateUtil.getNowDate());
        }

        String finalServerName = newServer.getName();
        if (newServer.getNumsOfNode() == 0) {
            newServer.setNumsOfNode(defaultNodeNums);
        }

        List<Long> newServerHashes = IntStream.range(0, newServer.getNumsOfNode())
                .mapToObj(i -> hashFunction.hash(finalServerName + i))
                .toList();
        newServer.setHashValues(newServerHashes);

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
    public ServerUpdateInfo deleteServerInfo(HashServer server) {
        for (int i = 0; i < server.getNumsOfNode(); i++) {
            ring.remove(hashFunction.hash(server.getName()+i));
        }

        return ServerUpdateInfo
                .builder()
                .rehashServer(new ArrayList<>(List.of(server)))
                .build();
    }

    @Override
    public List<HashServer> getReplicaServers(HashKey key, int n) {
        if (ring.isEmpty()) {
            return null;
        }
        n = n+1;
        long hash = hashFunction.hash(key.getKey());
        key.setHashVal(hash);

        Set<HashServer> replicaServers = new HashSet<>();

        SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
        hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        key.setServerHash(hash);

        HashServer responseServer = ring.get(hash);

        Iterator<Map.Entry<Long, HashServer>> iterator = tailMap.isEmpty() ? ring.entrySet().iterator() : tailMap.entrySet().iterator();

        while (iterator.hasNext() && replicaServers.size() < n) {
            Map.Entry<Long, HashServer> entry = iterator.next();
            replicaServers.add(entry.getValue());
        }

        if (replicaServers.size() < n) {
            iterator = ring.entrySet().iterator();
            while (iterator.hasNext() && replicaServers.size() < n) {
                Map.Entry<Long, HashServer> entry = iterator.next();
                replicaServers.add(entry.getValue());
            }
        }

        replicaServers.remove(responseServer);
        return replicaServers.stream().toList();
    }

}
