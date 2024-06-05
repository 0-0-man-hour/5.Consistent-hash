package com.zeromh.consistenthash.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class ServerStatus {
    private Integer serverNums;
    private List<HashServer> serverList;

}
