package com.zeromh.consistenthash.server.adapter;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class DockerService {

    private static final String SCRIPT_PATH = "scripts/docker-redis.sh";

    public void startRedis(String containerName, int port) {
        executeDockerCommand("start", containerName, port);
    }

    public void stopRedis(String containerName) {
        executeDockerCommand("stop", containerName, -1);
    }

    private void executeDockerCommand(String command, String containerName, int port) {
        try {
            File scriptFile = new ClassPathResource(SCRIPT_PATH).getFile();
            ProcessBuilder processBuilder = new ProcessBuilder(scriptFile.getAbsolutePath(), command, containerName, String.valueOf(port));

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((reader.readLine()) != null) {
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> listRunningRedisContainers() {
        List<String> containers = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "ps", "--filter", "ancestor=redis", "--format", "{{.Names}}: {{.Ports}}");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                containers.add(parseContainerInfo(line));
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return containers;
    }

    private String parseContainerInfo(String line) {
        String[] parts = line.split(": ");
        if (parts.length >= 2) {
            String name = parts[0];
            String port = parts[1].split("->")[0];  // 포트 정보를 추출 (예: "0.0.0.0:6379")
            return name + ": " + port;
        }
        return line;
    }
}
