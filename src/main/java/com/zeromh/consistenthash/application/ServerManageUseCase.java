package com.zeromh.consistenthash.application;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;

public interface ServerManageUseCase {
    ServerStatus addServer(HashServer hashServer);
    ServerStatus deleteServer(HashServer hashServer);
    HashServer getServer(HashKey key);
}
