package org.grobid.service;

import java.io.InputStream;
import java.io.IOException;
import java.util.List; 

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;

/**import javax.naming.Context;*/
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.main.LibraryLoader;

/**
 * 
 * RESTful service for GROBID quantity extension.
 *
 * @author Patrice
 *
 */

@Singleton
@Path(QuantityPaths.PATH_QUANTITY)
public class QuantityRestService implements QuantityPaths {

    /**
     * The class Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityRestService.class);

    private static final String TEXT = "text";  
    private static final String XML = "xml";
    private static final String PDF = "pdf";

    public QuantityRestService() {
        LOGGER.info("Init Servlet QuantityRestService.");
        LOGGER.info("Init lexicon and KB resources.");
        try {
            InitialContext intialContext = new javax.naming.InitialContext();
            String path2grobidHome = (String)intialContext.lookup("java:comp/env/org.grobid.home");
            String path2grobidProperty = (String)intialContext.lookup("java:comp/env/org.grobid.property");

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

    /**
     * 
     */
    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public Response processText_post(@FormParam(TEXT) String text) {
        System.out.println(text);
        return QuantityProcessString.processText(text);
    }

    /**
     * 
     */
    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @PUT
    public Response processText(@QueryParam(TEXT) String text) {
        System.out.println(text);
        return QuantityProcessString.processText(text);
    }

    /**
     * 
     */
    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @GET
    public Response processText_get(@QueryParam(TEXT) String text) {
        System.out.println(text);
        return QuantityProcessString.processText(text);
    }

}