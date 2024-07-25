package com.zeromh.consistenthash.adapter;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.service.hash.impl.ModularHashAdapter;
import com.zeromh.consistenthash.domain.service.hash.function.HashFunction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;


@SpringBootTest
class ModularHashAdapterTest {

    @Autowired
    private ModularHashAdapter modularHashAdapter;
    @Autowired
    private HashFunction hashFunction;

    @Test
    void getIndex() {
        int serverNum = 3;
        List<HashServer> serverList = IntStream.range(0, serverNum)
                .mapToObj(i -> HashServer.builder()
                        .name("server"+i)
                        .build()).toList();

        ServerStatus serverStatus = ServerStatus.builder().serverNums(serverNum)
                .serverList(serverList)
                .build();

        modularHashAdapter.setServer(serverList);

        String key = "test";
        int serverIndex = (int) (hashFunction.hash("test") % serverNum);
        HashKey hashKey = HashKey.builder()
                .key("test")
                .build();

        Assertions.assertThat(modularHashAdapter.getServer(hashKey))
                .isEqualTo(serverList.get(serverIndex));

    }
}