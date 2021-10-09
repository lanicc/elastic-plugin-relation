package io.github.lanicc;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.junit.jupiter.api.BeforeEach;

import java.net.InetAddress;

/**
 * Created on 2021/9/30.
 *
 * @author lan
 * @since 2.0.0
 */
public class BaseTests {

    protected TransportClient client;

    @BeforeEach
    void setUp() {
        client = getClient();
    }

    private static TransportClient getClient() {
        Settings settings = Settings.builder()
                .put("cluster.name", "es-cluster")
                .put("xpack.security.user", "elastic:changeme")
                .build();
        TransportClient client = new PreBuiltXPackTransportClient(settings, LanPlugin.class);
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getLoopbackAddress(), 9300));
        return client;
    }
}
