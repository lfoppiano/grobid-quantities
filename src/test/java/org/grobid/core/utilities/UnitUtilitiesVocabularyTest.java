package org.grobid.core.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grobid.core.utilities.UnitUtilities.System_Type;
import org.grobid.core.utilities.UnitUtilities.Unit_Type;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The measurement type and system vocabularies are read from the lexicon rather than declared as
 * Java enumerations, so that a unit type is added in one place.
 * See https://github.com/lfoppiano/grobid-quantities/issues/92
 */
class UnitUtilitiesVocabularyTest {

    private static final String VOCABULARY = "/lexicon/en/unit-vocabulary.json";

    private List<String> idsIn(String field) throws Exception {
        List<String> ids = new ArrayList<>();
        try (InputStream is = UnitUtilities.class.getResourceAsStream(VOCABULARY)) {
            assertNotNull(is, VOCABULARY + " is missing");
            Iterator<JsonNode> terms = new ObjectMapper().readTree(is).get(field).elements();
            while (terms.hasNext()) {
                ids.add(terms.next().get("id").asText());
            }
        }
        return ids;
    }

    @Test
    void everyTypeOfTheFile_shouldBeResolvable() throws Exception {
        List<String> ids = idsIn("types");

        assertEquals(ids.size(), Unit_Type.values().size());
        for (String id : ids) {
            assertEquals(id, Unit_Type.valueOf(id).name());
        }
    }

    @Test
    void everySystemOfTheFile_shouldBeResolvable() throws Exception {
        List<String> ids = idsIn("systems");

        assertEquals(ids.size(), System_Type.values().size());
        for (String id : ids) {
            assertEquals(id, System_Type.valueOf(id).name());
        }
    }

    @Test
    void declarationOrder_shouldBePreserved() throws Exception {
        List<String> expected = idsIn("types");

        List<String> actual = new ArrayList<>();
        for (Unit_Type type : Unit_Type.values()) {
            actual.add(type.name());
        }

        assertEquals(expected, actual);
    }

    /**
     * {@code toString()} has to keep returning the identifier: the TEI serialisation writes it
     * into the {@code type} attribute of a measure, where the corpus uses the uppercase form.
     */
    @Test
    void toString_shouldReturnTheIdentifierAndGetNameTheLabel() {
        Unit_Type time = Unit_Type.valueOf("TIME");

        assertEquals("TIME", time.toString());
        assertEquals("TIME", time.name());
        assertEquals("time", time.getName());
    }

    /**
     * Terms are interned, so the identity comparisons the code does on systems still hold - see
     * {@code QuantityNormalizer}, which branches on {@code == System_Type.SI_BASE}.
     */
    @Test
    void terms_shouldBeInterned() {
        assertSame(Unit_Type.valueOf("LENGTH"), Unit_Type.valueOf("LENGTH"));
        assertSame(System_Type.SI_BASE, System_Type.valueOf("SI_BASE"));
        assertSame(System_Type.SI_DERIVED, System_Type.valueOf("SI_DERIVED"));
        assertSame(Unit_Type.UNKNOWN, Unit_Type.valueOf("UNKNOWN"));
    }

    @Test
    void equality_shouldBeByIdentifier() {
        assertEquals(Unit_Type.valueOf("MASS"), Unit_Type.valueOf("MASS"));
        assertEquals(Unit_Type.valueOf("MASS").hashCode(), Unit_Type.valueOf("MASS").hashCode());
        assertTrue(List.of(System_Type.SI_BASE, System_Type.SI_DERIVED)
            .contains(System_Type.valueOf("SI_DERIVED")));
    }

    /**
     * Mirrors {@code Enum.valueOf}: the two production call sites that resolve a type coming from
     * the corpus or from the lexicon rely on an unknown one throwing.
     */
    @Test
    void anUnknownTerm_shouldThrowLikeTheEnumerationDid() {
        IllegalArgumentException e =
            assertThrows(IllegalArgumentException.class, () -> Unit_Type.valueOf("NOT_A_TYPE"));

        assertTrue(e.getMessage().contains("NOT_A_TYPE"));
        assertThrows(IllegalArgumentException.class, () -> System_Type.valueOf("NOT_A_SYSTEM"));
        assertThrows(IllegalArgumentException.class, () -> Unit_Type.valueOf(null));
    }

    /**
     * The vocabulary carries the types the lexicon uses plus a tail of types kept for annotation;
     * the point of the file is that both can grow without touching Java.
     */
    @Test
    void theVocabulary_shouldCoverTheTypesUsedByTheLexicon() throws Exception {
        List<String> declared = idsIn("types");

        try (InputStream is = UnitUtilities.class.getResourceAsStream("/lexicon/en/units.json")) {
            Iterator<JsonNode> units = new ObjectMapper().readTree(is).get("units").elements();
            while (units.hasNext()) {
                String type = units.next().path("type").asText();
                assertTrue(declared.contains(type), type + " is used by units.json but not declared in " + VOCABULARY);
            }
        }
    }
}
