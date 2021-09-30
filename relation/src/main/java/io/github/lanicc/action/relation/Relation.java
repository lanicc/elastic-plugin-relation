package io.github.lanicc.action.relation;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class Relation implements Streamable {

    /**
     * 名称
     */
    private String name;

    /**
     * 主键
     */
    private String primaryKey;

    /**
     * 关联的外键
     */
    private String relatedKey;

    /**
     * 外键
     */
    private String foreignKey;

    private boolean nested;

    /**
     * 子
     */
    private List<Relation> children;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("name", name);
        map.put("primaryKey", primaryKey);
        map.put("relatedKey", relatedKey);
        map.put("foreignKey", foreignKey);
        map.put("nested", nested);
        map.put("children",
                children.stream()
                        .map(Relation::toMap)
                        .collect(Collectors.toList())
        );
        return map;
    }

    public static Relation fromMap(Map<String, Object> map) {
        Relation relation = new Relation();
        relation.setName(String.valueOf(map.get("name")));
        relation.setPrimaryKey(String.valueOf(map.get("primaryKey")));
        relation.setRelatedKey(String.valueOf(map.get("relatedKey")));
        relation.setForeignKey(String.valueOf(map.get("foreignKey")));
        relation.setNested(Boolean.parseBoolean(String.valueOf(map.get("nested"))));
        @SuppressWarnings("unchecked") List<Map<String, Object>> children = (List<Map<String, Object>>) map.getOrDefault("children", new ArrayList<>());
        relation.setChildren(children.stream().map(Relation::fromMap).collect(Collectors.toList()));
        return relation;
    }


    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readOptionalString();
        primaryKey = in.readOptionalString();
        relatedKey = in.readOptionalString();
        foreignKey = in.readOptionalString();
        children = in.readStreamableList(Relation::new);
        nested = in.readBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(name);
        out.writeOptionalString(primaryKey);
        out.writeOptionalString(relatedKey);
        out.writeOptionalString(foreignKey);
        out.writeStreamableList(getChildren());
        out.writeBoolean(nested);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getRelatedKey() {
        return relatedKey;
    }

    public void setRelatedKey(String relatedKey) {
        this.relatedKey = relatedKey;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public List<Relation> getChildren() {
        if (children == null) {
            children = Collections.emptyList();
        }
        return children;
    }

    public void setChildren(List<Relation> children) {
        this.children = children;
    }

    public boolean isNested() {
        return nested;
    }

    public void setNested(boolean nested) {
        this.nested = nested;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Relation.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("primaryKey='" + primaryKey + "'")
                .add("relatedKey='" + relatedKey + "'")
                .add("nested=" + nested)
                .add("children=" + children)
                .toString();
    }

    public enum Type {

        SINGLE("1"),
        ONE_ONE("1-1"),
        ONE_N("1-n"),
        N_ONE("n-1");

        public final String relation;

        Type(String relation) {
            this.relation = relation;
        }
    }

}
