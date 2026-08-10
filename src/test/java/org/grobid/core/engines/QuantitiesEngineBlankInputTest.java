package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.utilities.GrobidConfig;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.MeasurementOperations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantitiesEngineBlankInputTest {

    private static QuantitiesEngine target;

    @BeforeAll
    static void beforeAll() {
        GrobidConfig.ModelParameters modelParameters = new GrobidConfig.ModelParameters();
        modelParameters.name = "bao";
        GrobidProperties.addModel(modelParameters);

        QuantityParser quantityParser = new QuantityParser(
            GrobidModels.DUMMY, null, new MeasurementOperations(null), new ValueParser(GrobidModels.DUMMY));
        UnitParser unitParser = new UnitParser(GrobidModels.DUMMY, null);
        target = new QuantitiesEngine(quantityParser, unitParser, new EngineParsers());
    }

    @Test
    void blankInputNullReturnsJsonAndTei() {
        assertBlankInputHandled(null);
    }

    @Test
    void blankInputEmptyStringReturnsJsonAndTei() {
        assertBlankInputHandled("");
    }

    @Test
    void blankInputWhitespaceReturnsJsonAndTei() {
        assertBlankInputHandled("   \r\n\t");
    }

    private static void assertBlankInputHandled(String text) {
        // JSON endpoint parity: no exception (HTTP 200), empty measurement list with runtime
        String json = assertDoesNotThrow(() -> target.processText(text).toJson());
        assertNotNull(json);
        assertTrue(json.contains("\"runtime\""));

        // TEI endpoint parity: no exception (HTTP 200), well-formed empty TEI document
        String tei = assertDoesNotThrow(() -> target.processTextTei(text));
        assertNotNull(tei);
        assertTrue(tei.contains("<teiHeader>"));
        assertTrue(tei.contains("<text xml:lang=\"en\"><p"));
    }
}
