package com.red;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AccountResourceTest {

    @Test
    void testRootEndpoint() {
        given()
          .when().get("/")
          .then()
             .statusCode(503)
             .body(is("quarkus accounts service is running\n"));
    }

    @Test
    void testListAccounts() {
        given()
          .when().get("/accounts")
          .then()
             .statusCode(200)
             .body("name", hasItem("Savings"));
    }

    @Test
    void testCreateAccount() {
        String accountName = "Holiday Fund " + UUID.randomUUID();

        String accountId = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
                  "name", accountName,
                  "currency", "USD",
                  "balance", 250.50
          ))
          .when().post("/accounts")
          .then()
             .statusCode(201)
             .body("id", notNullValue())
             .body("name", is(accountName))
             .body("currency", is("USD"))
             .extract().path("id");

        given()
          .when().get("/accounts/" + accountId)
          .then()
             .statusCode(200)
             .body("id", is(accountId))
             .body("name", is(accountName))
             .body("currency", is("USD"));
    }
}
