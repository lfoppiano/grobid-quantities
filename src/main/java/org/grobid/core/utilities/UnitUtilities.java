package org.grobid.core.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utilities for managing SI and non-SI units.
 * <p>
 * The measurement type and measurement system vocabularies used to be Java enumerations declared
 * here, which meant a unit could not be added to the lexicon with a new type without also editing
 * this file - and an unknown type was only reported as a runtime warning, the unit ending up
 * untyped. Both vocabularies are now read from {@code /lexicon/en/unit-vocabulary.json}, so the
 * lexicon and its vocabularies are maintained in one place.
 * <p>
 * See https://github.com/lfoppiano/grobid-quantities/issues/92 and doc/lexicon.md.
 *
 * @author Patrice Lopez
 */
public class UnitUtilities {

    private static final String VOCABULARY_PATH = "/lexicon/en/unit-vocabulary.json";

    /**
     * A term of a controlled vocabulary read from the vocabulary file. Terms are interned, so
     * the identity comparisons that were valid on the enumerations these replaced still hold.
     */
    public abstract static class VocabularyTerm {

        private final String id;
        private final String name;

        VocabularyTerm(String id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         * The identifier, e.g. {@code MAGNETIC_FLUX_DENSITY}. This is what the lexicon and the
         * annotated corpus use, and what {@link #toString()} returns - the enumerations these
         * classes replaced behaved the same way.
         */
        public String name() {
            return id;
        }

        /**
         * The human-readable label, e.g. {@code magnetic flux density}. This is what the JSON
         * output of the service carries.
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if ((other == null) || (getClass() != other.getClass())) {
                return false;
            }
            return id.equals(((VocabularyTerm) other).id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    /**
     * The terms of one vocabulary, in the order they are declared in the file.
     */
    static class Vocabulary<T extends VocabularyTerm> {

        private final String field;
        private final Map<String, T> byId;

        private Vocabulary(String field, Map<String, T> byId) {
            this.field = field;
            this.byId = byId;
        }

        static <T extends VocabularyTerm> Vocabulary<T> load(String field, BiFunction<String, String, T> factory) {
            Map<String, T> byId = new LinkedHashMap<>();

            try (InputStream is = UnitUtilities.class.getResourceAsStream(VOCABULARY_PATH)) {
                if (is == null) {
                    throw new IllegalStateException("Cannot find the unit vocabulary at " + VOCABULARY_PATH);
                }
                JsonNode terms = new ObjectMapper().readTree(is).get(field);
                if ((terms == null) || !terms.isArray()) {
                    throw new IllegalStateException("The unit vocabulary at " + VOCABULARY_PATH
                        + " has no '" + field + "' array");
                }
                Iterator<JsonNode> iterator = terms.elements();
                while (iterator.hasNext()) {
                    JsonNode term = iterator.next();
                    String id = term.path("id").asText(null);
                    if (id == null) {
                        throw new IllegalStateException("A '" + field + "' term of " + VOCABULARY_PATH
                            + " has no 'id'");
                    }
                    if (byId.containsKey(id)) {
                        throw new IllegalStateException("The '" + field + "' term " + id
                            + " is declared twice in " + VOCABULARY_PATH);
                    }
                    byId.put(id, factory.apply(id, term.path("name").asText(id)));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read the unit vocabulary at " + VOCABULARY_PATH, e);
            }

            return new Vocabulary<>(field, byId);
        }

        /**
         * Mirrors {@code Enum.valueOf}, exception included, so the call sites that catch an
         * unknown term keep working.
         */
        T valueOf(String id) {
            T term = byId.get(id);
            if (term == null) {
                throw new IllegalArgumentException("No " + field + " term named " + id
                    + ". Declare it in " + VOCABULARY_PATH + " if it is a new one.");
            }
            return term;
        }

        List<T> values() {
            return Collections.unmodifiableList(new java.util.ArrayList<>(byId.values()));
        }
    }

    // measurement systems
    public static final class System_Type extends VocabularyTerm {

        private static final Vocabulary<System_Type> VOCABULARY =
            Vocabulary.load("systems", System_Type::new);

        /**
         * The two systems the normalisation branches on: an SI unit is resolved against a
         * different set of unit formats than the rest, see
         * {@code QuantityNormalizer#getUnitFormats}. Declared here, rather than looked up at
         * each call site, because they carry behaviour rather than being simple labels - and
         * because it makes their removal from the vocabulary file fail the build instead of
         * silently changing how units are normalised.
         */
        public static final System_Type SI_BASE = valueOf("SI_BASE");
        public static final System_Type SI_DERIVED = valueOf("SI_DERIVED");

        private System_Type(String id, String name) {
            super(id, name);
        }

        public static System_Type valueOf(String id) {
            return VOCABULARY.valueOf(id);
        }

        public static List<System_Type> values() {
            return VOCABULARY.values();
        }
    }

    // measurement types
    public static final class Unit_Type extends VocabularyTerm {

        private static final Vocabulary<Unit_Type> VOCABULARY =
            Vocabulary.load("types", Unit_Type::new);

        /**
         * The fallback for a measurement whose type could not be established. Every other type
         * is a plain label and is reached through {@link #valueOf(String)}.
         */
        public static final Unit_Type UNKNOWN = valueOf("UNKNOWN");

        private Unit_Type(String id, String name) {
            super(id, name);
        }

        public static Unit_Type valueOf(String id) {
            return VOCABULARY.valueOf(id);
        }

        public static List<Unit_Type> values() {
            return VOCABULARY.values();
        }
    }

    // measurement type (atomic value, interval of conjuctive/disjunctive list of values/intervals)
    public enum Measurement_Type {
        VALUE("value"),
        INTERVAL_MIN_MAX("interval min max"),
        INTERVAL_BASE_RANGE("interval base range"),
        CONJUNCTION("listc"),
        DISJUNCTION("listd");

        private String name;

        private Measurement_Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static volatile UnitUtilities instance;

    public static UnitUtilities getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
            if (instance == null)
                getNewInstance();
            // }
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new UnitUtilities();
    }

    private UnitUtilities() {
    }


}
