package com.zeromh.consistenthash.hash.presentation;

import com.zeromh.consistenthash.domain.HashKey;
import com.zeromh.consistenthash.domain.HashServer;
import com.zeromh.consistenthash.hash.dto.HashKeyRequestDto;
import com.zeromh.consistenthash.hash.dto.KeyServerDto;
import com.zeromh.consistenthash.hash.port.in.KeyManageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KeyController {

    private final KeyManageUseCase keyManageUseCase;

    @PostMapping(path = "/key")
    public HashServer addKey(@RequestBody HashKeyRequestDto requestDto) {
        return keyManageUseCase.addKey(HashKey.builder()
                .key(requestDto.getKey()).build());
    }

    @GetMapping("/key/{key}")
    public KeyServerDto getKey(@PathVariable String key) {
        return keyManageUseCase.getKey(HashKey.builder()
                .key(key).build());
    }
}
