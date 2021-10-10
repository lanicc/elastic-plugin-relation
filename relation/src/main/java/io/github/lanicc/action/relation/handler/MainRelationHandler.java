package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import io.github.lanicc.action.relation.Relation;
import io.github.lanicc.action.relation.RelationHelper;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class MainRelationHandler extends AbstractRelationHandler {

    private static final String SCRIPT_REMOVE_KEY = "ctx._source.removeKey('%s');";

    public MainRelationHandler(ActionRunner actionRunner) {
        super(actionRunner);
    }

    @Override
    public void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) throws Exception {
        String index = request.getIndex();
        String key = primaryRelation.getPrimaryKey();
        Map<String, Object> source = request.getSource();
        Object o = source.get(key);
        if (o == null) {
            throw new IllegalArgumentException("relation " + primaryRelation.getName() + " key cannot be null");
        }
        String keyValue = String.valueOf(o);

        //parent doc update to index
        Runnable r = () -> {
            List<Relation> parentRelations = RelationHelper.parentOf(primaryRelation);
            if (!parentRelations.isEmpty()) {
                Map<String, Object> parentSources = new HashMap<>();
                for (Relation parentRelation : parentRelations) {
                    String parentRelationName = parentRelation.getName();
                    String relatedKey = parentRelation.getRelatedKey();
                    Object relatedKeyO = source.get(relatedKey);
                    if (relatedKeyO != null && !relatedKeyO.toString().isEmpty()) {
                        String relatedKeyValue = String.valueOf(relatedKeyO);
                        String parentIndex = getParentIndex(index, parentRelationName);
                        GetRequest getRequest =
                                new GetRequest()
                                        .index(parentIndex)
                                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                                        .id(relatedKeyValue);
                        ActionFuture<GetResponse> future = actionRunner.execute(getRequest);
                        try {
                            GetResponse getResponse = future.get();
                            if (getResponse.isExists()) {
                                Map<String, Object> parentSource = getResponse.getSource();
                                parentSources.put(parentRelationName, parentSource);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }
                if (!parentRelations.isEmpty()) {
                    source.putAll(parentSources);
                    UpdateRequest updateRequest =
                            new UpdateRequest()
                                    .index(index)
                                    .id(keyValue)
                                    .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                                    .doc(source)
                                    .upsert(source);
                    actionRunner.execute(updateRequest, applyToUpdateResponse(listener));
                }
            }
        };


        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(keyValue)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .doc(source)
                        .upsert(source);
        actionRunner.execute(updateRequest, new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    r.run();
                } else {
                    BabyIndexResponse babyIndexResponse = new BabyIndexResponse();
                    babyIndexResponse.setSuccess(true);
                    listener.onResponse(babyIndexResponse);
                }
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
        DeleteRequest deleteRequest =
                new DeleteRequest()
                        .index(index)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(id);
        actionRunner.execute(deleteRequest, applyToDeleteResponse(listener));
    }
}
