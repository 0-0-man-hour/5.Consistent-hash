package com.zeromh.consistenthash.domain.service.hash;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;

import java.util.List;

public interface HashServicePort {


    void setServer(ServerStatus serverStatus);

    long getNodeHash(HashKey key);

    ServerStatus getServerStatus();
    HashServer getServer(HashKey key);
    ServerUpdateInfo addServerInfo(HashServer server);
    ServerUpdateInfo deleteServerInfo(HashServer server);
    List<HashServer> getReplicaServers(HashKey key, int n);

}
