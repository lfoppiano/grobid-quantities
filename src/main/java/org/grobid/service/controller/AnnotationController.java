package org.grobid.service.controller;

import com.codahale.metrics.annotation.Timed;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.grobid.core.data.MeasurementsResponse;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.engines.QuantitiesEngine;
import org.grobid.core.engines.QuantityParser;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@Timed
@Singleton
@Path("/")
public class AnnotationController {
    private static final String PATH_IS_ALIVE = "isalive";

    private static final String PATH_QUANTITY_TEXT = "processQuantityText";
    private static final String PATH_UNITS_TEXT = "processUnitsText";
    private static final String PATH_QUANTITY_XML = "processQuantityXML";
    private static final String PATH_ANNOTATE_QUANTITY_PDF = "annotateQuantityPDF";
    private static final String PATH_PARSE_MEASURE = "parseMeasure";


    private QuantitiesEngine engine;

    @Inject
    public AnnotationController(GrobidQuantitiesConfiguration configuration,
                                QuantitiesEngine engine) {
        this.engine = engine;
    }

    @Path(PATH_IS_ALIVE)
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public Response isAlive() {
        Response response = null;
        try {

            String retVal = null;
            try {
                QuantityParser.getInstance();
                retVal = Boolean.valueOf(true).toString();
            } catch (Exception e) {
                retVal = Boolean.valueOf(false).toString();
            }
            response = Response.status(Response.Status.OK).entity(retVal).build();
        } catch (Exception e) {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return response;
    }

    @Path(PATH_ANNOTATE_QUANTITY_PDF)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public String processPDF(@FormDataParam("input") InputStream uploadedInputStream,
                             @FormDataParam("input") FormDataContentDisposition fileDetail) {
        MeasurementsResponse response = engine.processPdf(uploadedInputStream);
        return response.toJson();
    }

    @Path(PATH_QUANTITY_TEXT)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public String processText(@FormDataParam("text") String text) {

        MeasurementsResponse response = engine.processText(text);

        return response.toJson();
    }

    @Path(PATH_PARSE_MEASURE)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @POST
    public String parseMeasure_post(String json) {
        MeasurementsResponse response = engine.processJson(json);

        return response.toJson();
    }

    @Path(PATH_UNITS_TEXT)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public List<UnitBlock> processUnits(@FormDataParam("text") String text) {
        return engine.parseUnits(text);
    }
}
