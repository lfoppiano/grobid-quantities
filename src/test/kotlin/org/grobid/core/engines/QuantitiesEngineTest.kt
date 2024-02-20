package org.grobid.core.engines;

import org.grobid.core.analyzers.QuantityAnalyzer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class QuantitiesEngineTest {

    @Test
    fun normaliseAndCleanup() {
        val tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken("This \uF0A0 is an interesting") 
        val tokensNormalised = QuantitiesEngine.normaliseAndCleanup(tokens)
        
        assertThat(tokensNormalised, hasSize(tokens.size))
        assertThat(tokensNormalised[2].text, `is`(" "))
    }
}