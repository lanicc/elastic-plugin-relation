package io.github.lanicc.action;

import io.github.lanicc.action.relation.RelationRequestDispatcher;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.function.Supplier;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public abstract class BabyTransportAction<Request extends ActionRequest, Response extends ActionResponse> extends HandledTransportAction<Request, Response> {

    protected final RelationRequestDispatcher requestDispatcher = RelationRequestDispatcher.INSTANCE;

    protected BabyTransportAction(Settings settings, String actionName, ThreadPool threadPool, TransportService transportService, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<Request> request) {
        super(settings, actionName, threadPool, transportService, actionFilters, indexNameExpressionResolver, request);
    }

    protected BabyTransportAction(Settings settings, String actionName, boolean canTripCircuitBreaker, ThreadPool threadPool, TransportService transportService, ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver, Supplier<Request> request) {
        super(settings, actionName, canTripCircuitBreaker, threadPool, transportService, actionFilters, indexNameExpressionResolver, request);
    }
}
