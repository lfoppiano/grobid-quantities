package org.grobid.service;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * RESTful service for GROBID quantity extension.
 *
 * @author Patrice
 */
@Singleton
@Path(QuantityPaths.PATH_QUANTITY)
public class QuantityRestService implements QuantityPaths {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityRestService.class);

    private static final String TEXT = "text";
    private static final String JSON = "text";
    private static final String XML = "xml";
    private static final String PDF = "pdf";
    private static final String INPUT = "input";

    public QuantityRestService() {
        LOGGER.info("Init Servlet QuantityRestService.");
        LOGGER.info("Init lexicon and KB resources.");
        try {
            InitialContext intialContext = new javax.naming.InitialContext();
            LibraryLoader.load();
            GrobidProperties.getInstance();
            QuantityLexicon.getInstance();
        } catch (final Exception exp) {
            LOGGER.error("GROBID Quantities initialisation failed: ", exp);
        }

        LOGGER.info("Init of Servlet QuantityRestService finished.");
    }

    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response processText_post(@FormParam(TEXT) String text) {
        LOGGER.info(text);
        return QuantityProcessString.processText(text);
    }

    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response processText_get(@QueryParam(TEXT) String text) {
        LOGGER.info(text);
        return QuantityProcessString.processText(text);
    }

    @Path(PATH_PARSE_MEASURE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response parseMeasure_post(String json) {
        LOGGER.info("parseMeasure_post: " + json);
        return QuantityProcessJson.parseMeasure(json);
    }

    @Path(PATH_ANNOTATE_QUANTITY_PDF)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @POST
    public Response processPDFAnnotation(@FormDataParam(INPUT) InputStream inputStream) {
        return QuantityProcessFile.processPDFAnnotation(inputStream);
    }

    /**
     * @see org.grobid.service.QuantitiesRestProcessGeneric#isAlive()
     */
    @Path(PATH_IS_ALIVE)
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public Response isAlive() {
        return QuantitiesRestProcessGeneric.isAlive();
    }
}
