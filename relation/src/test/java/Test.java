import io.github.lanicc.LanPlugin;
import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.admin.BabyCreateIndexRequestBuilder;
import io.github.lanicc.action.admin.BabyCreateIndexResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexRequestBuilder;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class Test {
    static TransportClient client = getClient();
    static String index = "lan00";

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        //createIndex();
        makeData();
        //updateByQuery();
    }

    private static void updateByQueryRelation() {
        Relation relation = new Relation();
        relation.setName("t_1");
        relation.setPrimaryKey("id");

        Relation c1 = new Relation();
        c1.setName("t_2");
        c1.setPrimaryKey("id");
        c1.setRelatedKey("column_2");
        c1.setNested(false);
    }

    private static void updateByQuery() {


        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("id", 1));
        UpdateByQueryRequestBuilder requestBuilder = UpdateByQueryAction.INSTANCE.newRequestBuilder(client);
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("column_2", 41);
        map.put("column_3", 1);
        map.put("column_4", 2);
        map.put("column_6", null);

        Map<String, Object> params = new HashMap<>();
        params.put("s", map);

        Script script = new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, "ctx._source.t_2 = params.s", params);
        requestBuilder.source(index)
                .script(script)
                .filter(boolQueryBuilder)
                .abortOnVersionConflict(false);
        BulkByScrollResponse bulkByScrollResponse = requestBuilder.get();
        System.out.println(bulkByScrollResponse.getBulkFailures());
    }

    private static void makeData() {
        BabyIndexRequestBuilder requestBuilder = new BabyIndexRequestBuilder(client);
        BabyIndexRequest request = requestBuilder.request();
        request.setIndex(index);
        request.setRelation("t2");
        Map<String, Object> map = new HashMap<>();
        map.put("id", 2);
        map.put("column_2", 4);
        map.put("column_3", 421);
        map.put("column_4", 1);
        map.put("column_6", null);
        request.setSource(map);
        //request.setType("up");
        BabyIndexResponse response = requestBuilder.get();
        System.out.println(response.isSuccess());
    }

    private static void createIndex() {
        BabyCreateIndexRequestBuilder requestBuilder = new BabyCreateIndexRequestBuilder(client);
        BabyCreateIndexRequest babyCreateIndexRequest = requestBuilder.request();
        babyCreateIndexRequest.setCreateIndexRequest(
                new CreateIndexRequest()
                        .index(index)
                        .settings(
                                Settings.builder()
                                        .put("number_of_replicas", 1)
                                        .put("number_of_shards", 1)
                                        .build()
                        )
                        .mapping(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE, mappingStr2, XContentType.JSON)
        );
        Relation relation = buildRelation();
        babyCreateIndexRequest.setRelation(relation);

        BabyCreateIndexResponse response = requestBuilder.get();

        System.out.println(response.isSuccess());
    }

    private static Relation buildRelation() {
        Relation relation = new Relation();
        relation.setName("t_1");
        relation.setPrimaryKey("id");

        Relation c1 = new Relation();
        c1.setName("t2");
        c1.setPrimaryKey("id");
        c1.setRelatedKey("id");
        c1.setNested(true);
        relation.setChildren(Collections.singletonList(c1));
        return relation;
    }

    private static TransportClient getClient() {
        Settings settings = Settings.builder()
                .put("cluster.name", "es-cluster")
                .put("xpack.security.user", "elastic:changeme")
                .build();
        TransportClient client = new PreBuiltXPackTransportClient(settings, LanPlugin.class);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getLoopbackAddress(), 9300));
        return client;
    }

    private static final String mappingStr2 = "{\n" +
            "    \"properties\": {\n" +
            "        \"c1\": {\n" +
            "            \"type\": \"integer\",\n" +
            "            \"index\": true\n" +
            "        },\n" +
            "        \"id\": {\n" +
            "            \"type\": \"keyword\",\n" +
            "            \"index\": true\n" +
            "        },\n" +
            "        \"t2\": {\n" +
            "            \"type\": \"nested\",\n" +
            "            \"properties\": {\n" +
            "                \"id\": {\n" +
            "                    \"type\": \"keyword\",\n" +
            "                    \"index\": true\n" +
            "                },\n" +
            "                \"name\": {\n" +
            "                    \"type\": \"keyword\",\n" +
            "                    \"index\": true\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}" +
            "";
    private static final String mappingStr =
            "{\n" +
                    "    \"properties\": {\n" +
                    "          \"column_2\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": true\n" +
                    "          },\n" +
                    "          \"column_3\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": false\n" +
                    "          },\n" +
                    "          \"column_4\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": false\n" +
                    "          },\n" +
                    "          \"column_5\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": false\n" +
                    "          },\n" +
                    "          \"column_6\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": false\n" +
                    "          },\n" +
                    "          \"id\": {\n" +
                    "            \"type\": \"integer\",\n" +
                    "            \"index\": true\n" +
                    "          },\n" +
                    "          \"t_2\": {\n" +
                    "            \"properties\": {\n" +
                    "              \"column_2\": {\n" +
                    "                \"type\": \"integer\"\n" +
                    "              },\n" +
                    "              \"column_3\": {\n" +
                    "                \"type\": \"integer\",\n" +
                    "                \"index\": false\n" +
                    "              },\n" +
                    "              \"column_4\": {\n" +
                    "                \"type\": \"integer\",\n" +
                    "                \"index\": false\n" +
                    "              },\n" +
                    "              \"column_5\": {\n" +
                    "                \"type\": \"integer\",\n" +
                    "                \"index\": false\n" +
                    "              },\n" +
                    "              \"column_6\": {\n" +
                    "                \"type\": \"integer\",\n" +
                    "                \"index\": false\n" +
                    "              },\n" +
                    "              \"id\": {\n" +
                    "                \"type\": \"integer\",\n" +
                    "                \"index\": true\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "}";

}
