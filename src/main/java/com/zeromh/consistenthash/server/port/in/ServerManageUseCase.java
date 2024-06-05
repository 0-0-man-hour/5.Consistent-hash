package com.zeromh.consistenthash.server.port.in;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashServer;

public interface ServerManageUseCase {
    ServerStatus addServer();
    ServerStatus deleteServer(HashServer hashServer);
}
