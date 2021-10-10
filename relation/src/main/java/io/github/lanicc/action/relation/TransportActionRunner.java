package io.github.lanicc.action.relation;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkAction;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class TransportActionRunner implements ActionRunner {

    private final Client client;

    public TransportActionRunner(Client client) {
        this.client = client;
    }

    public void execute(UpdateByQueryRequest request, ActionListener<BulkByScrollResponse> listener) {
        client.execute(UpdateByQueryAction.INSTANCE, request, listener);
    }

    @Override
    public void execute(IndexRequest request, ActionListener<IndexResponse> listener) {
        client.execute(IndexAction.INSTANCE, request, listener);
    }

    public void execute(UpdateRequest request, ActionListener<UpdateResponse> listener) {
        client.execute(UpdateAction.INSTANCE, request, listener);
    }

    public void execute(CreateIndexRequest request, ActionListener<CreateIndexResponse> listener) {
        client.execute(CreateIndexAction.INSTANCE, request, listener);
    }

    public void execute(BulkRequest request, ActionListener<BulkResponse> listener) {
        client.execute(BulkAction.INSTANCE, request, listener);
    }

    @Override
    public void execute(DeleteRequest request, ActionListener<DeleteResponse> listener) {
        client.execute(DeleteAction.INSTANCE, request, listener);
    }

    public ActionFuture<CreateIndexResponse> execute(CreateIndexRequest request) {
        return client.execute(CreateIndexAction.INSTANCE, request);
    }

    public ActionFuture<IndicesExistsResponse> execute(IndicesExistsRequest request) {
        return client.execute(IndicesExistsAction.INSTANCE, request);
    }

    public ActionFuture<GetResponse> execute(GetRequest request) {
        return client.execute(GetAction.INSTANCE, request);
    }
}
