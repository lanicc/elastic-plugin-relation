package io.github.lanicc.action.admin;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Created on 2021/9/26.
 *
 * @author lan
 * @since 2.0.0
 */
public class BabyCreateIndexResponse extends ActionResponse {

    private boolean success;

    private String message;

    public BabyCreateIndexResponse() {
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        success = in.readBoolean();
        message = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalBoolean(success);
        out.writeOptionalString(message);
    }

}
