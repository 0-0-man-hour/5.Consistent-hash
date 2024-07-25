package com.zeromh.consistenthash.domain.service.hash.impl;

import com.zeromh.consistenthash.application.dto.HashServerDto;
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
    public void setServer(List<HashServer> serverList) {
        this.ring =  serverList.stream()
                .flatMap(server -> IntStream.range(0, server.getNumsOfNode())
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
    public List<Long> getServerHashes(HashServer hashServer) {
        return IntStream.range(0, hashServer.getNumsOfNode())
                .mapToObj(i -> hashFunction.hash(hashServer.getName() + i))
                .toList();
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
    public List<HashServer> getServers(HashKey key, int n) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hashFunction.hash(key.getKey());
        key.setHashVal(hash);

        Set<HashServer> replicaServers = new LinkedHashSet<>();

        SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
        hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        key.setServerHash(hash);
        Iterator<Map.Entry<Long, HashServer>> iterator = tailMap.isEmpty() ? ring.entrySet().iterator() : tailMap.entrySet().iterator();

        while (iterator.hasNext() && replicaServers.size() < n) {
            HashServer server = iterator.next().getValue();
            replicaServers.add(server);
        }

        if (replicaServers.size() < n) {
            iterator = ring.entrySet().iterator();
            while (iterator.hasNext() && replicaServers.size() < n) {
                HashServer server = iterator.next().getValue();
                replicaServers.add(server);
            }
        }

        return replicaServers.stream().toList();
    }

    @Override
    public List<HashServerDto> getAliveServers(HashKey key, int n) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hashFunction.hash(key.getKey());
        key.setHashVal(hash);

        Set<HashServer> replicaServers = new LinkedHashSet<>();
        Set<HashServer> failureServers = new LinkedHashSet<>();

        SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
        Iterator<Map.Entry<Long, HashServer>> iterator = tailMap.isEmpty() ? ring.entrySet().iterator() : tailMap.entrySet().iterator();

        key.setServerHash(ring.tailMap(hash).firstKey() == null ? ring.firstKey() : ring.tailMap(hash).firstKey());
        key.setPrevServerHash(ring.headMap(hash).lastKey() == null ? ring.lastKey() : ring.headMap(hash).lastKey());

        while (iterator.hasNext() && replicaServers.size() < n) {
            HashServer server = iterator.next().getValue();
            if (!server.isAlive()) {
                failureServers.add(server);
            } else {
                replicaServers.add(server);
            }
        }

        if (replicaServers.size() < n) {
            iterator = ring.entrySet().iterator();
            while (iterator.hasNext() && replicaServers.size() < n) {
                HashServer server = iterator.next().getValue();
                if (!server.isAlive()) {
                    failureServers.add(server);
                } else {
                    replicaServers.add(server);
                }
            }
        }

        List<HashServerDto> targetServers = replicaServers.stream().map(HashServerDto::new).toList();
        if (!failureServers.isEmpty()) {
            List<HashServer> failureServerList = failureServers.stream().toList();
            for (int i = 0; i < failureServerList.size(); i++) {
                targetServers.get(targetServers.size() - failureServerList.size() + i).setFailureServer(failureServerList.get(i));
            }
        }
        return targetServers;
    }

    @Override
    public List<HashServer> getServersFromHash(Long hash, int n) {
        SortedMap<Long, HashServer> tailMap = ring.tailMap(hash);
        Iterator<Map.Entry<Long, HashServer>> iterator = tailMap.isEmpty() ? ring.entrySet().iterator() : tailMap.entrySet().iterator();
        Set<HashServer> replicaServers = new LinkedHashSet<>();

        while (iterator.hasNext() && replicaServers.size() < n) {
            HashServer server = iterator.next().getValue();
            replicaServers.add(server);
        }

        if (replicaServers.size() < n) {
            iterator = ring.entrySet().iterator();
            while (iterator.hasNext() && replicaServers.size() < n) {
                HashServer server = iterator.next().getValue();
                replicaServers.add(server);
            }
        }

        return replicaServers.stream().toList();
    }

}
