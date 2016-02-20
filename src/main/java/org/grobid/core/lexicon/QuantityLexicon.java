package org.grobid.core.lexicon;

import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.UnitUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Class for managing the measurement lexical resources
 *
 * @author Patrice Lopez
 */
public class QuantityLexicon {
    private static final Logger logger = LoggerFactory.getLogger(QuantityLexicon.class);

    private static volatile QuantityLexicon instance;
    private final String INFLECTION_PATH = "en/inflection.txt";
    private final String PREFIX_PATH = "en/prefix.txt";
    private final String UNITS_PATH = "en/units.txt";
    private final String COMPOSED_UNIT_REGEX = "[^/*]+";

    Pattern composedUnitPattern = Pattern.compile(COMPOSED_UNIT_REGEX);

    // lexical information - for feature generations
    private FastMatcher unitPattern = null;

    private Set<String> unitTokens = null;
    private Map<String, String> prefixes = null; // map prefix symbol to prefix string
    private Map<String, List<String>> inflection = null; // map a unit string to its morphological inflections

    // full unit information accessible from the unit names
    // this mapping depends on the language
    private Map<String, UnitDefinition> name2unit = null;

    // full unit information accessible from the unit notation
    // this mapping depends on the language
    private Map<String, UnitDefinition> notation2unit = null;

    // mapping between measurement types and the SI units for this type, the type here is represented with
    // the name() value of the enum
    private Map<String, UnitDefinition> type2SIUnit = null;

    private QuantityLexicon() {
        init();
    }

    public static QuantityLexicon getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new QuantityLexicon();
    }

    private void init() {
        initPrefix();
        initInflection();

        File file = null;
        InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;

        try {
            unitTokens = new HashSet<String>();
            ist = this.getClass().getClassLoader().getResourceAsStream(UNITS_PATH);

            unitPattern = new FastMatcher();
            //ist = new FileInputStream(file);
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);

            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;
                String[] pieces = l.split("\t");
                UnitDefinition unitDefinition = new UnitDefinition();
                UnitUtilities.Unit_Type type = null;
                UnitUtilities.System_Type system = null;
                for (int i = 0; i < pieces.length; i++) {
                    String piece = pieces[i].trim();
                    if (piece.length() == 0)
                        continue;

                    if (i == 0) {
                        String[] subpieces = piece.split(",");
                        for (int j = 0; j < subpieces.length; j++) {
                            String subpiece = subpieces[j].trim();

                            //expansion
                            List<String> derivations = derivationalMorphologyExpansion(subpiece, true);
                            for (String derivation : derivations) {
                                try {
                                    unitPattern.loadTerm(derivation);
                                } catch (Exception e) {
                                    logger.error("invalid unit term: " + derivation);
                                }
                                unitDefinition.addNotation(derivation);

                                List<String> subsubpieces = QuantityAnalyzer.tokenize(derivation);
                                for (String word : subsubpieces) {
                                    addToUnitTokens(word);
                                }
                            }
                        }
                    } else if (i == 1) {
                        try {
                            type = UnitUtilities.Unit_Type.valueOf(piece);
                        } catch (Exception e) {
                            logger.error("invalid unit type name: " + piece);
                        }
                        unitDefinition.setType(type);
                    } else if (i == 2) {
                        try {
                            system = UnitUtilities.System_Type.valueOf(piece);
                        } catch (Exception e) {
                            logger.error("invalid unit system name: " + piece);
                        }
                        unitDefinition.setSystem(system);
                    } else if (i == 3) {
                        String[] subpieces = piece.split(",");
                        for (int j = 0; j < subpieces.length; j++) {
                            String subpiece = subpieces[j].trim();

                            // expansion with inflections
                            List<String> inflections = inflectionalMorphologyExpansion(subpiece);

                            for (String inflectedForm : inflections) {
                                if ((system == UnitUtilities.System_Type.SI_BASE) || (system == UnitUtilities.System_Type.SI_DERIVED)) {
                                    // expansion with derivational morphology, but only for SI units!
                                    List<String> derivations = derivationalMorphologyExpansion(inflectedForm, false);
                                    for (String derivation : derivations) {
                                        unitDefinition.addName(derivation);
                                        try {
                                            unitPattern.loadTerm(derivation);
                                        } catch (Exception e) {
                                            logger.error("invalid unit term: " + derivation);
                                        }
                                        List<String> subSubpieces = QuantityAnalyzer.tokenize(derivation);
                                        for (String word : subSubpieces) {
                                            addToUnitTokens(word);
                                        }
                                    }
                                } else {
                                    unitDefinition.addName(inflectedForm);
                                    try {
                                        unitPattern.loadTerm(inflectedForm);
                                    } catch (Exception e) {
                                        logger.error("invalid unit term: " + inflectedForm);
                                    }
                                    List<String> subsubpieces = QuantityAnalyzer.tokenize(inflectedForm);
                                    for (String word : subsubpieces) {
                                        addToUnitTokens(word);
                                    }
                                }
                            }
                        }
                    }
                }

                // add unit names in the first map
                List<String> names = unitDefinition.getNames();
                if ((names != null) && (names.size() > 0)) {
                    for (int j = 0; j < names.size(); j++) {
                        if (name2unit == null)
                            name2unit = new HashMap<>();
                        name2unit.put(names.get(j).trim().toLowerCase(), unitDefinition);
                    }
                }

                // add unit notation map
                List<String> notations = unitDefinition.getNotations();
                if ((notations != null) && (notations.size() > 0)) {
                    for (int j = 0; j < notations.size(); j++) {
                        if (notation2unit == null)
                            notation2unit = new HashMap<>();
                        notation2unit.put(notations.get(j).trim(), unitDefinition);
                    }
                } else
                    notation2unit.put("no_notation", unitDefinition);

                // add unit in the second map
                system = unitDefinition.getSystem();
                if ((system == UnitUtilities.System_Type.SI_BASE) || (system == UnitUtilities.System_Type.SI_DERIVED)) {
                    if (type2SIUnit == null) {
                        type2SIUnit = new HashMap<>();
                    }
                    if ( (type == null) || (type.getName() == null) )
                        logger.error("unitDefinition has no type: " + unitDefinition.toString());

                    if (type2SIUnit.get(type.getName()) == null)
                        type2SIUnit.put(type.getName(), unitDefinition);
                }
//System.out.print(notations); System.out.println(" -> " + type);
//System.out.print(names); System.out.println(" -> " + type);
            }
