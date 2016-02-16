package org.grobid.core.analyzers;

import org.grobid.core.layout.LayoutToken;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Default tokenizer adequate for all Indo-European languages.
 *
 * @author Patrice Lopez
 */

public class QuantityAnalyzer {

    public static final String DELIMITERS = " \n\r\t([^%‰°,:;?.!/)-–=\"“”‘’'`$]*\u2666\u2665\u2663\u2660\u00A0";
    private static final String REGEX = "(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=\\D)";

    public static List<String> tokenize(String text) {
        List<String> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(text, DELIMITERS, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            // in addition we split "letter" characters and digits
            String[] subtokens = token.split(REGEX);
            for (int i = 0; i < subtokens.length; i++) {
                result.add(subtokens[i]);
            }
        }

        return result;
    }

    public static List<LayoutToken> tokenizeWithLayoutToken(String text) {
        List<LayoutToken> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(text, DELIMITERS, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            // in addition we split "letter" characters and digits
            String[] subtokens = token.split(REGEX);
            for (int i = 0; i < subtokens.length; i++) {
                LayoutToken layoutToken = new LayoutToken();
                layoutToken.setText(subtokens[i]);
                result.add(layoutToken);
            }
        }

        return result;
    }

    public static List<String> reTokenize(List<String> chunks) {
        List<String> result = new ArrayList<>();
        for (String chunk : chunks) {
            result.addAll(tokenize(chunk));
        }
        return result;
    }
}