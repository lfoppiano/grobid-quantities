package org.grobid.service;

import com.google.inject.Provides;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.grobid.core.engines.QuantitiesEngine;
import org.grobid.core.engines.QuantityParser;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.grobid.service.controller.AnnotationController;
import org.grobid.service.controller.HealthCheck;
import org.grobid.service.exceptions.mapper.GrobidExceptionMapper;
import org.grobid.service.exceptions.mapper.GrobidExceptionsTranslationUtility;
import org.grobid.service.exceptions.mapper.GrobidServiceExceptionMapper;
import org.grobid.service.exceptions.mapper.WebApplicationExceptionMapper;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;


public class QuantitiesServiceModule extends DropwizardAwareModule<GrobidQuantitiesConfiguration> {

    @Override
    public void configure() {
        // -- Generic modules --
        bind(GrobidEngineInitialiser.class);
        bind(HealthCheck.class);

        //Services
        bind(QuantityParser.class);
        bind(QuantitiesEngine.class);

        //REST
        bind(AnnotationController.class);

        //Exception Mappers - directly imported from Grobid
        bind(GrobidServiceExceptionMapper.class);
        bind(GrobidExceptionsTranslationUtility.class);
        bind(GrobidExceptionMapper.class);
        bind(WebApplicationExceptionMapper.class);
    }

    @Provides
    Client provideClient() {
        return ClientBuilder.newClient();
    }

}