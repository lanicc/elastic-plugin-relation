package io.github.lanicc.action.index;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyIndexAction extends Action<BabyIndexRequest, BabyIndexResponse, BabyIndexRequestBuilder> {
    public static final String NAME = "BabyIndexAction";

    public static final BabyIndexAction INSTANCE = new BabyIndexAction();


    private BabyIndexAction() {
        super(NAME);
    }

    @Override
    public BabyIndexRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new BabyIndexRequestBuilder(client);
    }

    @Override
    public BabyIndexResponse newResponse() {
        return new BabyIndexResponse();
    }
}
