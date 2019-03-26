package org.grobid.service.configuration;

import io.dropwizard.Configuration;

public class GrobidQuantitiesConfiguration extends Configuration {

    private String grobidHome;

    public String getGrobidHome() {
        return grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

}
