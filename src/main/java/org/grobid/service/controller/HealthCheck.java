package org.grobid.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Health / diagnostic endpoint, modelled on Grobid's own {@code HealthResource}.
 *
 * <p>Besides the Dropwizard admin-style {@link #check()}, this exposes
 * {@code GET /service/health} returning a JSON document describing the state of the service:
 * whether grobid-home is configured, and which models are loaded or failed to load. The status
 * is {@code 200} when the service is ready and {@code 503} when it is not, so that an
 * orchestrator or a load balancer can take the instance out of rotation.
 *
 * <p><b>On readiness and lazily loaded models.</b> grobid-quantities loads its CRF models on the
 * first request that needs them, not at startup, so {@code models.loaded} is legitimately empty
 * on a freshly started service. Readiness therefore does <em>not</em> require any model to be
 * loaded - it requires that none has <em>failed</em>. Reporting a fresh service as unhealthy
 * until someone happens to send a request would make the indicator useless.
 */
@Path("health")
@Singleton
@Produces(APPLICATION_JSON)
public class HealthCheck extends com.codahale.metrics.health.HealthCheck {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final GrobidQuantitiesConfiguration configuration;

    @Inject
    public HealthCheck(GrobidQuantitiesConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    public Response alive() {
        ObjectNode root = buildStatus();

        Response.Status status = root.get("ready").asBoolean()
            ? Response.Status.OK
            : Response.Status.SERVICE_UNAVAILABLE;

        return Response.status(status).entity(root.toString()).type(APPLICATION_JSON).build();
    }

    /**
     * The payload of {@link #alive()}, also used by the tests.
     */
    ObjectNode buildStatus() {
        boolean grobidHomeConfigured = (configuration != null) && (configuration.getGrobidHome() != null);

        Map<String, String> loadedModels = TaggerFactory.getLoadedModels();
        Map<String, String> failedModels = TaggerFactory.getFailedModels();

        // note: no requirement on loadedModels being non-empty, see the class javadoc
        boolean ready = grobidHomeConfigured && !TaggerFactory.hasFailures();

        ObjectNode root = MAPPER.createObjectNode();
        root.put("status", ready ? "healthy" : "unhealthy");
        root.put("ready", ready);
        root.put("grobidHomeConfigured", grobidHomeConfigured);

        ObjectNode models = MAPPER.createObjectNode();
        models.set("loaded", asJson(loadedModels));
        models.set("failed", asJson(failedModels));
        models.put("totalLoaded", loadedModels.size());
        models.put("totalFailed", failedModels.size());
        root.set("models", models);

        return root;
    }

    private ObjectNode asJson(Map<String, String> models) {
        ObjectNode node = MAPPER.createObjectNode();
        for (Map.Entry<String, String> entry : models.entrySet()) {
            node.put(entry.getKey(), entry.getValue());
        }
        return node;
    }

    @Override
    protected Result check() {
        if ((configuration == null) || (configuration.getGrobidHome() == null)) {
            return Result.unhealthy("Grobid home is null in the configuration");
        }
        if (TaggerFactory.hasFailures()) {
            return Result.unhealthy("Models failed to load: "
                + String.join(", ", TaggerFactory.getFailedModels().keySet()));
        }
        return Result.healthy("%d model(s) loaded", TaggerFactory.getLoadedModels().size());
    }
}
