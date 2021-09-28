package io.github.lanicc.action.admin;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyCreateIndexAction extends Action<BabyCreateIndexRequest, BabyCreateIndexResponse, BabyCreateIndexRequestBuilder> {

    public static final String NAME = "BabyCreateIndexAction";
    public static final BabyCreateIndexAction INSTANCE = new BabyCreateIndexAction();

    private BabyCreateIndexAction() {
        super(NAME);
    }

    @Override
    public BabyCreateIndexRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new BabyCreateIndexRequestBuilder(client);
    }

    @Override
    public BabyCreateIndexResponse newResponse() {
        return new BabyCreateIndexResponse();
    }
}
