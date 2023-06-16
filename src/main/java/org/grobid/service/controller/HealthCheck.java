package org.grobid.service.controller;

import org.grobid.service.configuration.GrobidQuantitiesConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("health")
@Singleton
@Produces(APPLICATION_JSON)
public class HealthCheck extends com.codahale.metrics.health.HealthCheck {

    @Inject
    private GrobidQuantitiesConfiguration configuration;

    @Inject
    public HealthCheck(GrobidQuantitiesConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    public Response alive() {
        return Response.ok().build();
    }

    @Override
    protected Result check() throws Exception {
        return configuration.getGrobidHome() != null ? Result.healthy() :
                Result.unhealthy("Grobid home is null in the configuration");
    }
}
