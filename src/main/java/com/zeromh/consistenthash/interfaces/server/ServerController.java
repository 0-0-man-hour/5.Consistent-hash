package com.zeromh.consistenthash.interfaces.server;

import com.zeromh.consistenthash.application.dto.ServerStatus;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.application.ServerManageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consistenthash/server")
@RequiredArgsConstructor
public class ServerController {
    private final ServerManageUseCase serverManageUseCase;

    @PostMapping
    public ServerStatus addServer(@RequestBody ServerRequestDto requestDto) {
        return serverManageUseCase.addServer(requestDto.toHashServer());
    }

    @DeleteMapping
    public ServerStatus delServer(@RequestBody ServerRequestDto requestDto) {
        return serverManageUseCase.deleteServer(HashServer.builder()
                .name(requestDto.getServerName())
                .build());
    }
}
