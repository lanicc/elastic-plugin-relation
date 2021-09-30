package io.github.lanicc.action.relation;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;

/**
 * 使用接口，方便本地调试
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public interface ActionRunner {

    void execute(UpdateByQueryRequest request, ActionListener<BulkByScrollResponse> listener);

    void execute(IndexRequest request, ActionListener<IndexResponse> listener);

    void execute(UpdateRequest request, ActionListener<UpdateResponse> listener);

    void execute(CreateIndexRequest request, ActionListener<CreateIndexResponse> listener);

    void execute(BulkRequest request, ActionListener<BulkResponse> listener);

    void execute(DeleteRequest request, ActionListener<DeleteResponse> listener);

    ActionFuture<CreateIndexResponse> execute(CreateIndexRequest request);

    ActionFuture<IndicesExistsResponse> execute(IndicesExistsRequest request);

    ActionFuture<GetResponse> execute(GetRequest request);

}
