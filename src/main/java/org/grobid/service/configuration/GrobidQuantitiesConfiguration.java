package org.grobid.service.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class GrobidQuantitiesConfiguration extends Configuration {

    private String grobidHome;

    @JsonProperty
    private String corsAllowedOrigins = "*";
    @JsonProperty
    private String corsAllowedMethods = "OPTIONS,GET,PUT,POST,DELETE,HEAD";
    @JsonProperty
    private String corsAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin";

    private int maxParallelRequests;

    public String getGrobidHome() {
        return grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public String getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public String getCorsAllowedMethods() {
        return corsAllowedMethods;
    }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
        this.corsAllowedMethods = corsAllowedMethods;
    }

    public String getCorsAllowedHeaders() {
        return corsAllowedHeaders;
    }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
        this.corsAllowedHeaders = corsAllowedHeaders;
    }

    public int getMaxParallelRequests() {
        if (this.maxParallelRequests == 0) {
            this.maxParallelRequests = Runtime.getRuntime().availableProcessors();
        }
        return this.maxParallelRequests;
    }
}
