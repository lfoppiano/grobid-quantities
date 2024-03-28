package org.grobid.core.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.N;
import org.grobid.core.data.normalization.NormalizationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert a number expressed with alphabetical characters into a normalized numerical value.
 *
 * @author Patrice Lopez
 */
public class WordsToNumber {

    private static final Logger logger = LoggerFactory.getLogger(WordsToNumber.class);

    private static volatile WordsToNumber instance;

    private final String VALUES_PATH = "lexicon/en/values.json";

    private final Pattern NUMERIC_PATTERN = Pattern.compile("\\b(?:\\d+(?:[.,]\\d+)*|\\d+[.,]?\\d*\\b)\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern OUT_OF_PATTERN_NUMBERS = Pattern.compile("([0-9.,]+)( out)? of (the )?([0-9.,]+)", Pattern.CASE_INSENSITIVE);
    private final Pattern OUT_OF_PATTERN_ALPHABETIC = Pattern.compile("([A-Za-z ]+) out of ([a-z]+ )?([A-Za-z]+)", Pattern.CASE_INSENSITIVE);

    private static List<String> bases = null;
    private static List<String> tens = null;
    private static List<String> scales = null;
    private static String decimalMark = null;

    private Set<String> numberTokens = null;
    private Map<String, Double> specials = new HashMap<>();
    private Map<String, Double> fractions = new HashMap<>();

    // the lexicon
    private Map<String, ScaleIncrementPair> numWord = null;

    public static WordsToNumber getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new WordsToNumber();
    }

    private WordsToNumber() {
        // init the lexicon with the numerical operations
        numWord = new HashMap<>();
        numWord.put("and", new ScaleIncrementPair(1, 0));

        init();
    }

    private void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(this.getClass().getClassLoader().getResourceAsStream(VALUES_PATH));

            bases = new ArrayList<>();
            tens = new ArrayList<>();
            scales = new ArrayList<>();
            numberTokens = new HashSet<>();
            List<String> decimalMarks = new ArrayList<>();

            JsonNode basesNode = rootNode.findPath("bases");
            if ((basesNode != null) && (!basesNode.isMissingNode())) {
                Iterator<JsonNode> iter = basesNode.elements();
                while (iter.hasNext()) {
                    String text = ((JsonNode) iter.next()).textValue();
                    bases.add(text);
                    numberTokens.add(text);
                }
            }
            for (int i = 0; i < bases.size(); i++) {
                numWord.put(bases.get(i), new ScaleIncrementPair(1, i));
            }

            JsonNode tensNode = rootNode.findPath("tens");
            if ((tensNode != null) && (!tensNode.isMissingNode())) {
                Iterator<JsonNode> iter = tensNode.elements();
                while (iter.hasNext()) {
                    String text = ((JsonNode) iter.next()).textValue();
                    tens.add(text);
                    numberTokens.add(text);
                }
            }
            for (int i = 1; i < tens.size(); i++) {
                numWord.put(tens.get(i), new ScaleIncrementPair(1, i * 10));
            }

            JsonNode scalesNode = rootNode.findPath("scales");
            if ((scalesNode != null) && (!scalesNode.isMissingNode())) {
                Iterator<JsonNode> iter = scalesNode.elements();
                while (iter.hasNext()) {
                    String text = ((JsonNode) iter.next()).textValue();
                    scales.add(text);
                    numberTokens.add(text);
                }
            }
            for (int i = 0; i < scales.size(); i++) {
                if (i == 0)
                    numWord.put(scales.get(i), new ScaleIncrementPair(100, 0));
                else {
                    numWord.put(scales.get(i), new ScaleIncrementPair(Math.pow(10, (i * 3)), 0));
                    if (scales.get(i).endsWith("lion")) {
                        numWord.put(scales.get(i) + "s", new ScaleIncrementPair(Math.pow(10, (i * 3)), 0));
                    }
                }
            }

