package org.grobid.core.analyzers;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

public class QuantityAnalyzerTest {
    private QuantityAnalyzer target;

    @Before
    public void setUp() throws Exception {
        target = QuantityAnalyzer.getInstance();
    }

    @Test
    public void testTokenize_plainText() throws Exception {
        List<String> tokens = target.tokenize("This is a sample text, with 1.5m of intelligence.");

        assertThat(tokens, hasSize(22));
        assertThat(tokens.get(0), is("This"));
        assertThat(tokens.get(21), is("."));
    }

    @Test
    public void testTokenizeWithLayoutToken_testOffsetConsistency() throws Exception {
        List<LayoutToken> tokens = target.tokenizeWithLayoutToken("This is a sample text, with 1.5m of intelligence.");
        List<LayoutToken> reTokens = target.retokenizeLayoutTokens(tokens);

        assertThat(tokens, hasSize(reTokens.size()));

        for (int i = 0; i < reTokens.size(); i++) {
            assertThat(tokens.get(i).getOffset(), is(reTokens.get(i).getOffset()));
        }
    }

    @Test
    public void testTokenizeWithLayoutToken_checkOffsets() throws Exception {
        List<LayoutToken> tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken("This is a sample text, with 1.5m of intelligence.");
        List<LayoutToken> reTokens = target.retokenizeLayoutTokens(tokens);

        assertThat(tokens.size(), lessThan(reTokens.size()));
    }

    @Test
    public void testTokenize_3() throws Exception {
        String inputText = "This is a sample text, with 1.5m of intelligence.";
        List<LayoutToken> tokens = target.tokenizeWithLayoutToken(inputText);

        assertThat(tokens, hasSize(22));
        assertThat(LayoutTokensUtil.toText(tokens), is(inputText));
        assertThat(tokens.get(0).getText(), is("This"));
        assertThat(tokens.get(0).getOffset(), is(0));
        assertThat(tokens.get(15).getText(), is("5"));
        assertThat(tokens.get(15).getOffset(), is(30));
    }

    @Test
    public void testSpecialCharacters() throws Exception {
        String inputText = "was maintained at 8.0±0.02, for which the DIC";

        List<LayoutToken> tokens = target.tokenizeWithLayoutToken(inputText);

//        tokens.stream().forEach(System.out::println);
        assertThat(tokens, hasSize(22));

        assertThat(tokens.get(8).getText(), is("0"));
        assertThat(tokens.get(9).getText(), is("±"));
        assertThat(tokens.get(10).getText(), is("0"));
    }

}