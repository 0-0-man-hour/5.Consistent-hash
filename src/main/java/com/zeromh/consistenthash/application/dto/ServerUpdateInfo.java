package com.zeromh.consistenthash.application.dto;

import com.zeromh.consistenthash.domain.model.server.HashServer;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ServerUpdateInfo {

    HashServer newServer;
    List<HashServer> rehashServer;
}
