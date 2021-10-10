package io.github.lanicc.action.admin;

import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyCreateIndexRequest extends AcknowledgedRequest<BabyCreateIndexRequest> {

    public final static String BABY_INDIES_RELATION = "baby_indies_relation";
    public final static String BABY_INDIES_RELATION_TYPE = "doc";

    private CreateIndexRequest createIndexRequest;

    private Relation relation;

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    public CreateIndexRequest getCreateIndexRequest() {
        return createIndexRequest;
    }

    public void setCreateIndexRequest(CreateIndexRequest createIndexRequest) {
        this.createIndexRequest = createIndexRequest;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        createIndexRequest = new CreateIndexRequest();
        createIndexRequest.readFrom(in);
        relation = new Relation();
        relation.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        createIndexRequest.writeTo(out);
        relation.writeTo(out);
    }
}
