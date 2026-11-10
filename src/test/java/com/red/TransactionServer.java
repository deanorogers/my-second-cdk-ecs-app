package com.red;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TransactionServer implements QuarkusTestResourceLifecycleManager {
    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/transactions", exchange -> {
                boolean valid = "GET".equals(exchange.getRequestMethod())
                        && "/transactions".equals(exchange.getRequestURI().getPath())
                        && "accountId=2001".equals(exchange.getRequestURI().getRawQuery());
                byte[] response = (valid ? "[{\"id\":\"t-2001\",\"accountId\":\"2001\"}]" : "bad request")
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(valid ? 200 : 400, response.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(response);
                }
            });
            server.createContext("/transactions/instance-name", exchange -> {
                byte[] response = "Serving Transaction from instance: test".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(response);
                }
            });
            server.start();
            return Map.of("transactions.base-url",
                    "http://127.0.0.1:" + server.getAddress().getPort());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
