package io.github.lanicc.action.admin;

import io.github.lanicc.action.BabyTransportAction;
import io.github.lanicc.action.index.TransportUpdateByQueryAction;
import io.github.lanicc.action.relation.RelationHolder;
import io.github.lanicc.action.relation.RelationRequestDispatcher;
import io.github.lanicc.action.relation.TransportActionRunner;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.TransportCreateIndexAction;
import org.elasticsearch.action.admin.indices.exists.indices.TransportIndicesExistsAction;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.elasticsearch.action.get.TransportGetAction;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.update.TransportUpdateAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyTransportCreateIndexAction extends BabyTransportAction<BabyCreateIndexRequest, BabyCreateIndexResponse> {

    @Inject
    public BabyTransportCreateIndexAction(
            Settings settings, ThreadPool threadPool,
            TransportService transportService, ActionFilters actionFilters,
            IndexNameExpressionResolver indexNameExpressionResolver,
            Client client,
            ClusterService clusterService,
            TransportCreateIndexAction transportCreateIndexAction,
            TransportBulkAction transportBulkAction,
            TransportIndicesExistsAction transportIndicesExistsAction,
            TransportUpdateAction transportUpdateAction,
            TransportGetAction transportGetAction) {
        super(settings, BabyCreateIndexAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyCreateIndexRequest::new);

        TransportUpdateByQueryAction transportUpdateByQueryAction = new TransportUpdateByQueryAction(settings, threadPool, actionFilters, indexNameExpressionResolver, client, transportService, clusterService);
        TransportActionRunner actionRunner = new TransportActionRunner(transportGetAction, transportUpdateAction, transportUpdateByQueryAction, transportCreateIndexAction, transportBulkAction, transportIndicesExistsAction);
        RelationHolder relationHolder = new RelationHolder(actionRunner);
        RelationRequestDispatcher.INSTANCE.init(relationHolder, actionRunner);
    }


    @Override
    protected void doExecute(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        requestDispatcher.create(request, listener);
    }

}
