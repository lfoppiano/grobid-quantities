package org.grobid.core.lexicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * The unit type and system vocabularies live in {@link UnitUtilities} while the units live in
 * the lexicon, so the two drift apart silently: an unknown type in the lexicon is only reported
 * as a warning at runtime, and the unit ends up without a type.
 * <p>
 * See https://github.com/lfoppiano/grobid-quantities/issues/92
 */
public class UnitsLexiconConsistencyTest {

    private static final String UNITS_LEXICON = "/lexicon/en/units.json";

    private static List<JsonNode> units;

    @BeforeClass
    public static void loadLexicon() throws Exception {
        units = new ArrayList<>();
        try (InputStream is = UnitsLexiconConsistencyTest.class.getResourceAsStream(UNITS_LEXICON)) {
            assertThat("cannot read " + UNITS_LEXICON, is != null, is(true));
            JsonNode root = new ObjectMapper().readTree(is);
            Iterator<JsonNode> iterator = root.get("units").elements();
            while (iterator.hasNext()) {
                units.add(iterator.next());
            }
        }
        assertThat(units, is(not(empty())));
    }

    @Test
    public void testEveryUnitType_shouldBeInTheControlledVocabulary() {
        List<String> unknown = new ArrayList<>();

        for (JsonNode unit : units) {
            String type = unit.path("type").asText(null);
            try {
                UnitUtilities.Unit_Type.valueOf(type);
            } catch (IllegalArgumentException | NullPointerException e) {
                unknown.add(type + " (" + notationOf(unit) + ")");
            }
        }

        assertThat("unit types missing from UnitUtilities.Unit_Type: " + unknown, unknown, is(empty()));
    }

    @Test
    public void testEverySystem_shouldBeInTheControlledVocabulary() {
        List<String> unknown = new ArrayList<>();

        for (JsonNode unit : units) {
            String system = unit.path("system").asText(null);
            try {
                UnitUtilities.System_Type.valueOf(system);
            } catch (IllegalArgumentException | NullPointerException e) {
                unknown.add(system + " (" + notationOf(unit) + ")");
            }
        }

        assertThat("systems missing from UnitUtilities.System_Type: " + unknown, unknown, is(empty()));
    }

    @Test
    public void testEveryUnit_shouldHaveAName() {
        List<String> nameless = new ArrayList<>();

        for (JsonNode unit : units) {
            if (!unit.has("names") || unit.get("names").isEmpty()) {
                nameless.add(notationOf(unit));
            }
        }

        assertThat("units without any name: " + nameless, nameless, is(empty()));
    }

    /**
     * A notation declared by two units is not resolvable: the lexicon indexes notations in a map,
     * so the last unit read wins and the other one becomes unreachable.
     * <p>
     * The ounce is a known offender - "oz" is claimed by the mass ounce and by both the US and
     * the imperial fluid ounce - and picking a winner changes the normalised values, so it is
     * listed here rather than silently fixed. Any new duplicate fails the test.
     */
    private static final Set<String> KNOWN_AMBIGUOUS_NOTATIONS =
        new HashSet<>(Arrays.asList("oz", "fl oz", "fl. oz.", "oz. fl."));

    @Test
    public void testNotations_shouldNotBeDeclaredTwice() {
        Set<String> seen = new HashSet<>();
        List<String> duplicated = new ArrayList<>();

        for (JsonNode unit : units) {
            for (JsonNode notation : unit.path("notations")) {
                String raw = notation.path("raw").asText(null);
                if ((raw != null) && !seen.add(raw) && !KNOWN_AMBIGUOUS_NOTATIONS.contains(raw)) {
                    duplicated.add(raw);
                }
            }
        }

        assertThat("notations declared by more than one unit: " + duplicated, duplicated, is(empty()));
    }

    private String notationOf(JsonNode unit) {
        JsonNode notations = unit.path("notations");
        if (notations.isEmpty()) {
            return unit.path("names").path(0).path("lemma").asText("?");
        }
        return notations.path(0).path("raw").asText("?");
    }
}
