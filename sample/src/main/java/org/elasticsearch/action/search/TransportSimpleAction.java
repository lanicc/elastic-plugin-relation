package org.elasticsearch.action.search;

import io.github.lanicc.action.SimpleAction;
import io.github.lanicc.action.SimpleRequest;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class TransportSimpleAction extends HandledTransportAction<SimpleRequest, SearchResponse> {

    private final TransportSearchAction transportSearchAction;

    @Inject
    public TransportSimpleAction(Settings settings, ThreadPool threadPool, TransportService transportService, ActionFilters actionFilters,
                                 IndexNameExpressionResolver indexNameExpressionResolver, TransportSearchAction transportSearchAction) {
        super(settings, SimpleAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, SimpleRequest::new);
        this.transportSearchAction = transportSearchAction;
    }

    @Override
    protected void doExecute(SimpleRequest request, ActionListener<SearchResponse> listener) {
        transportSearchAction.execute(request.getSearchRequest(), listener);
    }

}
