package org.grobid.service.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.apache.commons.io.IOUtils;
import org.grobid.core.utilities.GrobidConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GrobidQuantitiesConfiguration extends Configuration {

    public static final Logger LOGGER = LoggerFactory.getLogger(GrobidQuantitiesConfiguration.class);

    private String grobidHome;

    @JsonProperty
    private String corsAllowedOrigins = "*";
    @JsonProperty
    private String corsAllowedMethods = "OPTIONS,GET,PUT,POST,DELETE,HEAD";
    @JsonProperty
    private String corsAllowedHeaders = "X-Requested-With,Content-Type,Accept,Origin";


    // Version
    private static String VERSION = null;
    private static String REVISION = null;
    private static final String UNKNOWN_VERSION_STR = "unknown";
    private static final String GROBID_VERSION_FILE = "/version.txt";
    private static final String GROBID_REVISION_FILE = "/revision.txt";


    private List<GrobidConfig.ModelParameters> models = new ArrayList<>();
    private int maxParallelRequests;

    private String cleanlpModelPath = "resources/cleanlp/models";

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

    public List<GrobidConfig.ModelParameters> getModels() {
        return models;
    }

    public void setModels(List<GrobidConfig.ModelParameters> models) {
        this.models = models;
    }

    public int getMaxParallelRequests() {
        if (this.maxParallelRequests == 0) {
            this.maxParallelRequests = Runtime.getRuntime().availableProcessors();
        }
        return this.maxParallelRequests;
    }

    public static String getVersion() {
        if (VERSION != null) {
            return VERSION;
        }
        synchronized (GrobidProperties.class) {
            if (VERSION == null) {
                VERSION = readFromFile(GROBID_VERSION_FILE);
            }
        }
        return VERSION;
    }

    public static String getRevision() {
        if (REVISION != null) {
            return REVISION;
        }
        synchronized (GrobidProperties.class) {
            if (REVISION == null) {
                REVISION = readFromFile(GROBID_REVISION_FILE);
            }
        }
        return REVISION;
    }

    private static String readFromFile(String filePath) {
        String grobidVersion = UNKNOWN_VERSION_STR;
        try (InputStream is = GrobidProperties.class.getResourceAsStream(filePath)) {
            grobidVersion = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Cannot read the version from resources", e);
        }
        return grobidVersion;
    }

    public String getCleanlpModelPath() {
        return cleanlpModelPath;
    }

    public void setCleanlpModelPath(String cleanlpModelPath) {
        this.cleanlpModelPath = cleanlpModelPath;
    }
}
