package com.red;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

@ApplicationScoped
public class TranxEasyClient {

    private static final Logger LOG = LoggerFactory.getLogger(TranxEasyClient.class);

    @Inject
    HttpClient client;

    @ConfigProperty(name = "transactions.base-url")
    URI baseUrl;

    public Future<List<Tranx>> getTransactions(String accountId) {
        URI uri = UriBuilder.fromUri(baseUrl).path("transactions")
                .queryParam("accountId", accountId).build();
        return get(uri, "application/json").map(body -> {
            var transactions = body.toJsonArray();
            return IntStream.range(0, transactions.size())
                    .mapToObj(index -> transactions.getJsonObject(index).mapTo(Tranx.class))
                    .toList();
        });
    }

    public Future<String> getInstanceName() {
        LOG.info("Getting instance name from {}", baseUrl);
        URI uri = UriBuilder.fromUri(baseUrl).path("transactions/instance-name").build();
        return get(uri, "text/plain").map(Buffer::toString);
    }

    private Future<Buffer> get(URI uri, String accept) {
        RequestOptions options = new RequestOptions()
                .setMethod(HttpMethod.GET)
                .setAbsoluteURI(uri.toString())
                .putHeader("Accept", accept);
        return client.request(options)
                .compose(HttpClientRequest::send)
                .compose(response -> {
                    LOG.info("Response from {} used {}", uri, response.version());
                    return response.body().compose(body -> {
                        if (response.statusCode() < 200 || response.statusCode() >= 300) {
                            return Future.failedFuture(new WebApplicationException(
                                    "GET " + uri + " returned " + response.statusCode(),
                                    response.statusCode()));
                        }
                        return Future.succeededFuture(body);
                    });
                });
    }
}
