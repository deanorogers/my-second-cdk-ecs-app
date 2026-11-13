package com.red;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.PoolOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TranxEasyClientProducer {

    @Inject
    Vertx vertx;

    private static final Logger LOG = LoggerFactory.getLogger(TranxEasyClientProducer.class);

    @ConfigProperty(name = "transactions.connection-ttl-ms", defaultValue = "30000")
    int connectionTtlMs;

    @Produces
    @ApplicationScoped
    HttpClient createClient() {
        if (connectionTtlMs <= 0) {
            throw new IllegalArgumentException("transactions.connection-ttl-ms must be positive");
        }
        LOG.info("Creating Vert.x HttpClient with connection TTL {} ms", connectionTtlMs);
        PoolOptions poolOptions = new PoolOptions()
                .setHttp1MaxSize(10);
//                .setMaxLifetime(connectionTtlMs)
//                .setMaxLifetimeUnit(TimeUnit.MILLISECONDS);
        return vertx.httpClientBuilder()
                .with(poolOptions)
                .withConnectHandler(connection -> LOG.info(
                        "TranxEasyClient connection established: local={}, remote={}, protocol={}",
                        connection.localAddress(), connection.remoteAddress(), connection.protocolVersion()))
                .build();
    }

    void closeClient(@Disposes HttpClient client) {
        client.close();
    }
}
