package com.zeromh.consistenthash.server.port.out;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;

import java.util.List;

public interface ServerPort {
    ServerStatus getServerStatus();

    void addData(HashKey key, HashServer server);
    void deleteData(HashKey key, HashServer server);
    HashKey getKey(HashKey key, HashServer server);

    ServerStatus deleteServer(HashServer server);
    ServerStatus addServer(HashServer server);

    List<HashKey> getAllServerData(HashServer server);
    void addDataList(HashServer server, List<HashKey> hashKeys);
    void delDataList(HashServer server, List<HashKey> hashKeys);


}
