package com.zeromh.consistenthash.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@ToString
@Document
public class HashServer {
    @Id
    String name;
    @Setter
    List<Long> hashValues;
}
