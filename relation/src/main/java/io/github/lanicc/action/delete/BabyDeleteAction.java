package io.github.lanicc.action.delete;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyDeleteAction extends Action<BabyDeleteRequest, BabyDeleteResponse, BabyDeleteRequestBuilder> {

    public static final String NAME = "BabyDeleteAction";

    public static final BabyDeleteAction INSTANCE = new BabyDeleteAction();

    private BabyDeleteAction() {
        super(NAME);
    }

    @Override
    public BabyDeleteRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new BabyDeleteRequestBuilder(client);
    }

    @Override
    public BabyDeleteResponse newResponse() {
        return new BabyDeleteResponse();
    }
}
