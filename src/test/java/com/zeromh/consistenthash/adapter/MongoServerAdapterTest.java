package com.zeromh.consistenthash.adapter;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.server.adapter.MongoServerAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MongoServerAdapterTest {

    @Autowired
    private MongoServerAdapter mongoServerAdapter;
    @Autowired
    private MongoTemplate mongoTemplate;
    private static final String SERVER_STATUS = "Sever_Status";


    @Test
    void getServerStatus() {
        HashServer server = HashServer.builder()
                .name("server1")
                .hashValues(List.of(10L))
                .build();
        mongoTemplate.insert(server, SERVER_STATUS);

        ServerStatus serverStatus = mongoServerAdapter.getServerStatus();

        assertThat(serverStatus.getServerNums()).isEqualTo(1);
        assertThat(serverStatus.getServerList().get(0).getName()).isEqualTo("server1");

    }

    @Test
    void addData() {


    }

    @Test
    void deleteData() {
    }

    @Test
    void deleteServer() {
    }

    @Test
    void addServer() {
    }

    @Test
    void getAllServerData() {
    }

    @Test
    void addDataList() {
    }

    @Test
    void delDataList() {
    }
}