package com.zeromh.consistenthash.server.application;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.hash.application.KeyManageService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
class ServerManageServiceTest {

    @Autowired
    private ServerManageService serverManageService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KeyManageService keyManageService;

    @BeforeEach
    void setData() {
        for (var collection : mongoTemplate.getCollectionNames()) {
            mongoTemplate.dropCollection(collection);
        }

    }

    @Test
    void deleteServer() throws InterruptedException {
        serverManageService.addServer();
        Thread.sleep(1500);
        serverManageService.addServer();
        Thread.sleep(1500);
        ServerStatus serverStatus = serverManageService.addServer();

        Assertions.assertThat(serverStatus.getServerNums()).isEqualTo(3);

        for (int i = 0; i < 100; i++) {
            keyManageService.addKey(HashKey.builder()
                    .key("key"+i).build());
        }

        serverManageService.deleteServer(serverStatus.getServerList().get(0));
    }

    @Test
    void addServer() throws InterruptedException {
        serverManageService.addServer();
        Thread.sleep(1500);
        ServerStatus serverStatus = serverManageService.addServer();


        Assertions.assertThat(serverStatus.getServerNums()).isEqualTo(2);

        for (int i = 0; i < 100; i++) {
            keyManageService.addKey(HashKey.builder()
                    .key("key"+i).build());
        }

        Thread.sleep(1500);
        serverManageService.addServer();
    }
}