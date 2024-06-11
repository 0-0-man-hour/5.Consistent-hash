package com.zeromh.consistenthash.domain.service.hash;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.application.dto.ServerUpdateInfo;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;

public interface HashServicePort {


    void setServer(ServerStatus serverStatus);

    HashServer getServer(HashKey key);
    ServerUpdateInfo addServerInfo(ServerStatus serverStatus, String serverName);
    ServerUpdateInfo deleteServerInfo(ServerStatus serverStatus, HashServer server);

}
