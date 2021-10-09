package io.github.lanicc;

import io.github.lanicc.action.admin.*;
import io.github.lanicc.action.index.BabyIndexAction;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexRequestBuilder;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2021/9/30.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyRelationTests extends BaseTests {

    private static final String index = "lanicc";
    @Test
    void createIndex() {
        //CreateIndexRequest
        CreateIndexRequest createIndexRequest = new CreateIndexRequest();
        createIndexRequest.index(index);
        createIndexRequest.mapping(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE, mapping1, XContentType.JSON);

        //relation
        Relation relation = new Relation();
        relation.setName("a");
        relation.setPrimaryKey("a_id");
        Relation bRelation = new Relation();
        bRelation.setName("b");
        bRelation.setPrimaryKey("b_id");
        bRelation.setForeignKey("b_id");
        bRelation.setRelatedKey("a_id");
        bRelation.setNested(false);
        Relation cRelation = new Relation();
        cRelation.setName("c");
        cRelation.setPrimaryKey("c_id");
        cRelation.setForeignKey("c_x");
        cRelation.setRelatedKey("a_id");
        cRelation.setNested(true);
        Relation dRelation = new Relation();
        dRelation.setName("d");
        dRelation.setPrimaryKey("d_id");
        dRelation.setForeignKey("d_id");
        dRelation.setRelatedKey("a_x");
        dRelation.setNested(false);

        relation.setChildren(Arrays.asList(bRelation, cRelation, dRelation));

        BabyCreateIndexRequestBuilder requestBuilder = BabyCreateIndexAction.INSTANCE.newRequestBuilder(client);
        BabyCreateIndexRequest request = requestBuilder.request();
        request.setCreateIndexRequest(createIndexRequest);
        request.setRelation(relation);
        BabyCreateIndexResponse response = requestBuilder.get();
        System.out.println(response.isSuccess());
    }

    @Test
    void indexMain() {
        Map<String, Object> source = new HashMap<>();
        source.put("a_id", "1");
        source.put("a_x", "2");
        BabyIndexRequestBuilder requestBuilder = BabyIndexAction.INSTANCE.newRequestBuilder(client);
        BabyIndexRequest request = requestBuilder.request();
        request.setIndex(index);
        request.setRelation("a");
        request.setSource(source);
        BabyIndexResponse response = requestBuilder.get();

        System.out.println(response.isSuccess());
    }

    private static final String mapping1 =
            "{\n" +
                    "    \"properties\": {\n" +
                    "        \"a_id\": {\n" +
                    "            \"type\": \"keyword\",\n" +
                    "            \"index\": true\n" +
                    "        },\n" +
                    "        \"a_x\": {\n" +
                    "            \"type\": \"keyword\",\n" +
                    "            \"index\": true\n" +
                    "        },\n" +
                    "        \"b\": {\n" +
                    "            \"properties\": {\n" +
                    "                \"b_id\": {\n" +
                    "                    \"type\": \"keyword\",\n" +
                    "                    \"index\": true\n" +
                    "                },\n" +
                    "                \"b_x\": {\n" +
                    "                    \"type\": \"keyword\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"c\": {\n" +
                    "            \"type\": \"nested\",\n" +
                    "            \"properties\": {\n" +
                    "                \"c_id\": {\n" +
                    "                    \"type\": \"keyword\"\n" +
                    "                },\n" +
                    "                \"c_x\": {\n" +
                    "                    \"type\": \"keyword\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"d\": {\n" +
                    "            \"properties\": {\n" +
                    "                \"d_id\": {\n" +
                    "                    \"type\": \"keyword\"\n" +
                    "                },\n" +
                    "                \"d_x\": {\n" +
                    "                    \"type\": \"keyword\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
}
