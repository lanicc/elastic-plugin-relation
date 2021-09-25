package io.github.lanicc.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class SimpleAction extends Action<SimpleRequest, SearchResponse, SimpleRequestBuilder> {

    public static final SimpleAction INSTANCE = new SimpleAction();

    public static final String NAME = "io.github.lanicc.action";

    private SimpleAction() {
        super(NAME);
    }

    @Override
    public SimpleRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new SimpleRequestBuilder(client);
    }

    @Override
    public SearchResponse newResponse() {
        return new SearchResponse();
    }
}
