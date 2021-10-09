package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;

import java.util.Map;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class MainRelationHandler extends AbstractRelationHandler {

    private static final String SCRIPT_REMOVE_KEY = "ctx._source.removeKey('%s');";

    public MainRelationHandler(ActionRunner actionRunner) {
        super(actionRunner);
    }

    @Override
    public void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) {
        String index = request.getIndex();
        String key = primaryRelation.getPrimaryKey();
        Map<String, Object> source = request.getSource();
        Object o = source.get(key);
        if (o == null) {
            throw new IllegalArgumentException("relation " + primaryRelation.getName() + " key cannot be null");
        }
        String keyValue = String.valueOf(o);
        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(keyValue)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .doc(source)
                        .upsert(source);
        actionRunner.execute(updateRequest, applyToUpdateResponse(listener));
    }

    @Override
    public void delete(BabyDeleteRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyDeleteResponse> listener) {
        String index = request.getIndex();
        String id = request.getId();
        DeleteRequest deleteRequest =
                new DeleteRequest()
                        .index(index)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(id);
        actionRunner.execute(deleteRequest, applyToDeleteResponse(listener));
    }
}
