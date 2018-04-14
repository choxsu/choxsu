package com.choxsu.elastic.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "book",type = "book" , shards = 1, replicas = 0, refreshInterval = "-1")
public class Book {

    @Id
    private String id;
    private String name;
    private Long price;
    @Version
    private Long version;

	@Field(type = FieldType.Nested)
	private Map<Integer, Collection<String>> buckets = new HashMap();

    public Book(String id, String name, Long version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

}
