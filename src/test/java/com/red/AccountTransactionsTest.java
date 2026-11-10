package com.red;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
@QuarkusTestResource(value = TransactionServer.class, restrictToAnnotatedClass = true)
class AccountTransactionsTest {

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
