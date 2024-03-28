package org.grobid.core.utilities

import org.grobid.core.data.normalization.NormalizationException
import org.grobid.core.utilities.WordsToNumber
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class WordsToNumberTest {
    lateinit var target: WordsToNumber

    @Before
    @Throws(Exception::class)
    fun setUp() {
        target = WordsToNumber.getInstance()
    }

    @Test
    @Throws(Exception::class)
    fun testNonDecimal_1() {
        val input = "two hundred two million fifty three thousand and four"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("202053004").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun testNonDecimal_2() {
        val input = "fifteen"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("15.0")))
    }


    @Test
    @Throws(Exception::class)
    fun testDecimal_1() {
        val input = "two hundred two million fifty three thousand point zero eight five eight zero two"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("202053000.085802")))
    }

    @Test
    @Throws(Exception::class)
    fun testDecimal_2() {
        val input = "point zero eight five eight zero two"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.085802")))
    }

    @Test(expected = NormalizationException::class)
    @Throws(Exception::class)
    fun testCornerCase() {
        //Sometimes the word 'point' alone is recognised as value... this test was used to simulate the
        // exception
        val input = "point"
        target.normalize(input, Locale.ENGLISH)
    }

    @Test
    @Throws(Exception::class)
    fun testThreeBillions() {
        val input = "three billion"
        var number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("3000000000").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun test20Million() {
        val input = "twenty millions five hundred twenty five"
        var number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("20000525")))
    }

    @Test
    @Throws(Exception::class)
    fun test3Billions() {
        val input = "3 billion"
        var number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("3000000000").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun test3_8Billions() {
        val input = "3.8 billions"
        var number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("3800000000").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertIntegerPart() {
        val input = "two hundred two million fifty three thousand"
        val number = target.convertIntegerPart(input)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("202053000").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertDecimalPart() {
        val input = "three four five"
        val number = target.convertDecimalPart(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.345")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions1() {
        val input = "one third"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number.toPlainString(), Is.`is`(BigDecimal("0.3333333333").toPlainString()))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions2() {
        val input = "one fifth"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.2")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions5() {
        val input = "two fifth"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.4")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions2Mixed() {
        val input = "a fifth"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.2")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions3() {
        val input = "ten third"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("3.3333333330")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions4() {
        val input = "three out of four"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.75")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions6() {
        val input = "three out of the four"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.75")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions4Numeric() {
        val input = "3 out of 4"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.75")))
    }

    @Test
    @Throws(Exception::class)
    fun testConvertFractions6Numeric() {
        val input = "3 out of the 4"
        val number = target.normalize(input, Locale.ENGLISH)
        MatcherAssert.assertThat(number, Is.`is`(BigDecimal("0.75")))
    }

    @Test(expected = NormalizationException::class)
    @Throws(Exception::class)
    fun testConvertInvalidFractions1() {
        val input = "a temperature of 20"
        target.normalize(input, Locale.ENGLISH)
    }
}