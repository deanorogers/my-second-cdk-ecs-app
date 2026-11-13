package com.red;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/accounts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    TranxEasyClient tranxClient;

    @GET
    public List<Account> listAccounts() {
        return accountService.list();
    }

    @GET
    @Path("/{id}")
    public Response getAccount(@PathParam("id") String id) {
        return accountService.findById(id)
                .map(account -> Response.ok(account).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Account not found"))
                        .build());
    }

    @GET
    @Path("/{id}/transactions")
    public CompletionStage<List<Tranx>> getTransactions(@PathParam("id") String id) {
        return accountService.findById(id)
                .map(account -> tranxClient.getTransactions(account.getId()).toCompletionStage())
                .orElseGet(() -> CompletableFuture.completedFuture(List.of()));
    }

    @POST
    public Response createAccount(Account account) {
        try {
            Account created = accountService.create(account);
            URI location = UriBuilder.fromPath("/accounts/{id}").build(created.getId());
            return Response.created(location).entity(created).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", exception.getMessage()))
                    .build();
        } catch (IllegalStateException exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/instance-name")
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> getInstanceName() {
        return tranxClient.getInstanceName().toCompletionStage();
    }
}
