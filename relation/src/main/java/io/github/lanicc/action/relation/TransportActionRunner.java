package io.github.lanicc.action.relation;

import io.github.lanicc.action.index.TransportUpdateByQueryAction;
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
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.TransportGetAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.TransportUpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class TransportActionRunner implements ActionRunner {

    private final TransportGetAction transportGetAction;
    private final TransportUpdateAction transportUpdateAction;
    private final TransportUpdateByQueryAction transportUpdateByQueryAction;
    private final TransportCreateIndexAction transportCreateIndexAction;
    private final TransportBulkAction transportBulkAction;
    private final TransportIndicesExistsAction transportIndicesExistsAction;

    public TransportActionRunner(
            TransportGetAction transportGetAction,
            TransportUpdateAction transportUpdateAction,
            TransportUpdateByQueryAction transportUpdateByQueryAction,
            TransportCreateIndexAction transportCreateIndexAction,
            TransportBulkAction transportBulkAction,
            TransportIndicesExistsAction transportIndicesExistsAction
    ) {
        this.transportGetAction = transportGetAction;
        this.transportUpdateAction = transportUpdateAction;
        this.transportUpdateByQueryAction = transportUpdateByQueryAction;
        this.transportCreateIndexAction = transportCreateIndexAction;
        this.transportBulkAction = transportBulkAction;
        this.transportIndicesExistsAction = transportIndicesExistsAction;
    }

    public void execute(UpdateByQueryRequest request, ActionListener<BulkByScrollResponse> listener) {
        transportUpdateByQueryAction.execute(request, listener);
    }

    @Override
    public void execute(IndexRequest request, ActionListener<IndexResponse> listener) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(request);
        transportBulkAction.execute(bulkRequest, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                listener.onResponse(bulkItemResponses.getItems()[0].getResponse());
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    public void execute(UpdateRequest request, ActionListener<UpdateResponse> listener) {
        transportUpdateAction.execute(request, listener);
    }

    public void execute(CreateIndexRequest request, ActionListener<CreateIndexResponse> listener) {
        transportCreateIndexAction.execute(request, listener);
    }

    public void execute(BulkRequest request, ActionListener<BulkResponse> listener) {
        transportBulkAction.execute(request, listener);
    }

    @Override
    public void execute(DeleteRequest request, ActionListener<DeleteResponse> listener) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(request);
        transportBulkAction.execute(bulkRequest, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                listener.onResponse(bulkItemResponses.getItems()[0].getResponse());
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    public ActionFuture<CreateIndexResponse> execute(CreateIndexRequest request) {
        return transportCreateIndexAction.execute(request);
    }

    public ActionFuture<IndicesExistsResponse> execute(IndicesExistsRequest request) {
        return transportIndicesExistsAction.execute(request);
    }

    public ActionFuture<GetResponse> execute(GetRequest request) {
        return transportGetAction.execute(request);
    }
}
