package com.zeromh.consistenthash.interfaces.server;

import com.zeromh.consistenthash.domain.model.server.HashServer;
import lombok.Getter;

@Getter
public class ServerRequestDto {
    String serverName;
    int numsOfNodes;

    public HashServer toHashServer() {
        return HashServer.builder()
                .name(serverName)
                .numsOfNode(numsOfNodes)
                .build();
    }
}
