package org.grobid.core.utilities;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * 
 */
public class WordsToNumberTest {

    private WordsToNumber wordsToNumber = null;

    @Before
    public void setUp() throws Exception {
        wordsToNumber = new WordsToNumber();
    }

    @Test
    public void testNonDecimal() throws Exception {
        String input = "two hundred two million fifty three thousand and four";
        BigDecimal number = wordsToNumber.normalize(input);
        System.out.println(input + ": " + number.toString());

        input = "fifteen";
        number = wordsToNumber.normalize(input);
        System.out.println(input + ": " + number);
    }

    @Test
    public void testDecimal() throws Exception {
        String input = "two hundred two million fifty three thousand point zero eight five eight zero two";
        BigDecimal number = wordsToNumber.normalize(input);
        System.out.println(input + ": " + number.toString());
    }    
}