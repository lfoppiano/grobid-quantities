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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.io.*;

/**
 * @author Patrice
 */
public class QuantityProcessJson {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuantityProcessJson.class);

    public static Response parseMeasure(String json) {
        LOGGER.debug(methodLogIn());
        Response response = null;

        try {
            // try to parse the json query
            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonAnnotation = mapper.readTree(json);
            if ((jsonAnnotation == null) || (jsonAnnotation.isMissingNode())) {
                LOGGER.error("JSON input appears empty.");
                response = Response.status(Status.BAD_REQUEST).build();
            } else {
                long start = System.currentTimeMillis();
                // get the provided parameters
                String fromValue = null;
                String toValue = null;
                String unitValue = null;
                String typeValue = null;
                /*JsonNode from = jsonAnnotation.findPath("from");
                if ((from != null) && (!from.isMissingNode()))
                    fromValue = from.textValue();

                JsonNode to = jsonAnnotation.findPath("to");
                if ((to != null) && (!to.isMissingNode()))
                    toValue = to.textValue();

                JsonNode unit = jsonAnnotation.findPath("unit");
                if ((unit != null) && (!unit.isMissingNode()))
                    unitValue = unit.textValue();

                JsonNode type = jsonAnnotation.findPath("type");
                if ((type != null) && (!type.isMissingNode()))
                    typeValue = type.textValue();

                UnitUtilities.Measurement_Type theType = null;


                UnitUtilities.Measurement_Type.VALUE;

                Measurement measurement = new Measurement(theType);

                
                long end = System.currentTimeMillis();

                StringBuilder jsonBuilder = null;
                
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
           



                response = Response.status(Status.NO_CONTENT).build();

                if (jsonBuilder != null) {
                    //System.out.println(jsonBuilder.toString());
                    response = Response.status(Status.OK).entity(jsonBuilder.toString())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON+"; charset=UTF-8" )
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .build();
                }*/

                response = Response.status(Status.NO_CONTENT).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            String message = "Error in " + e.getStackTrace()[0].toString();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    private static String methodLogIn() {
        return ">> " + QuantityProcessJson.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    private static String methodLogOut() {
        return "<< " + QuantityProcessJson.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}