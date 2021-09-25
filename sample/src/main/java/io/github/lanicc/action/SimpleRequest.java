package io.github.lanicc.action;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class SimpleRequest extends ActionRequest {

    private SearchRequest searchRequest;

    public SearchRequest getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        searchRequest = new SearchRequest();
        searchRequest.readFrom(in);
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        searchRequest.writeTo(out);
    }
}
