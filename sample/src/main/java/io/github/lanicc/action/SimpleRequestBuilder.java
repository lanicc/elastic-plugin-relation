package io.github.lanicc.action;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class SimpleRequestBuilder extends ActionRequestBuilder<SimpleRequest, SearchResponse, SimpleRequestBuilder> {
    public SimpleRequestBuilder(ElasticsearchClient client) {
        super(client, SimpleAction.INSTANCE, new SimpleRequest());
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client, SearchAction.INSTANCE);
        searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
        request.setSearchRequest(searchRequestBuilder.request());
    }

    @Override
    public SimpleRequest request() {
        return request;
    }
}
