package org.grobid.core.utilities

import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Ignore
import org.junit.Test
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.util.*

class TextParserTest {
    @Ignore("Cannot reproduce the surrogate character")
    @Test
    @Throws(Exception::class)
    fun testConvertFractions6Numeric() {
        val byteArray = byteArrayOf(-3, -1, -73, 0, 103, 0, 47, 0, 109, 0, 108, 0);
        val input = String(byteArray, StandardCharsets.UTF_16LE)

        val output = TextParser.handleRawData(input)
        MatcherAssert.assertThat(output, Is.`is`("·g/ml"))
    }
}
