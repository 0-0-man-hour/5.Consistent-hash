package com.zeromh.consistenthash.domain.model.server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@Document
public class HashServer {
    @Id
    String name;
    String url;
    String port;
    int numsOfNode;
    List<Long> hashValues;
    boolean isAlive;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashServer that = (HashServer) o;

        return name.equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
