package org.grobid.service.configuration;

import io.dropwizard.Configuration;

public class GrobidQuantitiesConfiguration extends Configuration {

    private String grobidHome;
    private String chemspotUrl;
    private String grobidQuantitiesUrl;

    public String getGrobidHome() {
        return grobidHome;
    }

    public void setGrobidHome(String grobidHome) {
        this.grobidHome = grobidHome;
    }

    public String getChemspotUrl() {
        return chemspotUrl;
    }

    public void setChemspotUrl(String chemspotUrl) {
        this.chemspotUrl = chemspotUrl;
    }

    public String getGrobidQuantitiesUrl() {
        return grobidQuantitiesUrl;
    }

    public void setGrobidQuantitiesUrl(String grobidQuantitiesUrl) {
        this.grobidQuantitiesUrl = grobidQuantitiesUrl;
    }
}
