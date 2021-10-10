package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class ManyOneRelationHandler extends AbstractRelationHandler {

    private static final String SCRIPT_INDEX = "ctx._source.%s = params.p";
    private static final String SCRIPT_DELETE = "ctx._source.remove('%s')";

    public ManyOneRelationHandler(ActionRunner actionRunner) {
        super(actionRunner);
    }

    @Override
    public void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) {
        String index = request.getIndex();
        String name = hitRelation.getName();
        Map<String, Object> source = request.getSource();
        String relationPrimaryKey = hitRelation.getPrimaryKey();
        Object o = source.get(relationPrimaryKey);
        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " primary key " + relationPrimaryKey + " cannot be null");
        }
        String relationPrimaryKeyValue = String.valueOf(o);

        String relationForeignKey = hitRelation.getForeignKey();
        o = source.get(relationForeignKey);

        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " foreign key " + relationForeignKey + " cannot be null");
        }
        String relationForeignKeyValue = String.valueOf(o);

        String relateKey = hitRelation.getRelatedKey();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery(relateKey, relationForeignKeyValue));
        UpdateByQueryRequest updateByQueryRequest =
                new UpdateByQueryRequest(new SearchRequest(index).source(new SearchSourceBuilder().query(boolQueryBuilder)));
        Map<String, Object> params = new HashMap<>();
        params.put("p", source);
        Script script = new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, String.format(SCRIPT_INDEX, name), params);
        updateByQueryRequest.setScript(script);

        Runnable r = () -> actionRunner.execute(updateByQueryRequest, applyToBulkByScrollResponse(listener));

        IndexRequest indexRequest =
                new IndexRequest()
                        .index(getParentIndex(index, name))
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(relationPrimaryKeyValue)
                        .source(source);
        actionRunner.execute(indexRequest, new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                r.run();
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    @Override
    public void delete(BabyDeleteRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyDeleteResponse> listener) {
        String index = request.getIndex();
        String id = request.getId();
        String primaryKey = request.getPrimaryKey();
        String relationForeignKey = hitRelation.getForeignKey();
        String name = hitRelation.getName();
        if (id != null && !id.isEmpty()) {
            Script script = new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, String.format(SCRIPT_DELETE, name), Collections.emptyMap());

            UpdateRequest updateRequest =
                    new UpdateRequest()
                            .index(index)
                            .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                            .id(id)
                            .script(script);
            actionRunner.execute(updateRequest, applyDeleteToUpdateResponse(listener));
        } else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.termQuery(relationForeignKey, primaryKey));
            Script script = new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, String.format(SCRIPT_DELETE, name), Collections.emptyMap());
            UpdateByQueryRequest updateByQueryRequest =
                    new UpdateByQueryRequest(new SearchRequest(index)
                            .source(new SearchSourceBuilder().query(boolQueryBuilder)))
                            .setSlices(2)
                            .setScript(script);
            Runnable r = () -> actionRunner.execute(updateByQueryRequest, applyDeleteToBulkByScrollResponse(listener));

            DeleteRequest deleteRequest =
                    new DeleteRequest()
                            .index(getParentIndex(index, name))
                            .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                            .id(primaryKey);
            actionRunner.execute(deleteRequest, new ActionListener<DeleteResponse>() {
                @Override
                public void onResponse(DeleteResponse deleteResponse) {
                    if (deleteResponse.getResult().getOp() > 1) {
                        r.run();
                    } else {
                        BabyDeleteResponse response = new BabyDeleteResponse();
                        response.setSuccess(false);
                        listener.onResponse(response);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        }
    }
}
