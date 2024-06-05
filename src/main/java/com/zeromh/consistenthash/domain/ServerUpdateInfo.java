package com.zeromh.consistenthash.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ServerUpdateInfo {

    HashServer newServer;
    List<HashServer> rehashServer;
}
