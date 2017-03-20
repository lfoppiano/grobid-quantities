package org.grobid.service;

import org.grobid.core.data.Measurement;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;
import org.grobid.core.engines.QuantityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.ArrayList;
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
            QuantityParser quantityParser = QuantityParser.getInstance();

            JsonNode jsonAnnotation = mapper.readTree(json);
            if ((jsonAnnotation == null) || (jsonAnnotation.isMissingNode())) {
                LOGGER.error("JSON input appears empty.");
                response = Response.status(Status.BAD_REQUEST)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON+"; charset=UTF-8" )
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                            .build();
            } else {
                long start = System.currentTimeMillis();
                // get the provided parameters
                String fromValue = null;
                String toValue = null;
                String unitValue = null;
                String typeValue = null;
                JsonNode from = jsonAnnotation.findPath("from");
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
                String atomicValue = null;
                if ( ( (fromValue == null) || (fromValue.length() == 0) ) &&
                     ( (toValue == null) || (toValue.length() == 0) ) ) {
                    response = Response.status(Status.NO_CONTENT).build();
                } else if ( (fromValue == null) || (fromValue.length() == 0) ) {
                    atomicValue = toValue;
                    theType = UnitUtilities.Measurement_Type.VALUE;
                } else if ( (toValue == null) || (toValue.length() ==0) ) {
                    atomicValue = fromValue;
                    theType = UnitUtilities.Measurement_Type.VALUE;
                } else 
                    theType = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX;

                if (theType != null) {
                    Measurement measurement = new Measurement(theType);
                    
                    if (theType == UnitUtilities.Measurement_Type.VALUE) {
                        Quantity quantity = new Quantity();
                        quantity.setRawValue(atomicValue);
                        quantity.setRawUnit(new Unit(unitValue));
                        // note: there is no way to enforce the measurement type here
                        // it will be infered from the raw unit
                        measurement.setAtomicQuantity(quantity); 
                    } else if (theType == UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX) {
                        Quantity quantityLeast = new Quantity();
                        Quantity quantityMost = new Quantity();
                        quantityLeast.setRawValue(fromValue);
                        quantityLeast.setRawUnit(new Unit(unitValue));
                        quantityMost.setRawValue(toValue);
                        quantityMost.setRawUnit(new Unit(unitValue));
                        measurement.setQuantityLeast(quantityLeast); 
                        measurement.setQuantityMost(quantityMost); 
                    }
                    List<Measurement> measurements = new ArrayList<Measurement>();
                    measurements.add(measurement);
                    measurements = quantityParser.normalizeMeasurements(measurements);

                    long end = System.currentTimeMillis();

                    StringBuilder jsonBuilder = null;
                    
                    jsonBuilder = new StringBuilder();
                    jsonBuilder.append("{ ");
                    jsonBuilder.append("\"runtime\" : " + (end - start));
                    jsonBuilder.append(", \"measurements\" : [ ");
                    boolean first = true;
                    for (Measurement measure : measurements) {
                        if (first)
                            first = false;
                        else
                            jsonBuilder.append(", ");
                        jsonBuilder.append(measure.toJson());
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
                    }
                } else
                    response = Response.status(Status.NO_CONTENT).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Status.SERVICE_UNAVAILABLE)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON+"; charset=UTF-8" )
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                            .build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            String message = "Error in " + e.getStackTrace()[0].toString();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(message)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON+"; charset=UTF-8" )
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                            .build();
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