            JsonNode decimalNode = rootNode.findPath("decimalMark");
            if ((decimalNode != null) && (!decimalNode.isMissingNode())) {
                Iterator<JsonNode> iter = decimalNode.elements();
                while (iter.hasNext()) {
                    String text = ((JsonNode) iter.next()).textValue();
                    decimalMarks.add(text);
                    numberTokens.add(text);
                }
            }
            decimalMark = decimalMarks.get(0);

            specials = fillAsMap(rootNode, "specials", null);
            fractions = fillAsMap(rootNode, "fractions", null);
        } catch (IOException e) {
            logger.error("Error when reading the values.json file");
        }

    }

    private Map<String, Double> fillAsMap(JsonNode rootNode, String rootPath, Set<String> numberTokens) {
        Map<String, Double> map = new HashMap<>();

        JsonNode node = rootNode.findPath(rootPath);
        if ((node != null) && (!node.isMissingNode())) {
            Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> text = iter.next();
                map.put(text.getKey(), text.getValue().doubleValue());
                if (numberTokens != null) {
                    numberTokens.add(text.getKey());
                }
            }
        }

        return map;
    }

    private List<String> fillAsList(JsonNode rootNode, String rootPath, Set<String> numberTokens) {
        List<String> list = new ArrayList<>();
        JsonNode node = rootNode.findPath(rootPath);
        if ((node != null) && (!node.isMissingNode())) {
            Iterator<JsonNode> iter = node.elements();
            while (iter.hasNext()) {
                String text = ((JsonNode) iter.next()).textValue();
                list.add(text);
                numberTokens.add(text);
            }
        }

        return list;
    }

    public Set<String> getTokenSet() {
        return numberTokens;
    }

    public BigDecimal normalize(String text, Locale local) throws NormalizationException {
        text = StringUtils.lowerCase(text);

        String numericPart = "";
        // Check if we have a number, e.g. 3.5 millions
        Matcher matcher = NUMERIC_PATTERN.matcher(text);
        if (matcher.find()) {
            numericPart = matcher.group();
        }

        // If we have a numeric part but the decimal mark is written in alphabetic characters, we abort the normalisation
        if (StringUtils.isNotBlank(numericPart) && text.contains(decimalMark)) {
            throw new NormalizationException("Cannot convert the alphabetic value '" + text + "' to digits");
        } else if (OUT_OF_PATTERN_NUMBERS.matcher(text).find()) {
            Matcher m = OUT_OF_PATTERN_NUMBERS.matcher(text);
            m.matches();
            String numerator = m.group(1);
            String denominator = m.group(m.groupCount());

            BigDecimal division = null;
            try {
                division = new BigDecimal(numerator).divide(new BigDecimal(denominator));
            } catch (ArithmeticException ae) {
                division = new BigDecimal(numerator).divide(new BigDecimal(denominator), 10, BigDecimal.ROUND_HALF_UP);
            }
            return division;
        } else if (OUT_OF_PATTERN_ALPHABETIC.matcher(text).find()) {
            Matcher m = OUT_OF_PATTERN_ALPHABETIC.matcher(text);
            m.matches();
            String numerator = m.group(1);
            String denominator = m.group(m.groupCount());
            BigDecimal division = null;
            try {
                division = convertIntegerPart(numerator).divide(convertIntegerPart(denominator));
            } catch (ArithmeticException ae) {
                division = convertIntegerPart(numerator).divide(convertIntegerPart(denominator), 10, BigDecimal.ROUND_HALF_UP);
            }
            return division;
        } else if (StringUtils.isNotBlank(numericPart)) {

            String[] split = text.split(numericPart);

            String alphabeticPart = "";
            if (split.length == 2) {
                // we assume the alphabetical is after the numeric
                alphabeticPart = StringUtils.trim(split[1]);
            } else {
                throw new NormalizationException("The alphabetical values cannot be properly parsed. ");
            }

            return convertIntegerPart(alphabeticPart, new BigDecimal(numericPart).doubleValue());

        } else if (text.contains(decimalMark)) {
            // split integer and possible decimal part
            String[] pieces = text.split(decimalMark);
            if (ArrayUtils.isEmpty(pieces)) {
                throw new NormalizationException("Cannot convert the alphabetic value '" + text + "' to digits");
            }
            String integerPart = pieces[0].trim();
            String decimalPart = null;
            if (pieces.length > 1)
                decimalPart = pieces[1].trim();

            BigDecimal result = new BigDecimal("0");
            if (StringUtils.isNotBlank(integerPart)) {
                result = convertIntegerPart(integerPart);
            }

            // decimal part
            BigDecimal decimalResult = convertDecimalPart(decimalPart, local);

            return result.add(decimalResult);
        } else if (fractions.keySet().stream().filter(text::contains).findFirst().isPresent()) {
            // one xyzty = 1/xyz
            String matchingElement = fractions.keySet()
                .stream()
                .filter(text::contains)
                .findFirst()
                .get();

            String[] pieces = text.split("\\W");

            if (pieces.length > 2) {
                throw new NormalizationException("Cannot convert the alphabetic value '" + text + "' to digits. Such type of fraction is not yet supported.");
            }
            if (pieces.length == 1) {
                throw new NormalizationException("Cannot convert the alphabetic value '" + text + "' to digits. Invalid fraction.");
            }
            ScaleIncrementPair scaleIncrementPair = numWord.get(pieces[0]);
            BigDecimal numeratorOfFraction = null;
            if (scaleIncrementPair != null) {
                numeratorOfFraction = new BigDecimal(scaleIncrementPair.increment);
            } else {
                if (specials.get(pieces[0]) != null) {
                    numeratorOfFraction = new BigDecimal(specials.get(pieces[0]));
                } else {
                    throw new NormalizationException("Cannot convert the alphabetic value '" + text + "' to digits. Not a fraction.");
                }
            }

            return numeratorOfFraction.multiply(BigDecimal.valueOf(fractions.get(matchingElement)));
        } else {
            return convertIntegerPart(text);
        }
    }

    protected BigDecimal convertIntegerPart(String integerPart) {
        return convertIntegerPart(integerPart, 0);
    }

    protected BigDecimal convertIntegerPart(String integerPart, double current) {
        double result = 0;

        String[] pieces = integerPart.split("\\W"); // or limit to split(" |-|—");
        for (String word : pieces) {
            ScaleIncrementPair scaleIncrement = numWord.get(word);
            if (scaleIncrement == null) {
                logger.debug("Invalid token to be converted into number: " + word);
                continue;
            }
            current = current * scaleIncrement.scale + scaleIncrement.increment;
            if (scaleIncrement.scale > 100) {
                result += current;
                current = 0;
            }
        }
        result += current;
        return new BigDecimal(String.valueOf(result));
    }

    @NotNull
    protected BigDecimal convertDecimalPart(String decimalPart, Locale locale) {
        String[] pieces;
        BigDecimal decimalResult = new BigDecimal(0);
        if (StringUtils.isNotBlank(decimalPart)) {
            pieces = decimalPart.split("\\W"); // or limit to split(" |-|—");
            StringBuilder res = new StringBuilder().append("0.");
            for (String piece : pieces) {
                String word = piece.trim();
                if (word.isEmpty())
                    continue;
                ScaleIncrementPair scaleIncrement = numWord.get(word);
                if (scaleIncrement == null) {
                    logger.warn("Invalid decimal token to be converted into number: " + word);
                    continue;
                }
                res.append(scaleIncrement.increment);
            }
            NumberFormat format = NumberFormat.getInstance(locale);
            try {
                Number number = format.parse(res.toString());
                decimalResult = new BigDecimal(number.toString());
            } catch (ParseException pe) {
                logger.error("Invalid value expression: " + res + " , for: " + decimalPart);
            }
        }
        return decimalResult;
    }

    public class ScaleIncrementPair {

        public double scale;
        public int increment;

        public ScaleIncrementPair(double s, int i) {
            scale = s;
            increment = i;
        }
    }
}
