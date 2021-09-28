package io.github.lanicc.action.index;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Created on 2021/9/27.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyIndexResponse extends ActionResponse {

    private boolean success;

    public BabyIndexResponse() {
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        success = in.readOptionalBoolean();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalBoolean(success);
    }
}
