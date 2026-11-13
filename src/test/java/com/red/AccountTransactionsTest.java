package com.red;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(value = TransactionServer.class, restrictToAnnotatedClass = true)
class AccountTransactionsTest {

    @Inject
    TranxEasyClient easyClient;

    @Test
    void directClientCallsTransactionsService() throws Exception {
        var transactions = easyClient.getTransactions("2001").toCompletionStage().toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertEquals(1, transactions.size());
        assertEquals("t-2001", transactions.get(0).getId());
        assertEquals(Instant.parse("2026-10-01T09:00:00Z"), transactions.get(0).getTimestamp());
        assertEquals("Serving Transaction from instance: test", easyClient.getInstanceName()
                .toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
    }

    @Test
    void directClientReportsHttpErrors() {
        var exception = assertThrows(ExecutionException.class, () -> easyClient.getTransactions("unknown")
                .toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS));
        assertEquals(400, assertInstanceOf(WebApplicationException.class, exception.getCause())
                .getResponse().getStatus());
    }

//    @Test
//    void directClientReplacesConnectionsAfterMaxLifetime() throws Exception {
//        String firstPort = easyClient.getTransactions("ttl-test").toCompletionStage().toCompletableFuture()
//                .get(5, TimeUnit.SECONDS).get(0).getId();
//        String reusedPort = easyClient.getTransactions("ttl-test").toCompletionStage().toCompletableFuture()
//                .get(5, TimeUnit.SECONDS).get(0).getId();
//        assertEquals(firstPort, reusedPort, "The connection should be reused before its lifetime expires");
//        String currentPort = firstPort;
//        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
//        while (currentPort.equals(firstPort) && System.nanoTime() < deadline) {
//            Thread.sleep(50);
//            currentPort = easyClient.getTransactions("ttl-test").toCompletionStage().toCompletableFuture()
//                    .get(5, TimeUnit.SECONDS).get(0).getId();
//        }
//        assertNotEquals(firstPort, currentPort, "The connection should be replaced despite frequent requests");
//    }

    @Test
    void passesAccountIdToTransactionsService() {
        given()
          .when().get("/accounts/2001/transactions")
          .then()
             .statusCode(200)
             .body("size()", is(1))
             .body("[0].id", is("t-2001"))
             .body("[0].accountId", is("2001"));
    }

    @Test
    void returnsEmptyListForUnknownAccount() {
        given()
          .when().get("/accounts/unknown/transactions")
          .then()
             .statusCode(200)
             .body("$", empty());
    }

    @Test
    void accountsDoNotExposeInstanceName() {
        given()
          .when().get("/accounts")
          .then()
             .statusCode(200)
             .body("[0].instanceName", nullValue());
    }

    @Test
    void returnsInstanceNameFromTransactionsService() {
        given()
          .when().get("/accounts/instance-name")
          .then()
             .statusCode(200)
             .body(is("Serving Transaction from instance: test"));
    }
}
