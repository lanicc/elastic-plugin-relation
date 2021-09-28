package io.github.lanicc.action.admin;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyCreateIndexRequestBuilder extends ActionRequestBuilder<BabyCreateIndexRequest, BabyCreateIndexResponse, BabyCreateIndexRequestBuilder> {
    public BabyCreateIndexRequestBuilder(ElasticsearchClient client) {
        super(client, BabyCreateIndexAction.INSTANCE, new BabyCreateIndexRequest());
    }
}
