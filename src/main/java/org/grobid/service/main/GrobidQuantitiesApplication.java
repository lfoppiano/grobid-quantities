package org.grobid.service.main;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.inject.AbstractModule;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.forms.MultiPartBundle;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.server.handler.QoSHandler;
import org.grobid.service.QuantitiesServiceModule;
import org.grobid.service.command.PrepareDelftTrainingCommand;
import org.grobid.service.command.RunTrainingCommand;
import org.grobid.service.command.TrainingGenerationCommand;
import org.grobid.service.command.UnitBatchProcessingCommand;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.grobid.service.controller.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GrobidQuantitiesApplication extends Application<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidQuantitiesApplication.class);
    private static final String[] DEFAULT_CONF_LOCATIONS = {"config/config.yml", "resources/config/config.yml"};

    private static final String RESOURCES = "/service";

    @Override
    public String getName() {
        return "grobid-quantities";
    }

    private AbstractModule getGuiceModules() {
        return new QuantitiesServiceModule();
    }

    @Override
    public void initialize(Bootstrap<GrobidQuantitiesConfiguration> bootstrap) {
        GuiceBundle guiceBundle = GuiceBundle.builder()
            .modules(getGuiceModules())
            .build();

        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        bootstrap.addBundle(new AssetsBundle("/web", "/", "index.html", "assets"));
        bootstrap.addCommand(new TrainingGenerationCommand());
        bootstrap.addCommand(new UnitBatchProcessingCommand());
        bootstrap.addCommand(new RunTrainingCommand());
        bootstrap.addCommand(new PrepareDelftTrainingCommand());
    }

    @Override
    public void run(GrobidQuantitiesConfiguration configuration, Environment environment) {

        environment.healthChecks().register("health-check", new HealthCheck(configuration));

        LOGGER.info("Service config={}", configuration);
        environment.jersey().setUrlPattern(RESOURCES + "/*");

        // Enable CORS via Jetty 12's CrossOriginHandler (replaces the removed
        // org.eclipse.jetty.servlets.CrossOriginFilter). Inserted above the application
        // context so it applies to all served paths.
        CrossOriginHandler cors = new CrossOriginHandler();
        cors.setAllowedOriginPatterns(toSet(configuration.getCorsAllowedOrigins()));
        cors.setAllowedMethods(toSet(configuration.getCorsAllowedMethods()));
        cors.setAllowedHeaders(toSet(configuration.getCorsAllowedHeaders()));
        cors.setAllowCredentials(false);
        environment.getApplicationContext().insertHandler(cors);

        // Limit concurrent requests via Jetty 12's QoSHandler (replaces the removed
        // org.eclipse.jetty.servlets.QoSFilter). A negative value disables the limit;
        // 0 means one slot per available processor, see getMaxParallelRequests().
        int maxParallelRequests = configuration.getMaxParallelRequests();
        if (maxParallelRequests > 0) {
            environment.getApplicationContext().insertHandler(buildQoSHandler(configuration));
        } else {
            LOGGER.info("Parallel requests are not limited (maxParallelRequests={})", maxParallelRequests);
        }
    }

    /**
     * Builds the handler enforcing {@code maxParallelRequests}. Requests over the limit are not
     * rejected straight away, they are queued ("suspended") until a slot frees up; the queue has
     * a bounded length and, optionally, a bounded waiting time, and a request that exceeds either
     * is rejected with {@code requestQueueRejectStatus} (503 by default).
     * <p>
     * All three bounds used to be implicit, which is what made the 503s of
     * https://github.com/lfoppiano/grobid-quantities/issues/159 so hard to read: the Jetty 9
     * {@code QoSFilter} this replaced suspended over-limit requests with the servlet default
     * async timeout of 30 seconds, so requests were rejected after ~30s no matter what the
     * connector's {@code idleTimeout} said. Jetty 12's defaults are different again (a 1024-long
     * queue and no waiting time limit), so we set them explicitly and log them.
     */
    static QoSHandler buildQoSHandler(GrobidQuantitiesConfiguration configuration) {
        QoSHandler qos = new QoSHandler();
        qos.setMaxRequestCount(configuration.getMaxParallelRequests());
        qos.setMaxSuspendedRequestCount(configuration.getRequestQueueMaxSize());
        qos.setMaxSuspend(configuration.getRequestQueueMaxWaitAsJavaDuration());
        qos.setRejectStatusCode(configuration.getRequestQueueRejectStatus());

        LOGGER.info("Limiting parallel requests: maxParallelRequests={}, requestQueueMaxSize={}, "
                + "requestQueueMaxWait={}, rejecting with {}",
            qos.getMaxRequestCount(),
            qos.getMaxSuspendedRequestCount() < 0 ? "unbounded" : qos.getMaxSuspendedRequestCount(),
            qos.getMaxSuspend().isZero() ? "unbounded" : qos.getMaxSuspend(),
            qos.getRejectStatusCode());

        return qos;
    }

    /**
     * Splits a comma-separated config value (e.g. "OPTIONS,GET,POST") into a set of trimmed,
     * non-empty entries preserving order, as expected by {@link CrossOriginHandler}.
     */
    private static Set<String> toSet(String csv) {
        if (csv == null) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void main(String[] args) throws Exception {
        new GrobidQuantitiesApplication().run(args);
    }
}
