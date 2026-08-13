package org.grobid.service.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The payload behind the green/red indicator of the web interface.
 *
 * <p>The model state comes from the {@code TaggerFactory} statics, which other tests in the same
 * JVM may have populated, so the assertions here are on the <em>relations</em> the payload has to
 * satisfy rather than on absolute values - except where the configuration alone decides.
 */
class HealthCheckTest {

    private HealthCheck healthCheckWithGrobidHome(String grobidHome) {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();
        configuration.setGrobidHome(grobidHome);
        return new HealthCheck(configuration);
    }

    @Test
    void readiness_shouldFollowGrobidHomeAndModelFailures() {
        ObjectNode status = healthCheckWithGrobidHome("../grobid-home").buildStatus();

        boolean expected = status.get("grobidHomeConfigured").asBoolean()
            && (status.get("models").get("totalFailed").asInt() == 0);

        assertEquals(expected, status.get("ready").asBoolean());
    }

    /**
     * The models are loaded on the first request that needs them, so an empty
     * {@code models.loaded} must not by itself make the service look broken.
     */
    @Test
    void readiness_shouldNotRequireAnyModelToBeLoadedYet() {
        ObjectNode status = healthCheckWithGrobidHome("../grobid-home").buildStatus();

        if (status.get("models").get("totalFailed").asInt() == 0) {
            assertTrue(status.get("ready").asBoolean(),
                "no model failed, so the service must be ready whatever is loaded");
        }
    }

    @Test
    void theStatusLabel_shouldMatchTheReadyFlag() {
        ObjectNode status = healthCheckWithGrobidHome("../grobid-home").buildStatus();

        assertEquals(status.get("ready").asBoolean() ? "healthy" : "unhealthy",
            status.get("status").asText());
    }

    @Test
    void aServiceWithoutGrobidHome_shouldNotBeReady() {
        ObjectNode status = healthCheckWithGrobidHome(null).buildStatus();

        assertFalse(status.get("ready").asBoolean());
        assertEquals("unhealthy", status.get("status").asText());
        assertFalse(status.get("grobidHomeConfigured").asBoolean());
    }

    @Test
    void theResponse_shouldCarryTheModelBreakdown() {
        ObjectNode status = healthCheckWithGrobidHome("../grobid-home").buildStatus();

        ObjectNode models = (ObjectNode) status.get("models");
        assertTrue(models.has("loaded"));
        assertTrue(models.has("failed"));
        assertEquals(models.get("loaded").size(), models.get("totalLoaded").asInt());
        assertEquals(models.get("failed").size(), models.get("totalFailed").asInt());
    }

    /**
     * 503 rather than 500: an orchestrator reads it as "do not route to me yet".
     */
    @Test
    void anUnreadyService_shouldAnswer503() {
        Response response = healthCheckWithGrobidHome(null).alive();

        assertEquals(503, response.getStatus());
        assertTrue(response.getEntity().toString().contains("\"ready\":false"));
    }

    @Test
    void theStatusCode_shouldFollowTheReadyFlag() {
        HealthCheck healthCheck = healthCheckWithGrobidHome("../grobid-home");

        int expected = healthCheck.buildStatus().get("ready").asBoolean() ? 200 : 503;

        assertEquals(expected, healthCheck.alive().getStatus());
    }

    @Test
    void theDropwizardCheck_shouldAgreeWithTheEndpoint() {
        HealthCheck configured = healthCheckWithGrobidHome("../grobid-home");
        assertEquals(configured.buildStatus().get("ready").asBoolean(),
            configured.check().isHealthy());

        assertFalse(healthCheckWithGrobidHome(null).check().isHealthy());
    }
}
