package org.grobid.core.engines

import org.grobid.core.GrobidModels
import org.grobid.core.utilities.GrobidConfig.ModelParameters
import org.grobid.core.utilities.GrobidProperties
import org.grobid.core.utilities.OffsetPosition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class QuantityParserTest {
    private var target: QuantityParser? = null

    @BeforeEach
    fun setUp() {
        target = QuantityParser(GrobidModels.DUMMY, null, null, null)
    }

    @AfterEach
    fun tearDown() {
    }


    @Test
    fun testGetSentence_entityInsideSentence() {

        val sentences: List<OffsetPosition> = listOf(
            OffsetPosition(0, 3),
            OffsetPosition(4, 6),
            OffsetPosition(7, 10)
        )

        val entityPosition = OffsetPosition(1, 2)

        val foundSentence = target?.findSentenceOffset(sentences, entityPosition)

        assertThat(foundSentence, `is`(sentences[0]))
    }


    @Test
    fun testGetSentence_entityBetweenSentences() {

        val sentences: List<OffsetPosition> = listOf(
            OffsetPosition(0, 3),
            OffsetPosition(4, 6),
            OffsetPosition(7, 10)
        )

        val entityPosition = OffsetPosition(2, 5)

        val foundSentence = target?.findSentenceOffset(sentences, entityPosition)

        assertThat(foundSentence, `is`(OffsetPosition(0, 10)))
    }

    @Test
    fun testGetSentence_entityIncludingASentence() {

        val sentences: List<OffsetPosition> = listOf(
            OffsetPosition(0, 3),
            OffsetPosition(4, 6),
            OffsetPosition(7, 10)
        )

        val entityPosition = OffsetPosition(2, 8)

        val foundSentence = target?.findSentenceOffset(sentences, entityPosition)

        assertThat(foundSentence, `is`(OffsetPosition(0, 10)))
    }

    companion object {
        @JvmStatic
        @BeforeAll
        @Throws(Exception::class)
        fun before() {
            val modelParameters = ModelParameters()
            modelParameters.name = "bao"
            GrobidProperties.addModel(modelParameters)
        }
    }


}