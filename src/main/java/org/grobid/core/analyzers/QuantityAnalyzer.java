package org.grobid.core.analyzers;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.lang.Language;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.UnicodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Quantity tokenizer adequate for all Indo-European languages and special characters.
 * <p>
 * The difference with the Standard Grobid tokenizer is that this tokenizer
 * is also tokenizing mixture of alphabetical and numerical characters.
 * <p>
 * 1m74 ->  tokens.add(new LayoutToken("1"));
 * tokens.add(new LayoutToken("m"));
 * tokens.add(new LayoutToken("74"));
 *
 * @author Patrice Lopez
 */

public class QuantityAnalyzer implements Analyzer {

    private static volatile QuantityAnalyzer instance;

    public static QuantityAnalyzer getInstance() {
        if (instance == null) {
            //double check idiom
            // synchronized (instanceController) {
            if (instance == null)
                getNewInstance();
            // }
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new QuantityAnalyzer();
    }

    /**
     * Hidden constructor
     */
    private QuantityAnalyzer() {
    }

    public static final String DELIMITERS = " \n\r\t\f([^%‰°•⋅·,:;?.!/)-–−‐=~∼≈<>+±\"“”‘’'`#$]*\u2666\u2665\u2663\u2660\u00A0";
    private static final String REGEX = "(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=\\D)";

    public String getName() {
        return "QuantityAnalyzer";
    }

    public List<String> tokenize(String text) {
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

    public List<String> tokenize(String text, Language lang) {
        return tokenize(text);
    }

    public List<LayoutToken> tokenizeWithLayoutToken(String text) {
        List<LayoutToken> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(text, DELIMITERS, true);
        int offset = 0;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            // in addition we split "letter" characters and digits
            String[] subtokens = token.split(REGEX);
            for (int i = 0; i < subtokens.length; i++) {
                LayoutToken layoutToken = new LayoutToken();
                layoutToken.setText(subtokens[i]);
                layoutToken.setOffset(offset);
                offset += subtokens[i].length();
                result.add(layoutToken);
            }
        }

        return result;
    }

    @Override
    public List<String> retokenizeSubdigits(List<String> list) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public List<LayoutToken> retokenizeSubdigitsWithLayoutToken(List<String> list) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public List<LayoutToken> retokenizeSubdigitsFromLayoutToken(List<LayoutToken> list) {
        throw new NotImplementedException("Not yet implemented");
    }

    public List<String> retokenize(List<String> chunks) {
        List<String> result = new ArrayList<>();
        for (String chunk : chunks) {
            result.addAll(tokenize(chunk));
        }
        return result;
    }

    public List<LayoutToken> retokenizeLayoutTokens(List<LayoutToken> tokens) {
        List<LayoutToken> result = new ArrayList<>();
        for (LayoutToken token : tokens) {
            result.addAll(tokenize(token, token.getOffset()));
        }
        return result;
    }

    public List<LayoutToken> tokenize(LayoutToken chunk, int startingIndex) {
        List<LayoutToken> result = new ArrayList<>();
        String text = chunk.getText();
        StringTokenizer st = new StringTokenizer(text, DELIMITERS, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            // in addition we split "letter" characters and digits
            String[] subtokens = token.split(REGEX);
            for (int i = 0; i < subtokens.length; i++) {
                LayoutToken theChunk = new LayoutToken(chunk); // deep copy
                theChunk.setText(subtokens[i]);
                result.add(theChunk);
                theChunk.setOffset(startingIndex);
                startingIndex += StringUtils.length(theChunk.getText());
            }
        }
        return result;
    }

    public List<String> tokenizeByCharacter(String text) {
        char[] tokenizationByCharacter = text.toCharArray();
        List<String> tokenizations = new ArrayList<>();
        for (char characterToken : tokenizationByCharacter) {
            tokenizations.add("" + characterToken);
        }
        return tokenizations;
    }


    public List<LayoutToken> tokenizeWithLayoutTokenByCharacter(String text) {
        List<LayoutToken> layoutTokens = new ArrayList<>();

        for (char character : text.toCharArray()) {
            OffsetPosition position = new OffsetPosition();
            position.start = text.indexOf(character);
            position.end = text.indexOf(character) + 1;
            LayoutToken lt = new LayoutToken(UnicodeUtil.normaliseText(String.valueOf(character)));
            layoutTokens.add(lt);
        }
        return layoutTokens;
    }
}