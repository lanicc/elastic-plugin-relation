package io.github.lanicc.action.index;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyIndexRequestBuilder extends ActionRequestBuilder<BabyIndexRequest, BabyIndexResponse, BabyIndexRequestBuilder> {
    public BabyIndexRequestBuilder(ElasticsearchClient client) {
        super(client, BabyIndexAction.INSTANCE, new BabyIndexRequest());
    }
}
