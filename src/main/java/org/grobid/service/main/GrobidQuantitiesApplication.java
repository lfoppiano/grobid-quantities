package org.grobid.service.main;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.grobid.service.QuantitiesServiceModule;
import org.grobid.service.command.UnitBatchProcessingCommand;
import org.grobid.service.command.TrainingGenerationCommand;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    @Override
    public void run(GrobidQuantitiesConfiguration configuration, Environment environment) {
        environment.jersey().setUrlPattern(RESOURCES + "/*");
    }

    public static void main(String[] args) throws Exception {
        new GrobidQuantitiesApplication().run(args);
    }
}
