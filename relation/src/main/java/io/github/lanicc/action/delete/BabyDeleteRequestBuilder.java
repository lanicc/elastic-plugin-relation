package io.github.lanicc.action.delete;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyDeleteRequestBuilder extends ActionRequestBuilder<BabyDeleteRequest, BabyDeleteResponse, BabyDeleteRequestBuilder> {
    protected BabyDeleteRequestBuilder(ElasticsearchClient client) {
        super(client, BabyDeleteAction.INSTANCE, new BabyDeleteRequest());
    }
}
