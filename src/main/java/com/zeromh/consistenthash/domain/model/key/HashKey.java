package com.zeromh.consistenthash.domain.model.key;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Document
public class HashKey {
    @Id
    private String key;
    @Setter
    long hashVal;

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
