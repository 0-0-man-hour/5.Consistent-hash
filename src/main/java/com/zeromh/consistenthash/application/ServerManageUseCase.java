package com.zeromh.consistenthash.application;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.server.HashServer;

public interface ServerManageUseCase {
    ServerStatus addServer(String serverName);
    ServerStatus deleteServer(HashServer hashServer);
}
