package org.grobid.core.engines.utilities

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LabellingUtilsTest {


    @Test
    fun testCorrectLabelling_single_shouldWork() {

        var resultLabelling: String =
            "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>"

        var correctedLabelling = LabellingUtils.correctLabelling(resultLabelling)

        assertEquals(resultLabelling.replace("<valueAtomic>", "<valueMost>"), correctedLabelling)
    }

    @Test
    fun testCorrectLabelling_double_shouldWork() {

        var input: String =
            "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\tI-<valueAtomic>\n" +
                "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueMost>\n" +
                "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueLeast>"

        var expected: String =
            "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueMost>\n" +
                "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueMost>\n" +
                "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\tI-<valueAtomic>\n" +
                "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>\n" +
                "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(expected, output)
    }

    @Test
    fun testCorrectLabelling_noIssue_shouldNotChangeOutput() {

        var input: String =
            "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\tI-<valueAtomic>\n" +
                "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>\n" +
                "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(input, output)
    }

    @Test
    fun testCorrectLabelling_valueParser() {
        val input = "3\t1\t1\tNOPUNCT\tI-<alpha>\n" +
            ".\t1\t0\tDOT\t<alpha>\n" +
            "6\t1\t1\tNOPUNCT\t<number>\n" +
            "billion\t0\t0\tNOPUNCT\t<alpha>"

        var output = LabellingUtils.correctLabelling(input)

        val expected = "3\t1\t1\tNOPUNCT\tI-<alpha>\n" +
            ".\t1\t0\tDOT\t<alpha>\n" +
            "6\t1\t1\tNOPUNCT\t<alpha>\n" +
            "billion\t0\t0\tNOPUNCT\t<alpha>"

        assertEquals(expected, output)
    }

    @Test
    fun testCorrectingRangeValues1() {
        val input = "70\t70\t7\t70\t70\t70\t0\t70\t70\t70\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(output, input)
    }

    @Test
    fun testCorrectingRangeValues2() {
        val input = "70\t70\t7\t70\t70\t70\t0\t70\t70\t70\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(output, input)
    }

    @Test
    fun testCorrectingRangeValues3() {
        val input = "70\t70\t7\t70\t70\t70\t0\t70\t70\t70\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(output, input)
    }

    @Test
    fun testCorrectingRangeValues4() {
        val input = "70\t70\t7\t70\t70\t70\t0\t70\t70\t70\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\tI-<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(output, input)
    }


}