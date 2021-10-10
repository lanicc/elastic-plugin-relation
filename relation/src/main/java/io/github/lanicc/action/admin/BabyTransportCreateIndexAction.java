package io.github.lanicc.action.admin;

import io.github.lanicc.action.BabyTransportAction;
import io.github.lanicc.action.relation.RelationHolder;
import io.github.lanicc.action.relation.RelationRequestDispatcher;
import io.github.lanicc.action.relation.TransportActionRunner;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
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
            Client client
    ) {
        super(settings, BabyCreateIndexAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyCreateIndexRequest::new);
        TransportActionRunner actionRunner = new TransportActionRunner(client);
        RelationHolder relationHolder = new RelationHolder(actionRunner);
        RelationRequestDispatcher.INSTANCE.init(relationHolder, actionRunner);
    }


    @Override
    protected void doExecute(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        requestDispatcher.create(request, listener);
    }

}
