package io.github.lanicc.action.index;

import io.github.lanicc.action.BabyTransportAction;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyTransportIndexAction extends BabyTransportAction<BabyIndexRequest, BabyIndexResponse> {

    @Inject
    public BabyTransportIndexAction(
            Settings settings, ThreadPool threadPool,
            TransportService transportService, ActionFilters actionFilters,
            IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, BabyIndexAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyIndexRequest::new);
    }

    @Override
    protected void doExecute(BabyIndexRequest request, ActionListener<BabyIndexResponse> listener) {
        requestDispatcher.index(request, listener);
    }


}
