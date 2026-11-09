package com.red;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "transaction-data-api")
public interface TranxClient {

    @GET
    @Path("/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    List<Tranx> getTransactions(@QueryParam("accountId") String accountId);

}
