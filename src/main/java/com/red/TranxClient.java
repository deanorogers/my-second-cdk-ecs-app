package com.red;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@ApplicationScoped
public class TranxClient {

    @ConfigProperty(name = "transactions.base-url")
    String baseUrl;

    // Absolute connection lifetime in milliseconds, regardless of activity.
    @ConfigProperty(name = "transactions.connection-ttl-ms", defaultValue = "30000")
    long connectionTtlMs;

    @ConfigProperty(name = "transactions.connect-timeout-ms", defaultValue = "5000")
    long connectTimeoutMs;

    @ConfigProperty(name = "transactions.response-timeout-ms", defaultValue = "10000")
    long responseTimeoutMs;

    @Inject
    ObjectMapper objectMapper;

    private CloseableHttpClient httpClient;

    @PostConstruct
    void init() {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setTimeToLive(TimeValue.ofMilliseconds(connectionTtlMs))
                        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                        .build())
                .build();
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofMilliseconds(responseTimeoutMs))
                        .build())
                .build();
    }

    @PreDestroy
    void close() throws IOException {
        httpClient.close();
    }

    public List<Tranx> getTransactions(String accountId) {
        URI uri = uri("/transactions", accountId);
        return execute(uri, body -> objectMapper.readValue(body, new TypeReference<List<Tranx>>() { }));
    }

    public String getInstanceName() {
        return execute(uri("/transactions/instance-name", null), body -> new String(body, java.nio.charset.StandardCharsets.UTF_8));
    }

    private URI uri(String path, String accountId) {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + path);
            if (accountId != null) {
                builder.addParameter("accountId", accountId);
            }
            return builder.build();
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid transactions URL: " + baseUrl, exception);
        }
    }

    private <T> T execute(URI uri, BodyParser<T> parser) {
        try {
            return httpClient.execute(new HttpGet(uri), (ClassicHttpResponse response) -> {
                if (response.getCode() != HttpStatus.SC_OK) {
                    throw new IllegalStateException("GET " + uri + " returned " + response.getCode());
                }
                return parser.parse(EntityUtils.toByteArray(response.getEntity()));
            });
        } catch (IOException exception) {
            throw new UncheckedIOException("GET " + uri + " failed", exception);
        }
    }

    private interface BodyParser<T> {
        T parse(byte[] body) throws IOException;
    }
}
