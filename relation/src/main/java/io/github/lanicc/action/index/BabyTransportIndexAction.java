package io.github.lanicc.action.index;

import com.google.common.collect.Maps;
import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.exception.RelationNotFoundException;
import io.github.lanicc.action.relation.Relation;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.TransportGetAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.action.update.TransportUpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyTransportIndexAction extends HandledTransportAction<BabyIndexRequest, BabyIndexResponse> {
    private final TransportGetAction transportGetAction;
    private final TransportUpdateAction transportUpdateAction;
    private final TransportUpdateByQueryAction transportUpdateByQueryAction;


    private static final String SCRIPT_UPDATE_1_N =
            "        if (ctx._source.%s != null)\n" +
                    "            ctx._source.%s.removeIf(item -> String.valueOf(item.%s)=='%s');\n" +
                    "        if (ctx._source.%s == null) \n" +
                    "            ctx._source.%s=[]; \n" +
                    "        ctx._source.%s.add(params.p);";

    @Inject
    public BabyTransportIndexAction(
            Settings settings, ThreadPool threadPool,
            Client client,
            ClusterService clusterService,
            TransportService transportService, ActionFilters actionFilters,
            IndexNameExpressionResolver indexNameExpressionResolver,
            TransportUpdateAction transportUpdateAction,
            TransportGetAction transportGetAction
    ) {
        super(settings, BabyIndexAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyIndexRequest::new);
        this.transportGetAction = transportGetAction;
        this.transportUpdateAction = transportUpdateAction;
        this.transportUpdateByQueryAction = new TransportUpdateByQueryAction(settings, threadPool, actionFilters, indexNameExpressionResolver, client, transportService, clusterService);
    }

    @Override
    protected void doExecute(BabyIndexRequest request, ActionListener<BabyIndexResponse> listener) {
        String index = request.getIndex();
        Relation primaryRelation = getRelationByIndex(index);
        logger.info("index {}'s relation is {}", index, primaryRelation);
        if (primaryRelation == null) {
            listener.onFailure(new IndexNotFoundException(index + " not found from " + BabyCreateIndexRequest.BABY_INDIES_RELATION));
        }
        String relationName = request.getRelation();
        String name = primaryRelation.getName();
        Map<String, Object> source = request.getSource();

        if (Objects.equals(name, relationName)) {
            logger.info("primary relation index");
            indexMain(index, primaryRelation, source, listener);
            return;
        }
        List<Relation> relates = primaryRelation.getChildren();
        Optional<Relation> relationOptional = relates.stream()
                .filter(r -> Objects.equals(r.getName(), relationName))
                .findAny();
        if (!relationOptional.isPresent()) {
            listener.onFailure(new RelationNotFoundException(relationName + " not found from index " + index));
            return;
        }
        Relation relation = relationOptional.get();
        logger.info("relate relation index");
        indexRelate(index, primaryRelation, relation, source, listener);
    }


    private void indexMain(String index, Relation relation, Map<String, Object> source, ActionListener<BabyIndexResponse> listener) {
        String key = relation.getPrimaryKey();
        Object o = source.get(key);
        if (o == null) {
            throw new IllegalArgumentException("relation " + relation.getName() + " key cannot be null");
        }
        String keyValue = String.valueOf(o);
        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(keyValue)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .doc(source)
                        .upsert(source);
        transportUpdateAction.execute(updateRequest, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                BabyIndexResponse babyIndexResponse = new BabyIndexResponse();
                babyIndexResponse.setSuccess(true);
                listener.onResponse(babyIndexResponse);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private void indexRelate(String index, Relation primaryRelation, Relation relation, Map<String, Object> source, ActionListener<BabyIndexResponse> listener) {
        String relationPrimaryKey = relation.getPrimaryKey();
        Object o = source.get(relationPrimaryKey);
        String name = relation.getName();

        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " primary key " + relationPrimaryKey + " cannot be null");
        }
        String relationPrimaryValue = String.valueOf(o);

        String relateKey = relation.getPrimaryKey();
        String primaryKey = primaryRelation.getPrimaryKey();



        if (Objects.equals(primaryKey, relateKey)) {
            UpdateRequest updateRequest;

            if (relation.isNested()) {
                logger.info("relation {} is nested", name);
                Map<String, Object> params = new HashMap<>();
                params.put("p", source);
                Script script =
                        new Script(
                                ScriptType.INLINE,
                                DEFAULT_SCRIPT_LANG,
                                String.format(SCRIPT_UPDATE_1_N, name, name, relateKey, relationPrimaryValue, name, name, name),
                                params
                        );

                logger.info("script is {}", script.toString());

                Map<String, Object> upsertMap = new HashMap<>();
                upsertMap.put(name, Collections.singletonList(source));

                updateRequest =
                        new UpdateRequest()
                                .index(index)
                                .id(relationPrimaryValue)
                                .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                                .script(script)
                                .upsert(upsertMap);
            } else {
                logger.info("relation {} is 1 - 1", name);
                Map<String, Object> map = Maps.newHashMap();
                map.put(name, source);
                //1-1
                updateRequest =
                        new UpdateRequest()
                                .index(index)
                                .id(relationPrimaryValue)
                                .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                                .doc(map)
                                .upsert(map);
            }
            transportUpdateAction.execute(updateRequest, new ActionListener<UpdateResponse>() {
                @Override
                public void onResponse(UpdateResponse updateResponse) {
                    BabyIndexResponse babyIndexResponse = new BabyIndexResponse();
                    babyIndexResponse.setSuccess(true);
                    listener.onResponse(babyIndexResponse);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        } else {
            //n-1
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery(relateKey, relationPrimaryValue));
            UpdateByQueryRequest updateByQueryRequest =
                    new UpdateByQueryRequest(
                            new SearchRequest(index)
                                    .source(new SearchSourceBuilder().query(boolQueryBuilder)
                                    )
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("p", source);
            Script script = new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, String.format("ctx._source.%s = params.p", name), params);
            updateByQueryRequest.setScript(script);
            transportUpdateByQueryAction.execute(updateByQueryRequest, new ActionListener<BulkByScrollResponse>() {
                @Override
                public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                    BabyIndexResponse babyIndexResponse = new BabyIndexResponse();
                    babyIndexResponse.setSuccess(true);
                    listener.onResponse(babyIndexResponse);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        }

    }

    private Relation getRelationByIndex(String index) {
        GetRequest getRequest =
                new GetRequest()
                        .index(BabyCreateIndexRequest.BABY_INDIES_RELATION)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(index);
        ActionFuture<GetResponse> future = transportGetAction.execute(getRequest);
        try {
            GetResponse getResponse = future.get();
            return Relation.fromMap(getResponse.getSource());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }
}
