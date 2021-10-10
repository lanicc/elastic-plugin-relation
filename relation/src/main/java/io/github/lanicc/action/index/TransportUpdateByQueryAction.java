package io.github.lanicc.action.index;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ParentBulkByScrollTask;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Created on 2021/9/28.
 *
 * @author lan
 * @since 2.0.0
 */

public class TransportUpdateByQueryAction extends HandledTransportAction<UpdateByQueryRequest, BulkByScrollResponse> {
    private final Client client;
    private final ClusterService clusterService;

    public TransportUpdateByQueryAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters,
                                        IndexNameExpressionResolver indexNameExpressionResolver, Client client,
                                        TransportService transportService, ClusterService clusterService) {
        super(settings, UpdateByQueryAction.NAME + "_", threadPool, transportService, actionFilters,
                indexNameExpressionResolver, UpdateByQueryRequest::new);
        this.client = client;
        this.clusterService = clusterService;
    }

    @Override
    protected void doExecute(Task task, UpdateByQueryRequest request, ActionListener<BulkByScrollResponse> listener) {
        BulkByScrollParallelizationHelper.startSlices(client, taskManager, UpdateByQueryAction.INSTANCE,
                clusterService.localNode().getId(), (ParentBulkByScrollTask) task, request, listener);
    }

    @Override
    protected void doExecute(UpdateByQueryRequest request, ActionListener<BulkByScrollResponse> listener) {
        throw new UnsupportedOperationException("task required");
    }


}
