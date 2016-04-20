package org.grobid.service;

import org.grobid.core.data.Measurement;
import org.grobid.core.engines.QuantityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Patrice
 */
public class QuantityProcessString {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityProcessString.class);


    public static Response processText(String text) {
        LOGGER.debug(methodLogIn());
        Response response = null;

        try {
            LOGGER.debug(">> set raw text for stateless quantity service'...");
            long start = System.currentTimeMillis();
            QuantityParser quantityParser = QuantityParser.getInstance();
            List<Measurement> measurements = quantityParser.extractQuantities(text);
            long end = System.currentTimeMillis();

            StringBuilder jsonBuilder = null;
            if (measurements != null) {
                jsonBuilder = new StringBuilder();
                jsonBuilder.append("{ ");
                jsonBuilder.append("\"runtime\" : " + (end - start));
                jsonBuilder.append(", \"measurements\" : [ ");
                boolean first = true;
                for (Measurement measurement : measurements) {
                    if (first)
                        first = false;
                    else
                        jsonBuilder.append(", ");
                    jsonBuilder.append(measurement.toJson());
                }
                jsonBuilder.append("] }");
            } else
                response = Response.status(Status.NO_CONTENT).build();

            System.out.println(jsonBuilder.toString());

            if (jsonBuilder != null) {
                response = Response.status(Status.OK).entity(jsonBuilder.toString())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                        .build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getCause().getMessage()).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    private static String methodLogIn() {
        return ">> " + QuantityProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    private static String methodLogOut() {
        return "<< " + QuantityProcessString.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}