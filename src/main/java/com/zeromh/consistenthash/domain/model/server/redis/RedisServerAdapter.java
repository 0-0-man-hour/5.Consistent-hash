package com.zeromh.consistenthash.domain.model.server.redis;

import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.domain.model.server.ServerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "server.infra", havingValue = "redis")
public class RedisServerAdapter implements ServerPort {

    private final DockerService dockerService;
    private final Map<HashServer, RedisTemplate<String, String >> redisTemplateMap = new HashMap<>();
    @Value("${server.host}")
    private String host;

    @Override
    public ServerStatus getServerStatus() {
        List<String> containers = dockerService.listRunningRedisContainers();
        if(redisTemplateMap.size() != containers.size()) {
            redisTemplateMap.clear();
            for (var container : containers) {
                String[] containerInfo = container.split(":");
                String serverName = containerInfo[0];
                int port = Integer.parseInt(containerInfo[2]);
                redisTemplateMap.put(HashServer.builder().name(serverName).build(), createRedisTemplate(host, port));
            }
        }

        return ServerStatus.builder()
                .serverList(new ArrayList<>(redisTemplateMap.keySet()))
                .serverNums(redisTemplateMap.size())
                .build();
    }

    @Override
    public void addData(HashKey key, HashServer server) {
        redisTemplateMap.get(server).opsForValue().set(key.getKey(), String.valueOf(key.getHashVal()));
    }

    @Override
    public void deleteData(HashKey key, HashServer server) {
        redisTemplateMap.get(server).delete(key.getKey());
    }

    @Override
    public HashKey getKey(HashKey key, HashServer server) {
        return HashKey.builder().key(key.getKey())
                .hashVal(Long.parseLong(redisTemplateMap.get(server).opsForValue().get(key.getKey())))
                .build();
    }

    @Override
    public ServerStatus deleteServer(HashServer server) {
        removeRedisTemplate(server);
        dockerService.stopRedis(server.getName());
        return getServerStatus();
    }

    @Override
    public ServerStatus addServer(HashServer server) {
        String serverName = server.getName();
        int port = (int) (9000 + server.getHashValues().get(0) % 1000);
        dockerService.startRedis(serverName, port);
        redisTemplateMap.put(server, createRedisTemplate(host, port));
        return getServerStatus();
    }

    @Override
    public List<HashKey> getAllServerData(HashServer server) {
        RedisTemplate<String, String> redisTemplate = redisTemplateMap.get(server);
        Set<String> keys = redisTemplate.keys("*");

        List<HashKey> hashKeys = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                hashKeys.add(HashKey.builder()
                        .key(key)
                        .hashVal(Long.parseLong(value)).build());
            }
        }

        return hashKeys;
    }

    @Override
    public void addDataList(HashServer server, List<HashKey> hashKeys) {
        Map<String, String> kv = hashKeys.stream()
                .collect(Collectors.toMap(HashKey::getKey, hashKey -> String.valueOf(hashKey.getHashVal())));
        redisTemplateMap.get(server).opsForValue().multiSet(kv);

    }

    @Override
    public void delDataList(HashServer server, List<HashKey> hashKeys) {
        redisTemplateMap.get(server).delete(hashKeys.stream().map(HashKey::getKey).toList());
    }

    private RedisTemplate<String, String> createRedisTemplate(String host, int port) {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(host, port);
        connectionFactory.afterPropertiesSet();

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    public void removeRedisTemplate(HashServer server) {
        RedisTemplate<String, String> redisTemplate = redisTemplateMap.remove(server);
        if (redisTemplate != null) {
            LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
            if (connectionFactory != null) {
                connectionFactory.destroy();
            }
        } else {
            throw new IllegalArgumentException("No Redis instance found with name: " + server.getName());
        }
    }
}
