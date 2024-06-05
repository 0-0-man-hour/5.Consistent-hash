package com.zeromh.consistenthash.server.presentation;

import com.zeromh.consistenthash.domain.ServerStatus;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.server.dto.ServerRequestDto;
import com.zeromh.consistenthash.server.port.in.ServerManageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServerController {
    private final ServerManageUseCase serverManageUseCase;

    @PostMapping(path = "/server")
    public ServerStatus addServer() {
        return serverManageUseCase.addServer();
    }

    @DeleteMapping(path = "/server")
    public ServerStatus delServer(@RequestBody ServerRequestDto requestDto) {
        return serverManageUseCase.deleteServer(HashServer.builder()
                .name(requestDto.getServerName())
                .build());
    }
}
