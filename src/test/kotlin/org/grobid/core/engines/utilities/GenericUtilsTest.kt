package org.grobid.core.engines.utilities

import junit.framework.TestCase.assertEquals
import org.junit.Test

class GenericUtilsTest {


    @Test
    fun testCorrectLabelling() {

        var resultLabelling: String = "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
            "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
            "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
            "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
            "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>" 

        var correctedLabelling = GenericUtils.correctLabelling(resultLabelling)
        
        assertEquals(resultLabelling.replace("<valueAtomic>", "<valueMost>"), correctedLabelling)
    }

    @Test
    fun testCorrectLabelling_twoExamples() {

        var input: String = "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
            "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
            "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\tI-<valueAtomic>\n" +
            "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueMost>\n" +
            "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueLeast>"

        var expected: String = "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
            "16\t16\t1\t16\t16\t16\t6\t16\t16\t16\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
            ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueMost>\n" +
            "91\t91\t9\t91\t91\t91\t1\t91\t91\t91\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueMost>\n" +
            "kOe\tkoe\tk\tkO\tkOe\tkOe\te\tOe\tkOe\tkOe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXx\txXx\t1\t0\tI-<unitLeft>\n" +
            ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
            "as\tas\ta\tas\tas\tas\ts\tas\tas\tas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
            "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\tI-<valueAtomic>\n" +
            "addition\taddition\ta\tad\tadd\taddi\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>\n" +
            "amount\tamount\ta\tam\tamo\tamou\tt\tnt\tunt\tount\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<valueAtomic>"

        var output = GenericUtils.correctLabelling(input)

        assertEquals(expected, output)
    }

}