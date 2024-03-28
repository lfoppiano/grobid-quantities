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
    fun testCorrectingRangeValues() {
        val input = "70\t70\t7\t70\t70\t70\t0\t70\t70\t70\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "9\t9\t9\t9\t9\t9\t9\t9\t9\t9\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "5\t5\t5\t5\t5\t5\t5\t5\t5\t5\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "%\t%\t%\t%\t%\t%\t%\t%\t%\t%\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t%\t%\t0\t0\t<unitLeft>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "fat\tfat\tf\tfa\tfat\tfat\tt\tat\tfat\tfat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
            "mass\tmass\tm\tma\tmas\tmass\ts\tss\tass\tmass\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "VO\tvo\tV\tVO\tVO\tVO\tO\tVO\tVO\tVO\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXX\tX\t0\t0\t<other>\n" +
            "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
            "max\tmax\tm\tma\tmax\tmax\tx\tax\tmax\tmax\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            ":\t:\t:\t:\t:\t:\t:\t:\t:\t:\tALLCAPS\tNODIGIT\t1\tPUNCT\t:\t:\t0\t0\t<other>\n" +
            "50\t50\t5\t50\t50\t50\t0\t50\t50\t50\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "8\t8\t8\t8\t8\t8\t8\t8\t8\t8\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "ml\tml\tm\tml\tml\tml\tl\tml\tml\tml\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\t<unitLeft>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<unitLeft>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<unitLeft>\n" +
            "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\t<unitLeft>\n" +
            "min\tmin\tm\tmi\tmin\tmin\tn\tin\tmin\tmin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<unitLeft>\n" +
            "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<unitLeft>\n" +
            "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<unitLeft>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "21\t21\t2\t21\t21\t21\t1\t21\t21\t21\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
            "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "race\trace\tr\tra\trac\trace\te\tce\tace\trace\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "A\ta\tA\tA\tA\tA\tA\tA\tA\tA\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
            "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
            "6\t6\t6\t6\t6\t6\t6\t6\t6\t6\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueAtomic>\n" +
            "women\twomen\tw\two\twom\twome\tn\ten\tmen\tomen\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
            "men\tmen\tm\tme\tmen\tmen\tn\ten\tmen\tmen\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "40\t40\t4\t40\t40\t40\t0\t40\t40\t40\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "7\t7\t7\t7\t7\t7\t7\t7\t7\t7\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "years\tyears\ty\tye\tyea\tyear\ts\trs\tars\tears\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "176\t176\t1\t17\t176\t176\t6\t76\t176\t176\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "7\t7\t7\t7\t7\t7\t7\t7\t7\t7\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueRange>\n" +
            "cm\tcm\tc\tcm\tcm\tcm\tm\tcm\tcm\tcm\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>\n" +
            ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
            "72\t72\t7\t72\t72\t72\t2\t72\t72\t72\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueBase>\n" +
            "±\t±\t±\t±\t±\t±\t±\t±\t±\t±\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t±\t±\t0\t0\t<other>\n" +
            "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueRange>\n" +
            "kg\tkg\tk\tkg\tkg\tkg\tg\tkg\tkg\tkg\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<unitLeft>"

        var output = LabellingUtils.correctLabelling(input)

        assertEquals(output, input)
    }

}