//System.out.println(unitTokens.toString());
        } catch (PatternSyntaxException e) {
            throw new
                    GrobidResourceException("Error when compiling lexicon matcher for unit vocabulary.", e);
        } catch (FileNotFoundException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    private void addToUnitTokens(String word) {
        word = word.trim().toLowerCase();
        if ((word.length() > 0) && !unitTokens.contains(word)) {
            // we don't add pure digit sub-token and token delimiters
            if ((TextUtilities.countDigit(word) != word.length()) && (QuantityAnalyzer.DELIMITERS.indexOf(word) == -1))
                unitTokens.add(word);
        }
    }

    private void initPrefix() {
        File file = null;
        InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            unitTokens = new HashSet<String>();
            ist = this.getClass().getClassLoader().getResourceAsStream(PREFIX_PATH);

            unitPattern = new FastMatcher();
            //ist = new FileInputStream(file);
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);

            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;
                String pieces[] = l.split("\t");
                if (pieces.length != 3)
                    continue;
                String symbol = pieces[1].trim();
                String name = pieces[2].trim();
                if (prefixes == null)
                    prefixes = new HashMap<String, String>();

                prefixes.put(symbol, name);
            }

//System.out.println(prefixes.toString());
        } catch (PatternSyntaxException e) {
            throw new
                    GrobidResourceException("Error when compiling prefix map for unit vocabulary.", e);
        } catch (FileNotFoundException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    private void initInflection() {
        File file = null;
        InputStream ist = null;
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            unitTokens = new HashSet<String>();
            ist = this.getClass().getClassLoader().getResourceAsStream(INFLECTION_PATH);

            unitPattern = new FastMatcher();
            //ist = new FileInputStream(file);
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);

            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;
                String pieces[] = l.split("\t");
                if (pieces.length != 2)
                    continue;
                String name = pieces[0].trim();
                String inflections = pieces[1].trim();
                List<String> inflectionList = new ArrayList<String>();
                String[] subinflections = inflections.split(",");
                for (int i = 0; i < subinflections.length; i++) {
                    inflectionList.add(subinflections[i].trim());
                }
                if (inflection == null)
                    inflection = new HashMap<String, List<String>>();
                if (inflectionList.size() > 0)
                    inflection.put(name, inflectionList);
            }
