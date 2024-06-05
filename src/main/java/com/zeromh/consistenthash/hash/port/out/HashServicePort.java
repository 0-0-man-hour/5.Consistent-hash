package com.zeromh.consistenthash.hash.port.out;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.ServerUpdateInfo;
import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;

public interface HashServicePort {


    void setServer(ServerStatus serverStatus);

    HashServer getServer(HashKey key);
    ServerUpdateInfo addServerInfo(ServerStatus serverStatus);
    ServerUpdateInfo deleteServerInfo(ServerStatus serverStatus, HashServer server);

}
