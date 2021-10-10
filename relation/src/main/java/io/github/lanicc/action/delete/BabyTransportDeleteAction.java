package io.github.lanicc.action.delete;

import io.github.lanicc.action.BabyTransportAction;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyTransportDeleteAction extends BabyTransportAction<BabyDeleteRequest, BabyDeleteResponse> {

    @Inject
    public BabyTransportDeleteAction(Settings settings, ThreadPool threadPool, TransportService transportService, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, BabyDeleteAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, BabyDeleteRequest::new);
    }

    @Override
    protected void doExecute(BabyDeleteRequest request, ActionListener<BabyDeleteResponse> listener) {
        requestDispatcher.delete(request, listener);
    }
}
