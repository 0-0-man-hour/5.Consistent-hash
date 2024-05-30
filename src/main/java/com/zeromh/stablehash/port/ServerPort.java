package com.zeromh.stablehash.port;

import com.zeromh.stablehash.domain.ServerStatus;
import com.zeromh.stablehash.domain.StableHashKey;
import com.zeromh.stablehash.domain.StableHashServer;

import java.util.List;

public interface ServerPort {
    ServerStatus getServerStatus();

    Boolean addData(StableHashKey key, Long serverIndex);

    Boolean deleteData(StableHashKey key, Long serverIndex);

    ServerStatus deleteServer(StableHashServer server);
    ServerStatus addServer(StableHashServer server);

    List<StableHashKey> getAllServerData(StableHashServer server);

}
