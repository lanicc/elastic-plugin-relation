package io.github.lanicc;

import io.github.lanicc.action.admin.BabyCreateIndexAction;
import io.github.lanicc.action.admin.BabyTransportCreateIndexAction;
import io.github.lanicc.action.delete.BabyDeleteAction;
import io.github.lanicc.action.delete.BabyTransportDeleteAction;
import io.github.lanicc.action.index.BabyIndexAction;
import io.github.lanicc.action.index.BabyTransportIndexAction;
import io.github.lanicc.action.simple.SimpleAction;
import io.github.lanicc.action.simple.TransportSimpleAction;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 2021/9/24.
 *
 * @author lan
 * @since 2.0.0
 */
public class LanPlugin extends Plugin implements ActionPlugin {

    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Arrays.asList(
                new ActionHandler<>(SimpleAction.INSTANCE, TransportSimpleAction.class),
                new ActionHandler<>(BabyCreateIndexAction.INSTANCE, BabyTransportCreateIndexAction.class),
                new ActionHandler<>(BabyIndexAction.INSTANCE, BabyTransportIndexAction.class),
                new ActionHandler<>(BabyDeleteAction.INSTANCE, BabyTransportDeleteAction.class)
        );
    }


}
