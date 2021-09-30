package io.github.lanicc.action.relation;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;

import java.util.concurrent.ExecutionException;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class RelationHolder {

    protected ActionRunner actionRunner;

    public RelationHolder(ActionRunner actionRunner) {
        this.actionRunner = actionRunner;
    }

    public Relation get(String index) {
        GetRequest getRequest =
                new GetRequest()
                        .index(BabyCreateIndexRequest.BABY_INDIES_RELATION)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(index);
        ActionFuture<GetResponse> future = actionRunner.execute(getRequest);
        try {
            GetResponse response = future.get();
            return Relation.fromMap(response.getSource());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
