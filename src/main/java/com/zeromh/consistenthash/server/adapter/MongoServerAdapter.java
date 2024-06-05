package com.zeromh.consistenthash.server.adapter;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.server.port.out.ServerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoServerAdapter implements ServerPort {
    private final MongoTemplate mongoTemplate;
    private static final String SERVER_STATUS = "Sever_Status";

    @Override
    public ServerStatus getServerStatus() {
        List<HashServer> serverList = mongoTemplate.findAll(HashServer.class, SERVER_STATUS);

        return ServerStatus.builder()
                .serverNums(serverList.size())
                .serverList(serverList)
                .build();
    }

    @Override
    public void addData(HashKey key, HashServer server) {
        mongoTemplate.insert(key, getCollection(server));
    }

    @Override
    public void deleteData(HashKey key, HashServer server) {
        mongoTemplate.remove(key, getCollection(server));
    }

    @Override
    public HashKey getKey(HashKey key, HashServer server) {
        return mongoTemplate.findOne(
                new Query(Criteria.where("_id").is(key.getKey())),
                HashKey.class,
                getCollection(server)
        );
    }

    @Override
    public ServerStatus deleteServer(HashServer server) {
        mongoTemplate.dropCollection(getCollection(server));
        mongoTemplate.remove(server, SERVER_STATUS);

        return getServerStatus();
    }

    @Override
    public ServerStatus addServer(HashServer server) {
        mongoTemplate.createCollection(getCollection(server));
        mongoTemplate.insert(server, SERVER_STATUS);

        return getServerStatus();
    }

    @Override
    public List<HashKey> getAllServerData(HashServer server) {
        return mongoTemplate.findAll(HashKey.class, getCollection(server));
    }

    @Override
    public void addDataList(HashServer server, List<HashKey> hashKeys) {
        mongoTemplate.insert(hashKeys, getCollection(server));
    }

    @Override
    public void delDataList(HashServer server, List<HashKey> hashKeys) {
        List<String> ids = hashKeys.stream().map(HashKey::getKey).toList();
        Query query = new Query(Criteria.where("_id").in(ids));
        mongoTemplate.remove(query, getCollection(server));
    }

    private String getCollection(HashServer server) {
        return "Server"+server.getName();
    }
}
