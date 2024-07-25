package com.zeromh.consistenthash.domain.service.hash;

import com.zeromh.consistenthash.application.dto.HashServerDto;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;

import java.util.List;

public interface HashServicePort {


    void setServer(List<HashServer> serverList);

    List<Long> getServerHashes(HashServer hashServer);

    long getNodeHash(HashKey key);

    ServerStatus getServerStatus();
    HashServer getServer(HashKey key);
    ServerUpdateInfo addServerInfo(HashServer server);
    ServerUpdateInfo deleteServerInfo(HashServer server);
    List<HashServer> getServers(HashKey key, int n);

    List<HashServerDto> getAliveServers(HashKey key, int n);

    List<HashServer> getServersFromHash(Long hash, int n);
}
