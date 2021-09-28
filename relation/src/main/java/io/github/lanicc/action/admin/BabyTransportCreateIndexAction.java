package io.github.lanicc.action.admin;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.indices.TransportIndicesExistsAction;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyTransportCreateIndexAction extends HandledTransportAction<BabyCreateIndexRequest, BabyCreateIndexResponse> {

    private final TransportCreateIndexAction transportCreateIndexAction;
    private final TransportBulkAction transportBulkAction;
    private final TransportIndicesExistsAction transportIndicesExistsAction;

    private static final Logger logger = ESLoggerFactory.getLogger(BabyTransportCreateIndexAction.class.getName());

    @Inject
    public BabyTransportCreateIndexAction(
            Settings settings, ThreadPool threadPool,
            TransportService transportService, ActionFilters actionFilters,
            IndexNameExpressionResolver indexNameExpressionResolver,
            TransportCreateIndexAction transportCreateIndexAction,
            TransportBulkAction transportBulkAction,
            TransportIndicesExistsAction transportIndicesExistsAction) {
        super(settings, BabyCreateIndexAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyCreateIndexRequest::new);
        this.transportCreateIndexAction = transportCreateIndexAction;
        this.transportBulkAction = transportBulkAction;
        this.transportIndicesExistsAction = transportIndicesExistsAction;
    }

    private void checkRelationIndexExist() {
        logger.info("checkRelationIndexExist");
        IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest();
        indicesExistsRequest.indices(BabyCreateIndexRequest.BABY_INDIES_RELATION);
        ActionFuture<IndicesExistsResponse> future = transportIndicesExistsAction.execute(indicesExistsRequest);
        try {
            IndicesExistsResponse indicesExistsResponse = future.get();
            logger.info("relation index exists response: {}", indicesExistsResponse.isExists());
            if (!indicesExistsResponse.isExists()) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(BabyCreateIndexRequest.BABY_INDIES_RELATION);
                createIndexRequest.settings(Settings.EMPTY);
                createIndexRequest.mapping(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE, new HashMap<>(0));
                transportCreateIndexAction.execute(createIndexRequest, new ActionListener<CreateIndexResponse>() {
                    @Override
                    public void onResponse(CreateIndexResponse createIndexResponse) {
                        logger.info("create relation index result: {}", createIndexResponse.isAcknowledged());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        logger.error("create relation index error", e);
                    }
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doExecute(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        checkRelationIndexExist();
        BabyCreateIndexResponse response = new BabyCreateIndexResponse();
        CreateIndexRequest createIndexRequest = request.getCreateIndexRequest();

        ActionFuture<CreateIndexResponse> createIndexResponseActionFuture = transportCreateIndexAction.execute(createIndexRequest);

        BulkRequest bulkRequest = new BulkRequest();
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.index(BabyCreateIndexRequest.BABY_INDIES_RELATION)
                .id(createIndexRequest.index())
                .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                .source(request.getRelation().toMap());
        bulkRequest.add(indexRequest);
        ActionFuture<BulkResponse> bulkResponseActionFuture = transportBulkAction.execute(bulkRequest);
        try {
            CreateIndexResponse createIndexResponse = createIndexResponseActionFuture.get();
            logger.info("create index response {}", createIndexResponse.isAcknowledged());
            BulkResponse bulkItemResponses = bulkResponseActionFuture.get();
            logger.info("create relation response {}", !bulkItemResponses.hasFailures());
            response.setSuccess(createIndexResponse.isAcknowledged() && !bulkItemResponses.hasFailures());
            response.setMessage((createIndexResponse.isAcknowledged() ? "" : "创建索引失败 ") + (bulkItemResponses.hasFailures() ? bulkItemResponses.buildFailureMessage() : ""));
            listener.onResponse(response);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            listener.onFailure(e);
        }
        //Runnable runnable = () -> {
        //    BulkRequest bulkRequest = new BulkRequest();
        //    IndexRequest indexRequest = new IndexRequest();
        //    indexRequest.index(BabyCreateIndexRequest.BABY_INDIES_RELATION)
        //            .id(createIndexRequest.index())
        //            .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
        //            .source(request.getRelation());
        //    bulkRequest.add(indexRequest);
        //    transportBulkAction.execute(bulkRequest, new ActionListener<BulkResponse>() {
        //        @Override
        //        public void onResponse(BulkResponse bulkItemResponses) {
        //            logger.info("bulk response has failures {}", bulkItemResponses.hasFailures());
        //            BulkItemResponse[] items = bulkItemResponses.getItems();
        //            if (items[0].isFailed()) {
        //                response.setSuccess(false);
        //                response.setMessage(items[0].getFailureMessage());
        //            } else {
        //                response.setSuccess(true);
        //            }
        //            logger.info("listener response {}", response);
        //            listener.onResponse(response);
        //        }
        //
        //        @Override
        //        public void onFailure(Exception e) {
        //            logger.error("index {} relation failed", createIndexRequest.index(), e);
        //            listener.onFailure(e);
        //        }
        //    });
        //};
        //
        //transportCreateIndexAction.execute(createIndexRequest, new ActionListener<CreateIndexResponse>() {
        //    @Override
        //    public void onResponse(CreateIndexResponse createIndexResponse) {
        //        logger.info("create index {} response {}", createIndexRequest.index(), createIndexResponse.isAcknowledged());
        //        response.setSuccess(createIndexResponse.isAcknowledged());
        //        if (createIndexResponse.isAcknowledged()) {
        //            logger.info("start index relation for index {}", createIndexRequest.index());
        //            runnable.run();
        //        } else {
        //            logger.error("create index {} failed", createIndexRequest.index());
        //            response.setMessage("创建索引失败");
        //            listener.onResponse(response);
        //        }
        //    }
        //
        //    @Override
        //    public void onFailure(Exception e) {
        //        logger.error("create index {} failed", createIndexRequest.index(), e);
        //        listener.onFailure(e);
        //    }
        //});
    }

}
