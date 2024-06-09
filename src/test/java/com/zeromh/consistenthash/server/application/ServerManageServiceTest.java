package com.zeromh.consistenthash.server.application;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.hash.application.KeyManageService;
import com.zeromh.consistenthash.hash.dto.KeyServerDto;
import com.zeromh.consistenthash.server.port.out.ServerPort;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest
class ServerManageServiceTest {

    @Autowired
    private ServerManageService serverManageService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KeyManageService keyManageService;
    @Autowired
    private ServerPort serverPort;

//    @BeforeEach
    void setData() {
        for (var collection : mongoTemplate.getCollectionNames()) {
            mongoTemplate.dropCollection(collection);
        }

    }

    @Test
    void deleteServer() throws InterruptedException {
        serverManageService.addServer(null);
        Thread.sleep(1500);
        serverManageService.addServer(null);
        Thread.sleep(1500);
        ServerStatus serverStatus = serverManageService.addServer(null);

        Assertions.assertThat(serverStatus.getServerNums()).isEqualTo(3);

        for (int i = 0; i < 100; i++) {
            keyManageService.addKey(HashKey.builder()
                    .key("key"+i).build());
        }

        serverManageService.deleteServer(serverStatus.getServerList().get(0));
    }

    @Test
    void addServer() throws InterruptedException {
        serverManageService.addServer(null);
        Thread.sleep(1500);
        ServerStatus serverStatus = serverManageService.addServer(null);


        Assertions.assertThat(serverStatus.getServerNums()).isEqualTo(2);

        for (int i = 0; i < 100; i++) {
            keyManageService.addKey(HashKey.builder()
                    .key("key"+i).build());
        }

        Thread.sleep(1500);
        serverManageService.addServer(null);
    }

    @Test
    void serverRehashTestByCustom() throws InterruptedException {
        serverManageService.addServer(null);
        Thread.sleep(1500);
        serverManageService.addServer(null);
        Thread.sleep(1500);
        ServerStatus serverStatus = serverManageService.addServer(null);

        for (int i = 0; i < 100; i++) {
            keyManageService.addKey(HashKey.builder()
                    .key(String.valueOf(i)).build());
        }

        for (var server : serverStatus.getServerList()) {
           log.info(server.getName()+": " + server.getHashValues()+", count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments());
        }
    }

    @Test
    void server_distribute_test() {
        int serverNums = 5;
        ServerStatus serverStatus = null;
        for (int i = 0; i < serverNums; i++) {
            serverStatus = serverManageService.addServer("server_"+i);
        }

        for (int i = 0; i < 100; i++) {
            if(i % 10000 == 0) {
                log.info(i + " completed");
            }
            keyManageService.addKey(HashKey.builder()
                    .key(String.valueOf(i)).build());
        }

//        serverManageService.addServer("server_"+4);

        for (var server : serverStatus.getServerList()) {
            log.info(server.getName()+": " + server.getHashValues()+", count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments());
        }
    }


    @Test
    void cache_hit_test() {
        int serverNums = 4;
        ServerStatus serverStatus = serverPort.getServerStatus();

        if (serverStatus.getServerNums() == 0) {
            for (int i = 0; i < serverNums; i++) {
                serverStatus = serverManageService.addServer("server_"+i);
            }
            for (int i = 0; i < 1000000; i++) {
                keyManageService.addKey(HashKey.builder()
                        .key(String.valueOf(i)).build());
            }
        }



        Map<HashServer, Integer> serverHit = new HashMap<>();
        Map<HashServer, Integer> requestCount = new HashMap<>();


        for (int i = 0; i < serverStatus.getServerNums(); i++) {
            serverHit.put(serverStatus.getServerList().get(i), 0);
            requestCount.put(serverStatus.getServerList().get(i), 0);
        }
        for (int i = 0; i < 1000000; i++) {
            if(i % 10000 == 0) {
                log.info(i + " completed");
            }
            KeyServerDto keyServerDto = keyManageService.getKey(HashKey.builder()
                    .key(String.valueOf(i)).build());
            requestCount.put(keyServerDto.getServer(), requestCount.get(keyServerDto.getServer()) + 1);
            if (keyServerDto.getHashKey() != null) {
                serverHit.put(keyServerDto.getServer(), serverHit.get(keyServerDto.getServer()) + 1);
            }
        }

        for (var key : serverHit.keySet()) {
            log.info(key.getName()+ ": " + serverHit.get(key) +", request: "+requestCount.get(key));
        }
    }


    @Test
    void serverAddAndRemove_rehash_test() {
        setData();
        int serverNums = 4;
        ServerStatus serverStatus = null;
        for (int i = 0; i < serverNums; i++) {
            serverStatus = serverManageService.addServer("server_"+i);
        }

        for (int i = 0; i < 1000000; i++) {
            if(i % 10000 == 0) {
                log.info(i + " completed");
            }
            keyManageService.addKey(HashKey.builder()
                    .key(String.valueOf(i)).build());
        }

        String new_server = "server_4";

        long addStart = System.currentTimeMillis();
        serverStatus = serverManageService.addServer(new_server);

        log.info("add rehash work time: {}", System.currentTimeMillis() - addStart);
        for (var server : serverStatus.getServerList()) {
            log.info(server.getName()+": " + server.getHashValues()+", count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments());
        }

        long removeStart = System.currentTimeMillis();
        serverStatus = serverManageService.deleteServer(HashServer.builder()
                .name(new_server).build());

        log.info("remove rehash work time: {}", System.currentTimeMillis() - removeStart);
        for (var server : serverStatus.getServerList()) {
            log.info(server.getName()+": " + server.getHashValues()+", count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments());
        }


    }

    @Test
    void serverAdd_rehash_test() {
        long start = System.currentTimeMillis();
        ServerStatus serverStatus = serverManageService.addServer("server_"+4);

        log.info("add rehash work time: {}", System.currentTimeMillis() - start);
        for (var server : serverStatus.getServerList()) {
            log.info(server.getName()+": count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments() + " hashValues : "+ server.getHashValues());
        }
    }

    @Test
    void serverRemove_rehash_test() {
        long start = System.currentTimeMillis();

        ServerStatus serverStatus = serverManageService.deleteServer(HashServer.builder()
                .name("server_"+4).build());

        log.info("rehash work time: {}", System.currentTimeMillis() - start);
        for (var server : serverStatus.getServerList()) {
            log.info(server.getName()+": count: " +  mongoTemplate.getCollection("Server"+server.getName()).countDocuments() + " hashValues : "+ server.getHashValues());
        }
    }
}