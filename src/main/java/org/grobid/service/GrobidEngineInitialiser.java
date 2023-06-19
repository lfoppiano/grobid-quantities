package org.grobid.service;

import com.google.common.collect.ImmutableList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class GrobidEngineInitialiser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidEngineInitialiser.class);


    @Inject
    public GrobidEngineInitialiser(GrobidQuantitiesConfiguration configuration) {
        LOGGER.info("Initialising Grobid");
        GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(ImmutableList.of(configuration.getGrobidHome()));
        GrobidProperties.getInstance(grobidHomeFinder);
        GrobidProperties.setContextExecutionServer(true);
        configuration.getModels().stream().forEach(GrobidProperties::addModel);
        
        LibraryLoader.load();
    }
}
