package com.zeromh.consistenthash.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@ToString
@Document
public class HashServer {
    @Id
    String name;

    @Setter
    List<Long> hashValues;
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
