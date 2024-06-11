package com.zeromh.consistenthash.interfaces.key;

import com.zeromh.consistenthash.domain.model.key.HashKey;
import com.zeromh.consistenthash.domain.model.server.HashServer;
import com.zeromh.consistenthash.application.dto.KeyServerDto;
import com.zeromh.consistenthash.application.KeyManageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consistenthash/key")
@RequiredArgsConstructor
public class KeyController {

    private final KeyManageUseCase keyManageUseCase;

    @PostMapping
    public HashServer addKey(@RequestBody HashKeyRequestDto requestDto) {
        return keyManageUseCase.addKey(HashKey.builder()
                .key(requestDto.getKey()).build());
    }

    @GetMapping("/{key}")
    public KeyServerDto getKey(@PathVariable String key) {
        return keyManageUseCase.getKey(HashKey.builder()
                .key(key).build());
    }
}
