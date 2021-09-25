import io.github.lanicc.LanPlugin;
import io.github.lanicc.action.SimpleAction;
import io.github.lanicc.action.SimpleRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created on 2021/9/25.
 *
 * @author lan
 * @since 2.0.0
 */
public class Test {

    public static void main(String[] args) throws UnknownHostException, ExecutionException, InterruptedException {
        TransportClient client = getClient();
        IndexRequest indexRequest =
                new IndexRequest()
                        .index("test")
                        .type("test")
                        .id("test")
                        .source("Hello", "World")
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        IndexResponse indexResponse = client.index(indexRequest).actionGet();
        System.out.println(indexResponse);
        SimpleRequestBuilder builder = new SimpleRequestBuilder(client);
        SearchResponse searchResponse = client.execute(SimpleAction.INSTANCE, builder.request()).actionGet();
        System.out.println(searchResponse.toString());
    }

    private static TransportClient getClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "es-cluster")
                .put("xpack.security.user", "elastic:changeme")
                .build();
        TransportClient client = new PreBuiltXPackTransportClient(settings, LanPlugin.class);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getLoopbackAddress(), 9300));
        return client;
    }
}
