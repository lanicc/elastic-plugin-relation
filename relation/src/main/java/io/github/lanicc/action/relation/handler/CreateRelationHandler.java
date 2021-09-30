package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.admin.BabyCreateIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.settings.Settings;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class CreateRelationHandler {

    protected ActionRunner actionRunner;

    public CreateRelationHandler(ActionRunner actionRunner) {
        this.actionRunner = actionRunner;
    }


    public void create(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        checkRelationIndexExist();
        BabyCreateIndexResponse response = new BabyCreateIndexResponse();
        CreateIndexRequest createIndexRequest = request.getCreateIndexRequest();

        Runnable createRelationTask = () -> {
            BulkRequest bulkRequest = new BulkRequest();
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(BabyCreateIndexRequest.BABY_INDIES_RELATION)
                    .id(createIndexRequest.index())
                    .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                    .source(request.getRelation().toMap());
            bulkRequest.add(indexRequest);
            actionRunner.execute(bulkRequest, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        response.setSuccess(false);
                        response.setMessage("创建relation失败");
                    } else {
                        response.setSuccess(true);
                    }
                    listener.onResponse(response);
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        };

        actionRunner.execute(createIndexRequest, new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                if (createIndexResponse.isAcknowledged()) {
                    createRelationTask.run();
                } else {
                    response.setSuccess(false);
                    response.setMessage("创建索引失败");
                    listener.onResponse(response);
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private synchronized void checkRelationIndexExist() {
        IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest();
        indicesExistsRequest.indices(BabyCreateIndexRequest.BABY_INDIES_RELATION);
        ActionFuture<IndicesExistsResponse> future = actionRunner.execute(indicesExistsRequest);
        try {
            IndicesExistsResponse indicesExistsResponse = future.get();
            if (!indicesExistsResponse.isExists()) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(BabyCreateIndexRequest.BABY_INDIES_RELATION);
                createIndexRequest.settings(Settings.EMPTY);
                createIndexRequest.mapping(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE, new HashMap<>(0));
                try {
                    ActionFuture<CreateIndexResponse> actionFuture = actionRunner.execute(createIndexRequest);
                    actionFuture.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
