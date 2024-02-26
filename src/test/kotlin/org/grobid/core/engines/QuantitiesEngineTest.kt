package org.grobid.core.engines;

import org.grobid.core.analyzers.QuantityAnalyzer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class QuantitiesEngineTest {

    @Test
    fun normaliseAndCleanup_shouldReplaceToken() {
        val tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("This \uF0A0 is an interesting")
        val tokensNormalised = QuantitiesEngine.normaliseAndCleanup(tokens)

        assertThat(tokensNormalised, hasSize(tokens.size - 2))
        assertThat(tokensNormalised[1].text, `is`(" "))
        assertThat(tokensNormalised[2].text, `is`("is"))
        assertThat(tokensNormalised[0].offset, `is`(0))    
        assertThat(tokensNormalised[1].offset, `is`(4))    
        assertThat(tokensNormalised[2].offset, `is`(5))    
        assertThat(tokensNormalised[3].offset, `is`(7))    
        assertThat(tokensNormalised[4].offset, `is`(8))    
        assertThat(tokensNormalised[5].offset, `is`(10))
    }

    @Test
    fun normaliseAndCleanup_shouldNotReplaceToken() {
        val tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("This material is an interesting")
        val tokensNormalised = QuantitiesEngine.normaliseAndCleanup(tokens)

        assertThat(tokensNormalised, hasSize(tokens.size))
        assertThat(tokensNormalised[2].text, `is`("material"))
    }
}