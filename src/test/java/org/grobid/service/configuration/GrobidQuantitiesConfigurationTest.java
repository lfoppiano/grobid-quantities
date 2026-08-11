package org.grobid.service.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The application enables {@code FAIL_ON_UNKNOWN_PROPERTIES}, so a key the configuration class
 * does not know about - a typo, or a setting documented but never wired - stops the service at
 * startup. These tests parse the shipped configuration files the same way.
 */
class GrobidQuantitiesConfigurationTest {

    private GrobidQuantitiesConfiguration parse(String path) throws Exception {
        ObjectMapper mapper = Jackson.newObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return new YamlConfigurationFactory<>(
            GrobidQuantitiesConfiguration.class, Validators.newValidator(), mapper, "dw")
            .build(new FileConfigurationSourceProvider(), path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"resources/config/config.yml", "resources/config/config-docker.yml"})
    void shippedConfiguration_shouldParse(String path) throws Exception {
        assertTrue(new File(path).exists(), path + " not found - is the working directory the project root?");

        GrobidQuantitiesConfiguration configuration = parse(path);

        // 0 in the file, resolved to one slot per processor
        assertEquals(Runtime.getRuntime().availableProcessors(), configuration.getMaxParallelRequests());
        assertEquals(1024, configuration.getMaxQueuedRequests());
        assertEquals(503, configuration.getMaxQueuedRequestsRejectStatus());
    }

    /**
     * See https://github.com/lfoppiano/grobid-quantities/issues/159: a queued request waits for
     * its slot indefinitely unless the deployment says otherwise.
     */
    @ParameterizedTest
    @ValueSource(strings = {"resources/config/config.yml", "resources/config/config-docker.yml"})
    void shippedConfiguration_shouldNotBoundTheQueueingTime(String path) throws Exception {
        GrobidQuantitiesConfiguration configuration = parse(path);

        assertEquals(0, configuration.getMaxQueuedRequestTimeout().toMilliseconds());
        assertTrue(configuration.getMaxQueuedRequestTimeoutAsJavaDuration().isZero());
    }

    @Test
    void aQueueingTimeout_shouldBeReadInAnyTimeUnit() throws Exception {
        GrobidQuantitiesConfiguration configuration = new GrobidQuantitiesConfiguration();
        configuration.setMaxQueuedRequestTimeout(io.dropwizard.util.Duration.parse("2 minutes"));

        assertEquals(java.time.Duration.ofMinutes(2), configuration.getMaxQueuedRequestTimeoutAsJavaDuration());
    }
}