//System.out.println(inflection.toString());
        } catch (PatternSyntaxException e) {
            throw new
                    GrobidResourceException("Error when compiling inflection unit vocabulary.", e);
        } catch (FileNotFoundException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        } finally {
            try {
                if (ist != null)
                    ist.close();
                if (isr != null)
                    isr.close();
                if (dis != null)
                    dis.close();
            } catch (Exception e) {
                throw new GrobidResourceException("Cannot close all streams.", e);
            }
        }
    }

    /**
     * Expansion of a non-notation unit name into its inflected forms. Note that the
     * input unit name is included in the returned list of forms.
     */
    private List<String> inflectionalMorphologyExpansion(String unitTerm) {
        List<String> results = new ArrayList<>();
        results.add(unitTerm);

        List<String> inflections = inflection.get(unitTerm);
        if (isNotEmpty(inflections)) {
            for (String inflect : inflections)
                results.add(inflect);
        }

        return results;
    }

    /**
     * Expansion of a notation and non-notation unit name into derivations
     * based on standard unit prefix.
     * Note that the input unit name is included in the returned list of forms.
     * To be called after the inflectional expansion.
     */
    protected List<String> derivationalMorphologyExpansion(String unitTerm, boolean isNotation) {
        List<String> results = new ArrayList<>();
        results.add(unitTerm);

        if (!isComposed(unitTerm)) {
            // we expand based on the prefix list
            for (Map.Entry<String, String> prefix : prefixes.entrySet()) {
                String prefixString = selectPrefix(isNotation, prefix);
                results.add(prefixString + unitTerm);
            }
        } else {
            //A String.split() could have done the same job, since it was not sure which requirements there were
            //we have a more sophisticated - though useless way, that might be used if the expansion became more
            // complex.

            List<ValueHolder> decomposition = new ArrayList<>();
            Matcher m = composedUnitPattern.matcher(unitTerm);

            while (m.find()) {
                decomposition.add(new ValueHolder(m.group(), m.start(), m.end()));
            }

            ValueHolder firstElement = decomposition.get(0);
            ValueHolder secondElement = decomposition.get(1);

            for (Map.Entry<String, String> prefix : prefixes.entrySet()) {
                String prefixString = selectPrefix(isNotation, prefix);
                String firstElementExpanded = prefixString + firstElement.getValue();
                results.add(unitTerm.replace(secondElement.getValue(), prefixString + secondElement.getValue()));

                String outputExpanded = unitTerm.replace(firstElement.getValue(), firstElementExpanded);
                results.add(outputExpanded);

                for (Map.Entry<String, String> prefix2 : prefixes.entrySet()) {
                    String prefixString2 = selectPrefix(isNotation, prefix2);

                    results.add(outputExpanded.replace(secondElement.getValue(), prefixString2 + secondElement.getValue()));
                }
            }
        }

        return results;
    }

    private String selectPrefix(boolean isNotation, Map.Entry<String, String> prefix) {
        String prefixString;
        if (isNotation) {
            // if we have a notation, we use notation prefix (e.g. g -> kg)
            prefixString = prefix.getKey();
        } else {
            // otherwise we have a full form and we use the derivational prefix (e.g. gram -> kilogram)
            prefixString = prefix.getValue();
        }
        return prefixString;
    }

    private boolean isComposed(String unitTerm) {
        return unitTerm.contains("/") || unitTerm.contains("*");
        //return composedUnitPattern.matcher(unitTerm).find();
    }

    /**
     * Soft look-up in unit dictionary
     */
    public List<OffsetPosition> inUnitNames(String s) {
        if (unitPattern == null) {
            init();
        }
        List<OffsetPosition> results = unitPattern.matcher(s);
        return results;
    }

    public List<OffsetPosition> inUnitNames(List<String> s) {
        if (unitPattern == null) {
            init();
        }
        List<OffsetPosition> results = unitPattern.matcher(s);
        return results;
    }

    public List<OffsetPosition> inUnitNamesPairs(List<Pair<String, String>> s) {
        if (unitPattern == null) {
            init();
        }
        List<OffsetPosition> results = unitPattern.matcherPairs(s);
        return results;
    }

    public boolean inUnitDictionary(String s) {
        return unitTokens.contains(s.toLowerCase());
    }

    /**
     * Return a unit object based on an unit name.
     */
    public UnitDefinition getUnitbyName(String name) {
        if (name == null)
            return null;
        return (UnitDefinition) name2unit.get(name.toLowerCase());
    }

    /**
     * Return a unit object based on an unit notation.
     */
    public UnitDefinition getUnitbyNotation(String notation) {
        if (notation == null)
            return null;
        return (UnitDefinition) notation2unit.get(notation);
    }

    /**
     * Return the SI unit object from a measure type name
     */
    public UnitDefinition getSIUnitByType(String type) {
        if (type == null)
            return null;
        return (UnitDefinition) type2SIUnit.get(type);
    }

    class ValueHolder {
        private String value;
        private int start;
        private int end;

        public ValueHolder(String group, int start, int end) {
            this.value = group;
            this.start = start;
            this.end = end;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }

}