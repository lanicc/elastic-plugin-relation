package io.github.lanicc.action.delete;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyDeleteRequest extends ActionRequest {

    private String index;

    /**
     * 1,1-1，1-n时必传
     * n-1可选
     */
    private String id;

    /**
     * 必传
     */
    private String relation;

    /**
     * relation是1-n、n-1必传
     */
    private String primaryKey;

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        index = in.readString();
        id = in.readOptionalString();
        relation = in.readString();
        primaryKey = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(index);
        out.writeOptionalString(id);
        out.writeString(relation);
        out.writeOptionalString(primaryKey);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
