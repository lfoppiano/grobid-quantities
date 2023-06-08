package org.grobid.service.main;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlets.QoSFilter;
import org.grobid.service.QuantitiesServiceModule;
import org.grobid.service.command.PrepareDelftTrainingCommand;
import org.grobid.service.command.RunTrainingCommand;
import org.grobid.service.command.UnitBatchProcessingCommand;
import org.grobid.service.command.TrainingGenerationCommand;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import java.util.List;

public class GrobidQuantitiesApplication extends Application<GrobidQuantitiesConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidQuantitiesApplication.class);
    private static final String[] DEFAULT_CONF_LOCATIONS = {"config/config.yml", "resources/config/config.yml"};

    private static final String RESOURCES = "/service";

    @Override
    public String getName() {
        return "grobid-quantities";
    }

    private List<? extends Module> getGuiceModules() {
        return Lists.newArrayList(new QuantitiesServiceModule());
    }

    @Override
    public void initialize(Bootstrap<GrobidQuantitiesConfiguration> bootstrap) {
        GuiceBundle<GrobidQuantitiesConfiguration> guiceBundle = GuiceBundle.defaultBuilder(GrobidQuantitiesConfiguration.class)
                .modules(getGuiceModules())
                .build();
        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new AssetsBundle("/web", "/", "index.html", "assets"));
        bootstrap.addCommand(new TrainingGenerationCommand());
        bootstrap.addCommand(new UnitBatchProcessingCommand());
        bootstrap.addCommand(new RunTrainingCommand());
        bootstrap.addCommand(new PrepareDelftTrainingCommand());
    }

    @Override
    public void run(GrobidQuantitiesConfiguration configuration, Environment environment) {
        LOGGER.info("Service config={}", configuration);
        environment.jersey().setUrlPattern(RESOURCES + "/*");

        String allowedOrigins = configuration.getCorsAllowedOrigins();
        String allowedMethods = configuration.getCorsAllowedMethods();
        String allowedHeaders = configuration.getCorsAllowedHeaders();

        // Enable CORS headers
        final FilterRegistration.Dynamic cors =
            environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, allowedMethods);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, allowedHeaders);

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, RESOURCES + "/*");

        // Enable QoS filter
        final FilterRegistration.Dynamic qos = environment.servlets().addFilter("QOS", QoSFilter.class);
        qos.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        qos.setInitParameter("maxRequests", String.valueOf(configuration.getMaxParallelRequests()));
        qos.setInitParameter("waitMs", String.valueOf(configuration.getWaitMs()));
        
    }

    public static void main(String[] args) throws Exception {
        new GrobidQuantitiesApplication().run(args);
    }
}
