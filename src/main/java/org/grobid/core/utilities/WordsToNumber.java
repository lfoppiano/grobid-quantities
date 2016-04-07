package org.grobid.core.utilities;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert a number expressed with alphabetical charcaters into a normalized numerical value.
 * 
 * @author Patrice Lopez
 */
public class WordsToNumber {

    private static final Logger logger = LoggerFactory.getLogger(WordsToNumber.class);

    private static List<String> units = Arrays.asList("zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
        "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen");
    private static List<String> tens = Arrays.asList(null, null, "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety");
    private static List<String> scales = Arrays.asList("hundred", "thousand", "million", "billion", "trillion");
    private static String decimalMark = "point";

    // TBD: special words for numbers to be considered
    /*
    .put("dozen", 12);
    .put("score", 20);
    .put("gross", 144);
    .put("quarter", 0.25);
    .put("half", 0.5);
    .put("oh", 0);
    */

    // the lexicon
    private Map<String, ScaleIncrementPair> numWord = null; 

    public WordsToNumber() {
        // init the lexicon with the numerical operations
        numWord = new HashMap<String, ScaleIncrementPair>();
        numWord.put("and", new ScaleIncrementPair(1, 0));

        for (int i = 0; i < units.size(); i++) {
            numWord.put(units.get(i), new ScaleIncrementPair(1, i));
        }

        for (int i = 1; i < tens.size(); i++) {
            numWord.put(tens.get(i), new ScaleIncrementPair(1, i * 10));                
        }

        for (int i = 0; i < scales.size(); i++) {
            if(i == 0)
                numWord.put(scales.get(i), new ScaleIncrementPair(100, 0));
            else
                numWord.put(scales.get(i), new ScaleIncrementPair(Math.pow(10, (i*3)), 0));
        }
    }

    public BigDecimal normalize(String text, Locale local) {
        double current = 0; 
        double result = 0;
        text = text.toLowerCase();

        // split integer and possible decimal part
        String pieces[] = text.split(decimalMark);
        String integerPart = pieces[0].trim();
        String decimalPart = null;
        if (pieces.length > 1)
            decimalPart = pieces[1].trim();

        if ((integerPart != null) && (integerPart.length() > 0)) {
            pieces = integerPart.split("\\W"); // or limit to split(" |-|—");
            for(int i=0; i< pieces.length; i++) {
                String word = pieces[i];
                ScaleIncrementPair scaleIncrement = numWord.get(word);
                if (scaleIncrement == null) {
                    logger.warn("Invalid token to be converted into number: " + word);
                    continue;
                }
                current = current * scaleIncrement.scale + scaleIncrement.increment;
                if (scaleIncrement.scale > 100) {
                    result += current;
                    current = 0;
                }
            }
            result += current;
        }

        // decimal part 
        BigDecimal decimalResult = new BigDecimal(0);
        if ((decimalPart != null) && (decimalPart.length() > 0)) {
            pieces = decimalPart.split("\\W"); // or limit to split(" |-|—");
            StringBuilder res = new StringBuilder().append("0.");
            for(int i=0; i< pieces.length; i++) {
                String word = pieces[i].trim();
                if (word.length() == 0) 
                    continue;
                ScaleIncrementPair scaleIncrement = numWord.get(word);
                if (scaleIncrement == null) {
                    logger.warn("Invalid decimal token to be converted into number: " + word);
                    continue;
                }
                res.append(scaleIncrement.increment); 
            }
            NumberFormat format = NumberFormat.getInstance(local);
            try {
                Number number = format.parse(res.toString());
                decimalResult = new BigDecimal(number.toString());
            } catch (ParseException pe) {
                logger.error("Invalid value expression: " + res.toString() + " , for: " + decimalPart);
            }
        }

        return new BigDecimal(result).add(decimalResult);
    }
}
