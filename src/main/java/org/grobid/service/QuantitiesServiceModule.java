package org.grobid.service;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.hubspot.dropwizard.guicier.DropwizardAwareModule;
import org.grobid.core.engines.QuantitiesEngine;
import org.grobid.core.engines.QuantityParser;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.grobid.service.controller.AnnotationController;
import org.grobid.service.controller.HealthCheck;
import org.grobid.service.exceptions.mapper.GrobidExceptionMapper;
import org.grobid.service.exceptions.mapper.GrobidExceptionsTranslationUtility;
import org.grobid.service.exceptions.mapper.GrobidServiceExceptionMapper;
import org.grobid.service.exceptions.mapper.WebApplicationExceptionMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;


public class QuantitiesServiceModule extends DropwizardAwareModule<GrobidQuantitiesConfiguration> {

    @Override
    public void configure(Binder binder) {
        // -- Generic modules --
        binder.bind(GrobidEngineInitialiser.class);
        binder.bind(HealthCheck.class);

        //Services
        binder.bind(QuantityParser.class);
        binder.bind(QuantitiesEngine.class);

        //REST
        binder.bind(AnnotationController.class);

        //Exception Mappers - directly imported from Grobid
        binder.bind(GrobidServiceExceptionMapper.class);
        binder.bind(GrobidExceptionsTranslationUtility.class);
        binder.bind(GrobidExceptionMapper.class);
        binder.bind(WebApplicationExceptionMapper.class);
    }

    @Provides
    protected ObjectMapper getObjectMapper() {
        return getEnvironment().getObjectMapper();
    }

    @Provides
    protected MetricRegistry provideMetricRegistry() {
        return getMetricRegistry();
    }

    //for unit tests
    protected MetricRegistry getMetricRegistry() {
        return getEnvironment().metrics();
    }

    @Provides
    Client provideClient() {
        return ClientBuilder.newClient();
    }

}