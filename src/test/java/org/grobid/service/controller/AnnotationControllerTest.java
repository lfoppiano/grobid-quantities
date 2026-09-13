package org.grobid.service.controller;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.grobid.core.data.Measurement;
import org.grobid.core.data.MeasurementsResponse;
import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.QuantitiesEngine;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnitUtilities;
import org.grobid.service.configuration.GrobidQuantitiesConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AnnotationControllerTest {

    private QuantitiesEngine engine;
    private GrobidQuantitiesConfiguration configuration;
    private AnnotationController controller;

    @BeforeEach
    void setUp() {
        engine = Mockito.mock(QuantitiesEngine.class);
        configuration = Mockito.mock(GrobidQuantitiesConfiguration.class);
        controller = new AnnotationController(configuration, engine);
    }

    private MeasurementsResponse sampleResponse() {
        Unit unit = new Unit("kg", 16, 18);
        Quantity quantity = new Quantity("5", unit, 14, 15);
        Measurement measurement = new Measurement(UnitUtilities.Measurement_Type.VALUE);
        measurement.setAtomicQuantity(quantity);
        measurement.setRawOffsets(new OffsetPosition(14, 18));
        measurement.setRawString("5 kg");

        MeasurementsResponse response = new MeasurementsResponse(Collections.singletonList(measurement));
        response.setRuntime(42);
        return response;
    }

    @Test
    void processText_defaultFormat_shouldReturnJson() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.", "json");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("\"runtime\" : 42"));
        assertTrue(entity.contains("\"measurements\""));
    }

    @Test
    void processText_teiFormat_shouldReturnTeiXml() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.", "tei");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getMediaType().toString().contains("application/xml"));
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("<tei xmlns=\"http://www.tei-c.org/ns/1.0\">"));
        assertTrue(entity.contains("ident=\"grobid-quantities\""));
        assertTrue(entity.contains("<measure xml:id=\"m0\" type=\"value\" corresp=\"#ann-m0\">"));
        assertTrue(entity.contains("<num xml:id=\"m0-num\">5</num>"));
        assertTrue(entity.contains("<unit xml:id=\"m0-unit\">kg</unit>"));
        assertTrue(entity.contains("<standOff>"));
        assertTrue(entity.contains("<fs type=\"measurement\">"));
    }

    @Test
    void processText_xmlFormatAlias_shouldReturnTeiXml() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.", "xml");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getMediaType().toString().contains("application/xml"));
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("<tei xmlns=\"http://www.tei-c.org/ns/1.0\">"));
    }

    @Test
    void processText_invalidFormat_shouldReturnBadRequest() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.", "unsupported_format");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("Invalid format"));
    }

    @Test
    void processPDF_defaultFormat_shouldReturnJson() {
        when(engine.processPdf(any(InputStream.class))).thenReturn(sampleResponse());

        InputStream is = new ByteArrayInputStream(new byte[0]);
        Response response = controller.processPDF(is, null, "json");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("\"runtime\" : 42"));
    }

    @Test
    void processPDF_teiFormat_shouldReturnTeiXml() {
        when(engine.processPdf(any(InputStream.class))).thenReturn(sampleResponse());

        InputStream is = new ByteArrayInputStream(new byte[0]);
        Response response = controller.processPDF(is, null, "tei");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getMediaType().toString().contains("application/xml"));
        String entity = (String) response.getEntity();
        assertTrue(entity.contains("<tei xmlns=\"http://www.tei-c.org/ns/1.0\">"));
        assertTrue(entity.contains("<standOff>"));
        assertTrue(entity.contains("<fs type=\"measurement\">"));
    }

    @Test
    void processText_queryParamTakesPrecedenceOverFormParam() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.", "tei", "json");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getMediaType().toString().contains("application/xml"));
    }

    @Test
    void processText_noFormatProvided_shouldDefaultToJson() {
        when(engine.processText(anyString())).thenReturn(sampleResponse());

        Response response = controller.processText("The weight is 5 kg.");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
    }
}
