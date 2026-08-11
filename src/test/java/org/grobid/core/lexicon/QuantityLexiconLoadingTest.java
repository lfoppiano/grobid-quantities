package org.grobid.core.lexicon;

import org.grobid.core.data.UnitDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UnitsLexiconConsistencyTest} checks the lexicon as a JSON document; this one checks that
 * it actually loads, which is not the same thing. A notation whose only separator is the SI
 * multiplication sign (<code>W·h</code>) used to make the load throw, and no test noticed because
 * the lexicon happened not to contain one.
 * <p>
 * See https://github.com/lfoppiano/grobid-quantities/issues/92
 */
class QuantityLexiconLoadingTest {

    private static QuantityLexicon lexicon;

    @BeforeAll
    static void loadTheLexicon() {
        // the whole point: this must not throw
        lexicon = QuantityLexicon.getInstance();
    }

    private UnitDefinition resolve(String unit) {
        UnitDefinition definition = lexicon.getUnitByNotation(unit);
        return (definition != null) ? definition : lexicon.getUnitbyName(unit);
    }

    @ParameterizedTest
    @CsvSource({
        // the types that had no unit at all before
        "m2,             AREA,                  SI_DERIVED",
        "acre,           AREA,                  IMPERIAL",
        "sq ft,          AREA,                  IMPERIAL",
        "m/s2,           ACCELERATION,          SI_DERIVED",
        "lm,             LUMINOUS_FLUX,         SI_DERIVED",
        "lx,             ILLUMINANCE,           SI_DERIVED",
        "nit,            LUMINANCE,             SI_DERIVED",
        "W/m2,           IRRADIANCE,            SI_DERIVED",
        "m2/s,           KINEMATIC_VISCOSITY,   SI_DERIVED",
        "W/mK,           THERMAL_CONDUCTIVITY,  SI_DERIVED",
        // and a sample of the rest
        "cm3,            VOLUME,                SI_DERIVED",
        "bbl,            VOLUME,                US",
        "cal,            ENERGY,                NON_SI",
        "BTU,            ENERGY,                IMPERIAL",
        "hp,             POWER,                 IMPERIAL",
        "lbf,            FORCE,                 IMPERIAL",
        "inHg,           PRESSURE,              NON_SI",
        "km/h,           VELOCITY,              NON_SI",
        "ppt,            CONCENTRATION,         NON_SI",
        "gon,            ANGLE,                 NON_SI",
        "Mx,             MAGNETIC_FLUX,         CGS",
        "nmi,            LENGTH,                NON_SI",
        "ozt,            MASS,                  NON_SI",
    })
    void aNewNotation_shouldResolveToItsTypeAndSystem(String notation, String type, String system) {
        UnitDefinition definition = resolve(notation);

        assertNotNull(definition, notation + " is not in the lexicon");
        assertEquals(type, definition.getType().name());
        assertEquals(system, definition.getSystem().name());
    }

    /**
     * Units whose symbol would have shadowed an existing one - "ha" over hectoampere, "ct" over
     * centitesla - are in the lexicon under their spelled-out name only.
     */
    @ParameterizedTest
    @CsvSource({
        "hectare,      AREA",
        "parsec,       LENGTH",
        "carat,        MASS",
        "katal,        CATALYTIC_ACTIVITY",
        "stokes,       KINEMATIC_VISCOSITY",
        "foot-candle,  ILLUMINANCE",
    })
    void aNameOnlyUnit_shouldStillResolve(String name, String type) {
        UnitDefinition definition = lexicon.getUnitbyName(name);

        assertNotNull(definition, name + " is not in the lexicon");
        assertEquals(type, definition.getType().name());
    }

    /**
     * The units declaring {@code supportsPrefixes} are reachable in their prefixed forms too.
     */
    @ParameterizedTest
    @CsvSource({"kcal, ENERGY", "kWh, ENERGY", "MWh, ENERGY", "mb, AREA"})
    void aPrefixedForm_shouldResolveToTheSameUnit(String notation, String type) {
        UnitDefinition definition = resolve(notation);

        assertNotNull(definition, notation + " is not in the lexicon");
        assertEquals(type, definition.getType().name());
    }

    /**
     * "W·h" separates with the SI multiplication sign and nothing else, which used to throw an
     * IndexOutOfBoundsException while expanding the prefixes.
     */
    @Test
    void aNotationSeparatedOnlyByTheMultiplicationSign_shouldExpand() {
        assertTrue(QuantityLexicon.isComposedUnit("W·h"));

        assertNotNull(resolve("W·h"));
        assertNotNull(resolve("kWh"));

        assertTrue(lexicon.derivationalMorphologyExpansion("W·h", true).contains("kW·h"));
    }
}
