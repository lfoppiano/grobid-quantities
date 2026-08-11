package org.grobid.service.main;

import io.dropwizard.util.Duration;
import org.eclipse.jetty.server.handler.QoSHandler;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The 503s reported in https://github.com/lfoppiano/grobid-quantities/issues/159 came from the
 * request limiter, not from the connector's {@code idleTimeout}. The limiter's bounds used to be
 * implicit - and they differ between the Jetty 9 {@code QoSFilter} that produced the reported
 * behaviour and the Jetty 12 {@code QoSHandler} in use now - so they are now set explicitly from
 * the configuration. These tests pin that wiring.
 */
class GrobidQuantitiesApplicationQoSTest {

    @Test
    void defaults_shouldQueueWithoutATimeLimit() {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();

        QoSHandler qos = GrobidQuantitiesApplication.buildQoSHandler(configuration);

        // 0 parallel requests means one slot per processor, never "no limit"
        assertEquals(Runtime.getRuntime().availableProcessors(), qos.getMaxRequestCount());
        assertEquals(1024, qos.getMaxSuspendedRequestCount());
        // the important one: a queued request is not dropped after a fixed delay any more
        assertTrue(qos.getMaxSuspend().isZero());
        assertEquals(503, qos.getRejectStatusCode());
    }

    @Test
    void configuredValues_shouldReachTheHandler() {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();
        configuration.setMaxParallelRequests(4);
        configuration.setRequestQueueMaxSize(32);
        configuration.setRequestQueueMaxWait(Duration.seconds(120));
        configuration.setRequestQueueRejectStatus(429);

        QoSHandler qos = GrobidQuantitiesApplication.buildQoSHandler(configuration);

        assertEquals(4, qos.getMaxRequestCount());
        assertEquals(32, qos.getMaxSuspendedRequestCount());
        assertEquals(java.time.Duration.ofSeconds(120), qos.getMaxSuspend());
        assertEquals(429, qos.getRejectStatusCode());
    }

    @Test
    void negativeQueueLength_shouldMeanUnbounded() {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();
        configuration.setRequestQueueMaxSize(-1);

        QoSHandler qos = GrobidQuantitiesApplication.buildQoSHandler(configuration);

        assertTrue(qos.getMaxSuspendedRequestCount() < 0);
    }

    @Test
    void aNullTimeout_shouldMeanUnbounded() {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();
        configuration.setRequestQueueMaxWait(null);

        QoSHandler qos = GrobidQuantitiesApplication.buildQoSHandler(configuration);

        assertTrue(qos.getMaxSuspend().isZero());
    }
}
