package io.github.lanicc.action.relation.handler;

import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.ActionRunner;
import io.github.lanicc.action.relation.Relation;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public abstract class AbstractRelationHandler {

    public AbstractRelationHandler(ActionRunner actionRunner) {
        this.actionRunner = actionRunner;
    }

    protected ActionRunner actionRunner;

    public abstract void index(BabyIndexRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyIndexResponse> listener) throws Exception;

    public void delete(BabyDeleteRequest request, Relation primaryRelation, Relation hitRelation, ActionListener<BabyDeleteResponse> listener) throws Exception {
    }

    ;

    protected ActionListener<BulkByScrollResponse> applyToBulkByScrollResponse(ActionListener<BabyIndexResponse> listener) {
        return new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                BabyIndexResponse response = new BabyIndexResponse();
                response.setSuccess(bulkByScrollResponse.getBulkFailures().isEmpty());
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        };
    }

    protected ActionListener<UpdateResponse> applyToUpdateResponse(ActionListener<BabyIndexResponse> listener) {
        return new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                BabyIndexResponse response = new BabyIndexResponse();
                response.setSuccess(true);
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        };
    }

    protected ActionListener<UpdateResponse> applyDeleteToUpdateResponse(ActionListener<BabyDeleteResponse> listener) {
        return new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {
                BabyDeleteResponse response = new BabyDeleteResponse();
                response.setSuccess(true);
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        };
    }

    protected ActionListener<DeleteResponse> applyToDeleteResponse(ActionListener<BabyDeleteResponse> listener) {
        return new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                BabyDeleteResponse response = new BabyDeleteResponse();
                response.setSuccess(deleteResponse.getResult().getOp() > 1);
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        };
    }


    protected ActionListener<BulkByScrollResponse> applyDeleteToBulkByScrollResponse(ActionListener<BabyDeleteResponse> listener) {
        return new ActionListener<BulkByScrollResponse>() {
            @Override
            public void onResponse(BulkByScrollResponse bulkByScrollResponse) {
                BabyDeleteResponse response = new BabyDeleteResponse();
                response.setSuccess(bulkByScrollResponse.getBulkFailures().isEmpty());
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        };
    }

    static String getParentIndex(String index, String relationName) {
        return String.format("%s_parent_%s", index, relationName);
    }
}
