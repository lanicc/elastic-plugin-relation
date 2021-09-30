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
public class OneManyRelationHandler extends AbstractRelationHandler {

    private static final String SCRIPT_UPDATE =
            "        if (ctx._source.%s != null)\n" +
                    "            ctx._source.%s.removeIf(item -> String.valueOf(item.%s)=='%s');\n" +
                    "        if (ctx._source.%s == null) \n" +
                    "            ctx._source.%s=[]; \n" +
                    "        ctx._source.%s.add(params.p);";
    private static final String SCRIPT_DELETE =
            "        if (ctx._source.%s != null)\n" +
                    "            ctx._source.%s.removeIf(item -> String.valueOf(item.%s)=='%s');\n";

    public OneManyRelationHandler(ActionRunner actionRunner) {
        super(actionRunner);
    }

    @Override
    public void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) {
        String index = request.getIndex();
        Map<String, Object> source = request.getSource();

        String name = hitRelation.getName();
        String relationForeignKey = hitRelation.getForeignKey();
        String relationPrimaryKey = hitRelation.getPrimaryKey();
        Object o = source.get(relationForeignKey);

        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " foreign key " + relationForeignKey + " cannot be null");
        }
        String relationForeignKeyValue = String.valueOf(o);

        o = source.get(relationPrimaryKey);
        if (o == null) {
            throw new IllegalArgumentException("relation " + name + " primary key " + relationPrimaryKey + " cannot be null");
        }

        String relationPrimaryKeyValue = String.valueOf(o);

        Map<String, Object> params = new HashMap<>();
        params.put("p", source);
        Script script =
                new Script(
                        ScriptType.INLINE,
                        DEFAULT_SCRIPT_LANG,
                        String.format(SCRIPT_UPDATE, name, name, relationPrimaryKey, relationPrimaryKeyValue, name, name, name),
                        params
                );


        Map<String, Object> upsertMap = new HashMap<>();
        upsertMap.put(name, Collections.singletonList(source));

        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(relationForeignKeyValue)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .script(script)
                        .upsert(upsertMap);

        actionRunner.execute(updateRequest, applyToUpdateResponse(listener));
    }

    @Override
    public void delete(BabyDeleteRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyDeleteResponse> listener) {
        String index = request.getIndex();
        String id = request.getId();
        String primaryKeyValue = request.getPrimaryKey();
        String name = hitRelation.getName();
        String primaryKey = hitRelation.getPrimaryKey();
        Script script =
                new Script(
                        ScriptType.INLINE,
                        DEFAULT_SCRIPT_LANG,
                        String.format(SCRIPT_DELETE, name, name, primaryKey, primaryKeyValue),
                        Collections.emptyMap()
                );
        UpdateRequest updateRequest =
                new UpdateRequest()
                        .index(index)
                        .id(id)
                        .type(BabyCreateIndexRequest.BABY_INDIES_RELATION_TYPE)
                        .script(script);
        actionRunner.execute(updateRequest, applyDeleteToUpdateResponse(listener));
    }
}
