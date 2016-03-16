package org.grobid.core.utilities;

import org.apache.commons.collections4.Closure;
import org.apache.commons.io.FileUtils;
import org.grobid.core.data.RegexValueHolder;
import org.grobid.core.lexicon.QuantityLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by lfoppiano on 10/03/16.
 */
public class UnitTrainingDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTrainingDataGenerator.class);

    private List<UnitUtilities.Unit_Type> exclusions
            = Arrays.asList(new UnitUtilities.Unit_Type[]{
            UnitUtilities.Unit_Type.VO2_MAX,
            UnitUtilities.Unit_Type.TEMPERATURE,
            UnitUtilities.Unit_Type.DENSITY
    });

    private final String INFLECTION_FILE_NAME = "inflection.txt";
    private final String PREFIX_FILE_NAME = "prefix.txt";
    private final String UNITS_FILE_NAME = "units.txt";
    private String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";

    private String XML_UNITS_START = "<units>";
    private String XML_UNITS_END = "</units>";

    private String XML_UNIT_START = "<unit>";
    private String XML_UNIT_END = "</unit>";

    private String XML_POW_START = "<pow>";
    private String XML_POW_END = "</pow>";

    private String XML_BASE_START = "<base>";
    private String XML_BASE_END = "</base>";

    private String XML_PREFIX_START = "<prefix>";
    private String XML_PREFIX_END = "</prefix>";

    protected Pair<String, String> separateBaseAndPow(String value) {

        List<Character> powChars = Arrays.asList(new Character[]{
                new Character('^'),
                new Character('⁻'),
                new Character('¹'),
                new Character('²'),
                new Character('³'),
                new Character('-')
        });

        String base = "";
        String pow = "";

        for (int j = 0; j < value.length(); j++) {
            final char ch = value.charAt(j);

            if (Character.isDigit(ch) || powChars.contains(ch) || Character.getType(ch) == Character.OTHER_NUMBER) {
                pow += ch;
            } else {
                base += ch;
            }
        }

        return new Pair<>(base, pow);
    }

    public void appendUnit(PrintWriter write, String base1) {
        appendUnit(write, null, base1, null, null, null);
    }

    public void appendUnit(PrintWriter write, String prefix1, String base1, String operation,
                           String prefix2, String base2) {
        write.append(XML_UNIT_START);

        if (isNotEmpty(prefix1)) {
            write.append(XML_PREFIX_START).append(prefix1).append(XML_PREFIX_END);
        }

        Pair<String, String> pair = separateBaseAndPow(base1);

        write.append(XML_BASE_START).append(pair.getA()).append(XML_BASE_END);
        if (isNotEmpty(pair.getB())) {
            write.append(XML_POW_START).append(pair.getB()).append(XML_POW_END);
        }

        if (isNotEmpty(base2)) {
            if (isNotEmpty(operation)) {
                write.append(XML_POW_START).append(operation).append(XML_POW_END);
            }

            if (isNotEmpty(prefix2)) {
                write.append(XML_PREFIX_START).append(prefix2).append(XML_PREFIX_END);
            }

            pair = separateBaseAndPow(base2);

            write.append(XML_BASE_START).append(pair.getA()).append(XML_BASE_END);
            if (isNotEmpty(pair.getB())) {
                write.append(XML_POW_START).append(pair.getB()).append(XML_POW_END);
            }
        }
        write.append(XML_UNIT_END).append("\n");
    }


    public void generateData(String inputDirectoryPath, String outputFilePath) throws IOException {
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        String outputName = "generated.training." + now + ".tei.xml";

        File inputDirectory = new File(inputDirectoryPath);
        FileOutputStream outputFile = FileUtils.openOutputStream(new File(outputFilePath + File.separator + outputName));
        final PrintWriter write = new PrintWriter(outputFile);
        write.append(XML_PREFIX).append("\n");
        write.append(XML_UNITS_START).append("\n");

        if (inputDirectory != null && inputDirectory.isDirectory()) {

            final Map<String, List<String>> inflections = QuantityLexicon.loadInflections(new FileInputStream(inputDirectoryPath + File.separator + INFLECTION_FILE_NAME));
            final Map<String, String> prefixes = QuantityLexicon.loadPrefixes(new FileInputStream(inputDirectoryPath + File.separator + PREFIX_FILE_NAME));
            QuantityLexicon.loadUnits(new FileInputStream(inputDirectoryPath + File.separator + UNITS_FILE_NAME), new Closure<String>() {
                @Override
                public void execute(String inputLine) {
                    processLine(inputLine, write, prefixes, inflections);
                }
            });
        }

        write.append(XML_UNITS_END).append("\n");

        write.flush();
        write.close();
        outputFile.close();
    }

    protected void processLine(String inputLine, PrintWriter write, Map<String, String> prefixesList, Map<String, List<String>> inflectionsList) {
        String[] pieces = inputLine.split("\t", -2);
        UnitUtilities.System_Type system = null;
        UnitUtilities.Unit_Type unitType = null;
        try {
            system = UnitUtilities.System_Type.valueOf(pieces[2]);
        } catch (Exception e) {
            LOGGER.warn("Invalid unit type name: " + pieces[2]);

        }

        try {
            unitType = UnitUtilities.Unit_Type.valueOf(pieces[1]);
        } catch (Exception e) {
            LOGGER.warn("Invalid unit type name: " + pieces[2]);
        }

        if (exclusions.contains(unitType)) {
            return;
        }


        String units = pieces[0].trim();
        String names = pieces[3];

        if (units.length() > 0) {
            //Processing units.
            String[] subPieces = units.split(",");
            for (String subPiece : subPieces) {
                if (system == UnitUtilities.System_Type.SI_BASE || system == UnitUtilities.System_Type.SI_DERIVED) {
                    if (!QuantityLexicon.isComposedUnit(subPiece)) {
                        appendUnit(write, subPiece);

                        for (Map.Entry<String, String> prefix : prefixesList.entrySet()) {

                            String prefixString = prefix.getKey();

                            appendUnit(write, prefixString, subPiece, null, null, null);
                        }
                    } else {
                        List<RegexValueHolder> decomposition = QuantityLexicon.decomposeComplexUnitWithDelimiter(subPiece);

                        //Assuming there are only two elements
                        RegexValueHolder firstElement = decomposition.get(0);
                        RegexValueHolder operation = decomposition.get(1);
                        RegexValueHolder secondElement = decomposition.get(2);

                        appendUnit(write, null, firstElement.getValue(), operation.getValue(),
                                null, secondElement.getValue());

                        for (Map.Entry<String, String> prefix : prefixesList.entrySet()) {
                            String prefixString = prefix.getKey();

                            appendUnit(write, null, firstElement.getValue(),
                                    operation.getValue(), prefixString,
                                    secondElement.getValue());

                            appendUnit(write, prefixString, firstElement.getValue(),
                                    operation.getValue(), null,
                                    secondElement.getValue());

                            for (Map.Entry<String, String> prefix2 : prefixesList.entrySet()) {
                                String prefixString2 = prefix2.getKey();

                                appendUnit(write, prefixString, firstElement.getValue(),
                                        operation.getValue(), prefixString2,
                                        secondElement.getValue());

                            }
                        }
                    }
                } else {
                    if (!QuantityLexicon.isComposedUnit(subPiece)) {
                        appendUnit(write, subPiece);
                    } else {
                        List<RegexValueHolder> decomposition = QuantityLexicon.decomposeComplexUnitWithDelimiter(subPiece);

                        //Assuming there are only two elements
                        RegexValueHolder firstElement = decomposition.get(0);
                        RegexValueHolder operation = decomposition.get(1);
                        RegexValueHolder secondElement = decomposition.get(2);

                        appendUnit(write, null, firstElement.getValue(),
                                operation.getValue(), null,
                                secondElement.getValue());


                    }
                }
            }
        }

        //Processing unit names
        /*String[] subPieces = names.split(",");
        for (String subPiece : subPieces) {

            List<String> inflections = quantityLexicon.getInflections(subPiece);

            for (String inflection : inflections) {
                if (system == UnitUtilities.System_Type.SI_BASE || system == UnitUtilities.System_Type.SI_DERIVED) {
                    if (subPiece.split(" ").length == 1) {

                        for (Map.Entry<String, String> prefix : prefixesList.entrySet()) {
                            String prefixString = prefix.getValue();

                            appendUnit(write, prefixString, inflection, null, null, null);
                        }
                    } else {
                        appendUnit(write, null, inflection, null, null, null);
                    }
                } else {
                    appendUnit(write, inflection);
                }
            }

        }*/
    }
}
