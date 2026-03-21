package com.example.system.mapper.elasticsearch;

import com.example.system.domain.question.es.QuestionES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends ElasticsearchRepository<QuestionES, Long> {

    /**
     * 按难度分页查询题目
     */
    Page<QuestionES> findByDifficulty(Integer difficulty, Pageable pageable);

    /**
     * 按标题或内容匹配，且按难度过滤，分页查询
     * 使用 IK 分词器进行中文分词匹配
     */
    @Query("{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"term\": {\n" +
            "          \"difficulty\": \"?2\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"should\": [\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"title\": {\n" +
            "            \"query\": \"?0\",\n" +
            "            \"analyzer\": \"ik_smart\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"content\": {\n" +
            "            \"query\": \"?1\",\n" +
            "            \"analyzer\": \"ik_smart\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"minimum_should_match\": 1\n" +
            "  }\n" +
            "}")
    Page<QuestionES> findByTitleOrContentAndDifficulty(String keywordTitle,
                                                       String keywordContent,
                                                       Integer difficulty,
                                                       Pageable pageable);

    /**
     * 按标题或内容匹配，分页查询
     * 使用 IK 分词器进行中文分词匹配
     */
    @Query("{\n" +
            "  \"bool\": {\n" +
            "    \"should\": [\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"title\": {\n" +
            "            \"query\": \"?0\",\n" +
            "            \"analyzer\": \"ik_smart\"\n" +
            "          }\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"content\": {\n" +
            "            \"query\": \"?1\",\n" +
            "            \"analyzer\": \"ik_smart\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"minimum_should_match\": 1\n" +
            "  }\n" +
            "}")
    Page<QuestionES> findByTitleOrContent(String keywordTitle, String keywordContent, Pageable pageable);
}
