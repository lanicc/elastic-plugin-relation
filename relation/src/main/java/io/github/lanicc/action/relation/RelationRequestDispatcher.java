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
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexNotFoundException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.logging.ESLoggerFactory;

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

    private static final Logger logger = ESLoggerFactory.getLogger(RelationRequestDispatcher.class);

    public static final RelationRequestDispatcher INSTANCE = new RelationRequestDispatcher();

    public void init(RelationHolder relationHolder, TransportActionRunner actionRunner) {
        logger.info("init");
        this.relationHolder = relationHolder;
        this.actionRunner = actionRunner;
        buildRelationHandler();
    }


    private RelationRequestDispatcher() {
    }

    protected void buildRelationHandler() {
        this.main = new MainRelationHandler(actionRunner);
        this.oneOne = new OneOneRelationHandler(actionRunner);
        this.oneMany = new OneManyRelationHandler(actionRunner);
        this.manyOne = new ManyOneRelationHandler(actionRunner);
        this.createRelationHandler = new CreateRelationHandler(actionRunner);
    }

    protected RelationHolder relationHolder;
    protected TransportActionRunner actionRunner;

    protected CreateRelationHandler createRelationHandler;

    protected AbstractRelationHandler main;
    protected AbstractRelationHandler oneOne;
    protected AbstractRelationHandler oneMany;
    protected AbstractRelationHandler manyOne;

    public void create(BabyCreateIndexRequest request, ActionListener<BabyCreateIndexResponse> listener) {
        tryRun(() -> createRelationHandler.create(request, listener), listener);
    }

    /**
     * BabyIndexRequest
     *
     * @param request  BabyIndexRequest
     * @param listener listener
     */
    public void index(BabyIndexRequest request, ActionListener<BabyIndexResponse> listener) {
        logger.info("request: {}", request);
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
            logger.error("error", e);
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

        if (Objects.equals(requestRelation, primaryRelation.getName())) {
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
        }
        Relation.Type type = RelationHelper.typeOf(primaryRelation, hitRelation);
        switch (type) {
            case MAIN:
                return new Pair<>(hitRelation, main);
            case ONE_ONE:
                return new Pair<>(hitRelation, oneOne);
            case ONE_N:
                return new Pair<>(hitRelation, oneMany);
            case N_ONE:
                return new Pair<>(hitRelation, manyOne);
            default:
                throw new UnsupportedOperationException("not support relation type of " + type.relation);
        }
    }

    interface Runnable {
        void run() throws Exception;
    }
}
