package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class OneOneRelationHandler extends AbstractRelationHandler {

    private static final String SCRIPT_DELETE = "ctx._source.removeKey('%s');";

    public OneOneRelationHandler(ActionRunner actionRunner) {
        super(actionRunner);
    }

    @Override
    public void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) {
        String index = request.getIndex();
        Map<String, Object> source = request.getSource();

        String name = hitRelation.getName();
        String relationForeignKey = hitRelation.getForeignKey();
        Object o = source.get(relationForeignKey);

        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " foreign key " + relationForeignKey + " cannot be null");
        }
        String relationPrimaryValue = String.valueOf(o);

        Map<String, Object> map = new HashMap<>(1);
        map.put(name, source);

        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(relationPrimaryValue)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .doc(map)
                        .upsert(map);
        actionRunner.execute(updateRequest, applyToUpdateResponse(listener));
    }

    @Override
    public void delete(BabyDeleteRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyDeleteResponse> listener) {
        String index = request.getIndex();
        String id = request.getId();
        String relationName = hitRelation.getName();

        Script script =
                new Script(
                        ScriptType.INLINE,
                        DEFAULT_SCRIPT_LANG,
                        String.format(SCRIPT_DELETE, relationName),
                        Collections.emptyMap()
                );

        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .id(id)
                        .script(script);
        actionRunner.execute(updateRequest, applyDeleteToUpdateResponse(listener));
    }
}
