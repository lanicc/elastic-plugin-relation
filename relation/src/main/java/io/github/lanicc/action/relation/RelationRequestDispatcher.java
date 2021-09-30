package io.github.lanicc.action.relation;

import io.github.lanicc.action.admin.BabyCreateIndexRequest;
import io.github.lanicc.action.admin.BabyCreateIndexResponse;
import io.github.lanicc.action.delete.BabyDeleteRequest;
import io.github.lanicc.action.delete.BabyDeleteResponse;
import io.github.lanicc.action.exception.RelationNotFoundException;
import io.github.lanicc.action.index.BabyIndexRequest;
import io.github.lanicc.action.index.BabyIndexResponse;
import io.github.lanicc.action.relation.handler.*;
import io.github.lanicc.util.Pair;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created on 2021/9/29.
 *
 * @author lan
 * @since 2.0.0
 */
public class RelationRequestDispatcher {

    public static final RelationRequestDispatcher INSTANCE = new RelationRequestDispatcher();

    public void init(RelationHolder relationHolder, TransportActionRunner actionRunner) {
        this.relationHolder = relationHolder;
        this.actionRunner = actionRunner;
        buildRelationHandler();
    }


    private RelationRequestDispatcher() {
    }

    protected void buildRelationHandler() {
        this.single = new MainRelationHandler(actionRunner);
        this.oneOne = new OneOneRelationHandler(actionRunner);
        this.oneMany = new OneManyRelationHandler(actionRunner);
        this.manyOne = new ManyOneRelationHandler(actionRunner);
        this.createRelationHandler = new CreateRelationHandler(actionRunner);
    }

    protected RelationHolder relationHolder;
    protected TransportActionRunner actionRunner;

    protected CreateRelationHandler createRelationHandler;

    protected AbstractRelationHandler single;
    protected AbstractRelationHandler oneOne;
    protected AbstractRelationHandler oneMany;
    protected AbstractRelationHandler manyOne;

    public void create(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        createRelationHandler.create(request, listener);
    }

    /**
     * BabyIndexRequest
     *
     * @param request  BabyIndexRequest
     * @param listener listener
     */
    public void index(BabyIndexRequest request, ActionListener<BabyIndexResponse> listener) {
        tryRun(() -> {
            String requestRelation = request.getRelation();
            String index = request.getIndex();
            Relation primaryRelation = getPrimaryRelation(index);
            Pair<Relation, AbstractRelationHandler> pair = getRelationHandler(index, primaryRelation, requestRelation);
            pair.v().index(request, primaryRelation, pair.k(), listener);
        }, listener);
    }

    public void delete(BabyDeleteRequest request, ActionListener<BabyDeleteResponse> listener) {
        tryRun(() -> {
            String requestRelation = request.getRelation();
            String index = request.getIndex();
            Relation primaryRelation = getPrimaryRelation(index);
            Pair<Relation, AbstractRelationHandler> pair = getRelationHandler(index, primaryRelation, requestRelation);
            pair.v().delete(request, primaryRelation, pair.k(), listener);
        }, listener);
    }

    private void tryRun(Runnable r, ActionListener<? extends ActionResponse> listener) {
        try {
            r.run();
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    private Relation getPrimaryRelation(String index) throws IndexNotFoundException {
        Relation primaryRelation = relationHolder.get(index);
        if (primaryRelation == null) {
            throw new IndexNotFoundException(index + " not found from " + BabyCreateIndexRequest.BABY_INDIES_RELATION);
        }
        return primaryRelation;
    }

    private Pair<Relation, AbstractRelationHandler> getRelationHandler(String index, Relation primaryRelation, String requestRelation) {
        Relation hitRelation;
        AbstractRelationHandler relationHandler;

        if (Objects.equals(requestRelation, primaryRelation.getName())) {
            relationHandler = single;
            hitRelation = primaryRelation;
        } else {
            List<Relation> children = primaryRelation.getChildren();
            Optional<Relation> relationOptional = children.stream()
                    .filter(r -> Objects.equals(r.getName(), requestRelation))
                    .findAny();
            if (!relationOptional.isPresent()) {
                throw new RelationNotFoundException(requestRelation + " not found from index " + index);
            }
            hitRelation = relationOptional.get();
            String relatedKey = hitRelation.getRelatedKey();
            String primaryRelationPrimaryKey = primaryRelation.getPrimaryKey();
            if (Objects.equals(primaryRelationPrimaryKey, relatedKey)) {
                if (hitRelation.isNested()) {
                    relationHandler = oneMany;
                } else {
                    relationHandler = oneOne;
                }
            } else {
                relationHandler = manyOne;
            }
        }
        return new Pair<>(hitRelation, relationHandler);
    }

    interface Runnable {
        void run() throws Exception;
    }
}
