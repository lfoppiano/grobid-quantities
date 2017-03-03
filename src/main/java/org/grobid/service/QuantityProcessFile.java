package org.grobid.service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.nio.charset.Charset;
import java.util.*;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

import org.grobid.core.layout.Page;
import org.grobid.core.data.Measurement;
import org.grobid.core.engines.QuantityParser;
import org.grobid.core.document.Document;
import org.grobid.core.engines.Engine;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.IOUtilities;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.Pair;

/**
 * 
 * @author Patrice
 * 
 */
public class QuantityProcessFile {

    /**
     * The class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityProcessFile.class);


    /**
     * Uploads the origin PDF, process it and return PDF annotations for references in JSON.
     *
     * @param inputStream the data of origin PDF
     * @return a response object containing the JSON annotations
     */
    public static Response processPDFAnnotation(final InputStream inputStream) {
        LOGGER.debug(methodLogIn()); 
        Response response = null;
        File originFile = null;
        QuantityParser parser = QuantityParser.getInstance();
        Engine engine = null;

        try {
            LibraryLoader.load();
            engine = GrobidFactory.getInstance().getEngine();
            originFile = IOUtilities.writeInputFile(inputStream);
            GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().build();

            if (originFile == null) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
            } else {
                long start = System.currentTimeMillis();
                Pair<List<Measurement>, Document> extractedEntities = parser.extractQuantitiesPDF(originFile);
                long end = System.currentTimeMillis();

                Document doc = extractedEntities.getB();
                List<Measurement> measurements = extractedEntities.getA();
                StringBuilder json = new StringBuilder();
                json.append("{ ");

                // page height and width
                json.append("\"pages\":[");
                List<Page> pages = doc.getPages();
                boolean first = true;
                for(Page page : pages) {
                    if (first) 
                        first = false;
                    else
                        json.append(", ");    
                    json.append("{\"page_height\":" + page.getHeight());
                    json.append(", \"page_width\":" + page.getWidth() + "}");
                }

                json.append("], \"measurements\":[");
                first = true;
                for(Measurement entity : measurements) {
                    if (!first)
                        json.append(", ");
                    else
                        first = false;
                    json.append(entity.toJson());
                }
                
                json.append("]");
                json.append(", \"runtime\" :" + (end-start));
                json.append("}");

                if (json != null) {
                    response = Response
                            .ok()
                            .type("application/json")
                            .entity(json.toString())
                            .build();
                }
                else {
                    response = Response.status(Status.NO_CONTENT).build();
                }
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an instance of QuantityParser. Sending service unavailable.");
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception exp) {
            LOGGER.error("An unexpected exception occurs. ", exp);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(exp.getMessage()).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    /**
     * @return
     */
    private static String methodLogIn() {
        return ">> " + QuantityProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    /**
     * @return
     */
    private static String methodLogOut() {
        return "<< " + QuantityProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}