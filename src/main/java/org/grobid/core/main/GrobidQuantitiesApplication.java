package org.grobid.core.main;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.ArrayUtils;
import org.grobid.service.QuantitiesServiceModule;
import org.grobid.service.command.BatchProcessingCommand;
import org.grobid.service.command.TrainingGenerationCommand;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
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
//        bootstrap.addCommand(new BatchProcessingCommand());
    }

    @Override
    public void run(GrobidQuantitiesConfiguration configuration, Environment environment) {
        environment.jersey().setUrlPattern(RESOURCES + "/*");
    }

    public static void main(String[] args) throws Exception {
        if (ArrayUtils.getLength(args) < 2) {
            LOGGER.warn("Expected 2 arguments: [0]-server, [1]-<path to config.yaml>. Trying inferring configuration");

            String foundConf = null;
            for (String p : DEFAULT_CONF_LOCATIONS) {
                File confLocation = new File(p).getAbsoluteFile();
                if (confLocation.exists()) {
                    foundConf = confLocation.getAbsolutePath();
                    LOGGER.info("Found conf path: " + foundConf);
                    break;
                }
            }

            if (foundConf != null) {
                LOGGER.warn("Running with default arguments: \"server\" \"" + foundConf + "\"");
                args = new String[]{"server", foundConf};
            } else {
                throw new RuntimeException("No explicit config provided and cannot find in one of the default locations: "
                        + Arrays.toString(DEFAULT_CONF_LOCATIONS));
            }
        }

        LOGGER.info("Configuration file: {}", new File(args[1]).getAbsolutePath());
        new GrobidQuantitiesApplication().run(args);
    }
}
