package org.grobid.service;

import com.sun.jersey.spi.resource.Singleton;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    private static final String XML = "xml";
    private static final String PDF = "pdf";

    public QuantityRestService() {
        LOGGER.info("Init Servlet QuantityRestService.");
        LOGGER.info("Init lexicon and KB resources.");
        try {
            InitialContext intialContext = new javax.naming.InitialContext();
            String path2grobidHome = (String) intialContext.lookup("java:comp/env/org.grobid.home");
            String path2grobidProperty = (String) intialContext.lookup("java:comp/env/org.grobid.property");

            MockContext.setInitialContext(path2grobidHome, path2grobidProperty);

            System.out.println(path2grobidHome);
            System.out.println(path2grobidProperty);

            LibraryLoader.load();
            GrobidProperties.getInstance();
            QuantityLexicon.getInstance();
        } catch (final Exception exp) {
            System.err.println("GROBID Quantities initialisation failed: " + exp);
            exp.printStackTrace();
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

}