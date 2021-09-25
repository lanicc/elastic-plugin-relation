package io.github.lanicc;

import io.github.lanicc.action.SimpleAction;
import org.elasticsearch.action.search.TransportSimpleAction;
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
        return Arrays.asList(new ActionHandler<>(SimpleAction.INSTANCE, TransportSimpleAction.class));
    }


}
