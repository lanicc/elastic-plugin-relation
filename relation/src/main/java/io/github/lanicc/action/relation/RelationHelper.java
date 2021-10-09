package io.github.lanicc.action.relation;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public final class RelationHelper {

    public static Relation.Type typeOf(Relation primaryRelation, Relation hitRelation) {
        if (Objects.equals(primaryRelation, hitRelation)) {
            return Relation.Type.MAIN;
        } else {
            String relatedKey = hitRelation.getRelatedKey();
            String primaryRelationPrimaryKey = primaryRelation.getPrimaryKey();
            if (Objects.equals(primaryRelationPrimaryKey, relatedKey)) {
                if (hitRelation.isNested()) {
                    return Relation.Type.ONE_N;
                } else {
                    return Relation.Type.ONE_ONE;
                }
            } else {
                String primaryKey = hitRelation.getPrimaryKey();
                String foreignKey = hitRelation.getForeignKey();
                if (Objects.equals(foreignKey, primaryKey)) {
                    return Relation.Type.N_ONE;
                }
                //n-n
                throw new UnsupportedOperationException("not support relation type of n-n");
            }
        }
    }

    public static boolean matchType(Relation primaryRelation, Relation hitRelation, Relation.Type type) {
        return typeOf(primaryRelation, hitRelation) == type;
    }

    public static List<Relation> parentOf(Relation primaryRelation) {
        String primaryKey = primaryRelation.getPrimaryKey();
        return primaryRelation.getChildren()
                .stream()
                .filter(relation -> !Objects.equals(relation.getRelatedKey(), primaryKey))
                .collect(Collectors.toList());
    }

}
