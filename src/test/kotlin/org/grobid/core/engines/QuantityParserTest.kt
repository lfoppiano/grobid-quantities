package org.grobid.core.engines

import org.easymock.EasyMock
import org.easymock.Mock
import org.grobid.core.GrobidModel
import org.grobid.core.GrobidModels
import org.grobid.core.analyzers.QuantityAnalyzer
import org.grobid.core.data.Measurement
import org.grobid.core.data.Quantity
import org.grobid.core.utilities.GrobidConfig.ModelParameters
import org.grobid.core.utilities.GrobidProperties
import org.grobid.core.utilities.MeasurementOperations
import org.grobid.core.utilities.OffsetPosition
import org.grobid.core.utilities.UnitUtilities
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class QuantityParserTest {
    private lateinit var target: QuantityParser

    @Mock
    lateinit var mockValueParser: ValueParser

//    @Mock
//    lateinit var mockMeasurementOperations: MeasurementOperations

    @BeforeEach
    fun setUp() {
        mockValueParser = EasyMock.createMockBuilder<Any>(ValueParser::class.java)
            .withConstructor(GrobidModel::class.java)
            .withArgs(GrobidModels.DUMMY)
            .addMockedMethod("parseValue", String::class.java)
            .createMock()
//        mockMeasurementOperations = EasyMock.createMock(MeasurementOperations::class.java)
        target = QuantityParser(GrobidModels.DUMMY, null, MeasurementOperations(null), mockValueParser)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testFindSentenceOffsets() {
        val currentMeasurement = Measurement()
        currentMeasurement.type = UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX
        currentMeasurement.rawOffsets = OffsetPosition(8736, 8737)
        currentMeasurement.quantityLeast = Quantity("5", null, currentMeasurement.rawOffsets)

        val offset = 7753

        val sentences: MutableList<OffsetPosition> = ArrayList()
        sentences.add(OffsetPosition(0, 169))
        sentences.add(OffsetPosition(170, 261))
        sentences.add(OffsetPosition(262, 358))
        sentences.add(OffsetPosition(359, 513))
        sentences.add(OffsetPosition(514, 675))
        sentences.add(OffsetPosition(676, 816))
        sentences.add(OffsetPosition(817, 1053))
        sentences.add(OffsetPosition(1054, 1137))
        sentences.add(OffsetPosition(1138, 1579))
        sentences.add(OffsetPosition(1580, 1744))

        val output: OffsetPosition? = target?.findSentenceOffset(sentences, currentMeasurement, offset)

        assertThat(output?.start, `is`(sentences[6].start))
        assertThat(output?.end, `is`(sentences[6].end))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testReconstricting2ListWithMiddleUnit() {
        val text = """The Re-Lu films were prepared via magnetron sputtering 
in our ATC series UHV Hybrid deposition system (AJA 
International, Inc.) with a base pressure of 1 × 10 -8 Torr. The 
Re target (ACI Alloys, Inc. 99.99% purity) was accommod-
ated inside of a 1.5" DC gun. The Lu target (Heeger Materials, 
Inc. purity Lu/TREM 99.99%) was placed inside of a 2" DC 
gun. The sapphire substrate (AdValue Technology, thickness 
650 µm, C-cut) was cleaned thoroughly with isopropyl alco-
hol before it was mounted on the holder. In our chamber's con-
figuration, the substrate holder is at the center of the chamber 
facing upwards, while the (five) sputtering guns are located at 
the top. The substrate is rotated in plane throughout the whole 
deposition process to ensure a homogeneous deposition layer 
over the whole surface. Our predeposition in-situ cleaning of 
the substrate typically involves heating it up to 900 • C for 10 
min followed by a gentle bombardment of Ar + at 600 • C for 
5-10 min using a Kaufman ion source at 45 • to the substrate 
surface. Then the temperature was raised back to 900 • C and 
kept at that value for 30 min. Afterwards, the temperature was 
reduced to 600 • C and simultaneous deposition took place 
for 10 min, at pressure 3-4 mTorr (the increase to 4 mTorr 
was sometimes required for plasma stability; such action did 
not change the stoichiometry and just non-essentially reduced 
the deposition speed), with gun power 250-260 W and anode 
voltage 460-605 V for Re, and with gun power 45-90 W and 
anode voltage correspondingly 275-325 V for Lu. Keeping the 
Re gun power constant, and varying the sputtering power of Lu 
from case to case allowed us to vary the values of x in composi-
tion Re x Lu (see table """

        val tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text)

        val result =
            "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "Re\tre\tR\tRe\tRe\tRe\te\tRe\tRe\tRe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "films\tfilms\tf\tfi\tfil\tfilm\ts\tms\tlms\tilms\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "were\twere\tw\twe\twer\twere\te\tre\tere\twere\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "prepared\tprepared\tp\tpr\tpre\tprep\td\ted\tred\tared\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "via\tvia\tv\tvi\tvia\tvia\ta\tia\tvia\tvia\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "magnetron\tmagnetron\tm\tma\tmag\tmagn\tn\ton\tron\ttron\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "sputtering\tsputtering\ts\tsp\tspu\tsput\tg\tng\ting\tring\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "our\tour\to\tou\tour\tour\tr\tur\tour\tour\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "ATC\tatc\tA\tAT\tATC\tATC\tC\tTC\tATC\tATC\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXX\tX\t0\t0\t<other>\n" +
                "series\tseries\ts\tse\tser\tseri\ts\tes\ties\tries\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "UHV\tuhv\tU\tUH\tUHV\tUHV\tV\tHV\tUHV\tUHV\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXX\tX\t0\t0\t<other>\n" +
                "Hybrid\thybrid\tH\tHy\tHyb\tHybr\td\tid\trid\tbrid\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "deposition\tdeposition\td\tde\tdep\tdepo\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "system\tsystem\ts\tsy\tsys\tsyst\tm\tem\ttem\tstem\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "AJA\taja\tA\tAJ\tAJA\tAJA\tA\tJA\tAJA\tAJA\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXX\tX\t0\t0\t<other>\n" +
                "International\tinternational\tI\tIn\tInt\tInte\tl\tal\tnal\tonal\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "Inc\tinc\tI\tIn\tInc\tInc\tc\tnc\tInc\tInc\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "base\tbase\tb\tba\tbas\tbase\te\tse\tase\tbase\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "pressure\tpressure\tp\tpr\tpre\tpres\te\tre\ture\tsure\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueAtomic>\n" +
                "×\t×\t×\t×\t×\t×\t×\t×\t×\t×\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t×\t×\t0\t0\t<valueAtomic>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<valueAtomic>\n" +
                "8\t8\t8\t8\t8\t8\t8\t8\t8\t8\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueAtomic>\n" +
                "Torr\ttorr\tT\tTo\tTor\tTorr\tr\trr\torr\tTorr\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t1\t0\tI-<unitLeft>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "Re\tre\tR\tRe\tRe\tRe\te\tRe\tRe\tRe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "target\ttarget\tt\tta\ttar\ttarg\tt\tet\tget\trget\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "ACI\taci\tA\tAC\tACI\tACI\tI\tCI\tACI\tACI\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXX\tX\t0\t0\t<other>\n" +
                "Alloys\talloys\tA\tAl\tAll\tAllo\ts\tys\toys\tloys\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "Inc\tinc\tI\tIn\tInc\tInc\tc\tnc\tInc\tInc\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\tI-<valueAtomic>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "99\t99\t9\t99\t99\t99\t9\t99\t99\t99\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "99\t99\t9\t99\t99\t99\t9\t99\t99\t99\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "%\t%\t%\t%\t%\t%\t%\t%\t%\t%\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t%\t%\t0\t0\tI-<unitLeft>\n" +
                "purity\tpurity\tp\tpu\tpur\tpuri\ty\tty\tity\trity\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "accommod\taccommod\ta\tac\tacc\tacco\td\tod\tmod\tmmod\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "ated\tated\ta\tat\tate\tated\td\ted\tted\tated\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "inside\tinside\ti\tin\tins\tinsi\te\tde\tide\tside\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueAtomic>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "5\t5\t5\t5\t5\t5\t5\t5\t5\t5\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<valueAtomic>\n" +
                "\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\tALLCAPS\tNODIGIT\t1\tQUOTE\t\"\t\"\t0\t0\t<other>\n" +
                "DC\tdc\tD\tDC\tDC\tDC\tC\tDC\tDC\tDC\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXX\tX\t0\t0\t<other>\n" +
                "gun\tgun\tg\tgu\tgun\tgun\tn\tun\tgun\tgun\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "target\ttarget\tt\tta\ttar\ttarg\tt\tet\tget\trget\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "Heeger\theeger\tH\tHe\tHee\tHeeg\tr\ter\tger\teger\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "Materials\tmaterials\tM\tMa\tMat\tMate\ts\tls\tals\tials\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "Inc\tinc\tI\tIn\tInc\tInc\tc\tnc\tInc\tInc\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "purity\tpurity\tp\tpu\tpur\tpuri\ty\tty\tity\trity\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "/\t/\t/\t/\t/\t/\t/\t/\t/\t/\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t/\t/\t0\t0\t<other>\n" +
                "TREM\ttrem\tT\tTR\tTRE\tTREM\tM\tEM\tREM\tTREM\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXXXX\tX\t0\t0\t<other>\n" +
                "99\t99\t9\t99\t99\t99\t9\t99\t99\t99\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<valueAtomic>\n" +
                "99\t99\t9\t99\t99\t99\t9\t99\t99\t99\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<valueAtomic>\n" +
                "%\t%\t%\t%\t%\t%\t%\t%\t%\t%\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t%\t%\t0\t0\tI-<unitLeft>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "placed\tplaced\tp\tpl\tpla\tplac\td\ted\tced\taced\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "inside\tinside\ti\tin\tins\tinsi\te\tde\tide\tside\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "2\t2\t2\t2\t2\t2\t2\t2\t2\t2\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
                "\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\t\"\tALLCAPS\tNODIGIT\t1\tQUOTE\t\"\t\"\t0\t0\t<other>\n" +
                "DC\tdc\tD\tDC\tDC\tDC\tC\tDC\tDC\tDC\tALLCAPS\tNODIGIT\t0\tNOPUNCT\tXX\tX\t0\t0\t<other>\n" +
                "gun\tgun\tg\tgu\tgun\tgun\tn\tun\tgun\tgun\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "sapphire\tsapphire\ts\tsa\tsap\tsapp\te\tre\tire\thire\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "substrate\tsubstrate\ts\tsu\tsub\tsubs\te\tte\tate\trate\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "AdValue\tadvalue\tA\tAd\tAdV\tAdVa\te\tue\tlue\talue\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxXxxx\tXxXx\t0\t0\t<other>\n" +
                "Technology\ttechnology\tT\tTe\tTec\tTech\ty\tgy\togy\tlogy\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "thickness\tthickness\tt\tth\tthi\tthic\ts\tss\tess\tness\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "650\t650\t6\t65\t650\t650\t0\t50\t650\t650\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueAtomic>\n" +
                "µm\tµm\tµ\tµm\tµm\tµm\tm\tµm\tµm\tµm\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\tI-<unitLeft>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "cut\tcut\tc\tcu\tcut\tcut\tt\tut\tcut\tcut\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "cleaned\tcleaned\tc\tcl\tcle\tclea\td\ted\tned\taned\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "thoroughly\tthoroughly\tt\tth\ttho\tthor\ty\tly\thly\tghly\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "isopropyl\tisopropyl\ti\tis\tiso\tisop\tl\tyl\tpyl\topyl\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "alco\talco\ta\tal\talc\talco\to\tco\tlco\talco\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "hol\thol\th\tho\thol\thol\tl\tol\thol\thol\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "before\tbefore\tb\tbe\tbef\tbefo\te\tre\tore\tfore\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "it\tit\ti\tit\tit\tit\tt\tit\tit\tit\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "mounted\tmounted\tm\tmo\tmou\tmoun\td\ted\tted\tnted\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "on\ton\to\ton\ton\ton\tn\ton\ton\ton\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "holder\tholder\th\tho\thol\thold\tr\ter\tder\tlder\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "In\tin\tI\tIn\tIn\tIn\tn\tIn\tIn\tIn\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "our\tour\to\tou\tour\tour\tr\tur\tour\tour\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "chamber\tchamber\tc\tch\tcha\tcham\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "'\t'\t'\t'\t'\t'\t'\t'\t'\t'\tALLCAPS\tNODIGIT\t1\tQUOTE\t'\t'\t0\t0\t<other>\n" +
                "s\ts\ts\ts\ts\ts\ts\ts\ts\ts\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<other>\n" +
                "con\tcon\tc\tco\tcon\tcon\tn\ton\tcon\tcon\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "figuration\tfiguration\tf\tfi\tfig\tfigu\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "substrate\tsubstrate\ts\tsu\tsub\tsubs\te\tte\tate\trate\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "holder\tholder\th\tho\thol\thold\tr\ter\tder\tlder\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "center\tcenter\tc\tce\tcen\tcent\tr\ter\tter\tnter\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "chamber\tchamber\tc\tch\tcha\tcham\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "facing\tfacing\tf\tfa\tfac\tfaci\tg\tng\ting\tcing\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "upwards\tupwards\tu\tup\tupw\tupwa\ts\tds\trds\tards\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "while\twhile\tw\twh\twhi\twhil\te\tle\tile\thile\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "five\tfive\tf\tfi\tfiv\tfive\te\tve\tive\tfive\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t1\t<other>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                "sputtering\tsputtering\ts\tsp\tspu\tsput\tg\tng\ting\tring\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "guns\tguns\tg\tgu\tgun\tguns\ts\tns\tuns\tguns\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "are\tare\ta\tar\tare\tare\te\tre\tare\tare\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "located\tlocated\tl\tlo\tloc\tloca\td\ted\tted\tated\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "top\ttop\tt\tto\ttop\ttop\tp\top\ttop\ttop\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "The\tthe\tT\tTh\tThe\tThe\te\the\tThe\tThe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "substrate\tsubstrate\ts\tsu\tsub\tsubs\te\tte\tate\trate\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "is\tis\ti\tis\tis\tis\ts\tis\tis\tis\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "rotated\trotated\tr\tro\trot\trota\td\ted\tted\tated\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "plane\tplane\tp\tpl\tpla\tplan\te\tne\tane\tlane\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "throughout\tthroughout\tt\tth\tthr\tthro\tt\tut\tout\thout\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "whole\twhole\tw\twh\twho\twhol\te\tle\tole\thole\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "deposition\tdeposition\td\tde\tdep\tdepo\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "process\tprocess\tp\tpr\tpro\tproc\ts\tss\tess\tcess\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "ensure\tensure\te\ten\tens\tensu\te\tre\ture\tsure\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "homogeneous\thomogeneous\th\tho\thom\thomo\ts\tus\tous\teous\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "deposition\tdeposition\td\tde\tdep\tdepo\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "layer\tlayer\tl\tla\tlay\tlaye\tr\ter\tyer\tayer\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "over\tover\to\tov\tove\tover\tr\ter\tver\tover\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "whole\twhole\tw\twh\twho\twhol\te\tle\tole\thole\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "surface\tsurface\ts\tsu\tsur\tsurf\te\tce\tace\tface\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "Our\tour\tO\tOu\tOur\tOur\tr\tur\tOur\tOur\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxx\tXx\t0\t0\t<other>\n" +
                "predeposition\tpredeposition\tp\tpr\tpre\tpred\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "situ\tsitu\ts\tsi\tsit\tsitu\tu\ttu\titu\tsitu\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "cleaning\tcleaning\tc\tcl\tcle\tclea\tg\tng\ting\tning\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "substrate\tsubstrate\ts\tsu\tsub\tsubs\te\tte\tate\trate\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "typically\ttypically\tt\tty\ttyp\ttypi\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "involves\tinvolves\ti\tin\tinv\tinvo\ts\tes\tves\tlves\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "heating\theating\th\the\thea\theat\tg\tng\ting\tting\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "it\tit\ti\tit\tit\tit\tt\tit\tit\tit\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "up\tup\tu\tup\tup\tup\tp\tup\tup\tup\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "900\t900\t9\t90\t900\t900\t0\t00\t900\t900\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueMost>\n" +
                "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\tI-<unitLeft>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<unitLeft>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                "min\tmin\tm\tmi\tmin\tmin\tn\tin\tmin\tmin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                "followed\tfollowed\tf\tfo\tfol\tfoll\td\ted\twed\towed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "by\tby\tb\tby\tby\tby\ty\tby\tby\tby\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "gentle\tgentle\tg\tge\tgen\tgent\te\tle\ttle\tntle\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "bombardment\tbombardment\tb\tbo\tbom\tbomb\tt\tnt\tent\tment\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "Ar\tar\tA\tAr\tAr\tAr\tr\tAr\tAr\tAr\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "+\t+\t+\t+\t+\t+\t+\t+\t+\t+\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t+\t+\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "600\t600\t6\t60\t600\t600\t0\t00\t600\t600\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueAtomic>\n" +
                "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\tI-<unitLeft>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<unitLeft>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "5\t5\t5\t5\t5\t5\t5\t5\t5\t5\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\tI-<valueLeast>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                "min\tmin\tm\tmi\tmin\tmin\tn\tin\tmin\tmin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                "using\tusing\tu\tus\tusi\tusin\tg\tng\ting\tsing\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "a\ta\ta\ta\ta\ta\ta\ta\ta\ta\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "Kaufman\tkaufman\tK\tKa\tKau\tKauf\tn\tan\tman\tfman\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "ion\tion\ti\tio\tion\tion\tn\ton\tion\tion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "source\tsource\ts\tso\tsou\tsour\te\tce\trce\turce\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "45\t45\t4\t45\t45\t45\t5\t45\t45\t45\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\tI-<unitLeft>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "substrate\tsubstrate\ts\tsu\tsub\tsubs\te\tte\tate\trate\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "surface\tsurface\ts\tsu\tsur\tsurf\te\tce\tace\tface\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "Then\tthen\tT\tTh\tThe\tThen\tn\ten\then\tThen\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "temperature\ttemperature\tt\tte\ttem\ttemp\te\tre\ture\tture\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "raised\traised\tr\tra\trai\trais\td\ted\tsed\tised\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "back\tback\tb\tba\tbac\tback\tk\tck\tack\tback\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "900\t900\t9\t90\t900\t900\t0\t00\t900\t900\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueMost>\n" +
                "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\tI-<unitLeft>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<unitLeft>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "kept\tkept\tk\tke\tkep\tkept\tt\tpt\tept\tkept\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "that\tthat\tt\tth\ttha\tthat\tt\tat\that\tthat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "value\tvalue\tv\tva\tval\tvalu\te\tue\tlue\talue\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "30\t30\t3\t30\t30\t30\t0\t30\t30\t30\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                "min\tmin\tm\tmi\tmin\tmin\tn\tin\tmin\tmin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "Afterwards\tafterwards\tA\tAf\tAft\tAfte\ts\tds\trds\tards\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "temperature\ttemperature\tt\tte\ttem\ttemp\te\tre\ture\tture\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "reduced\treduced\tr\tre\tred\tredu\td\ted\tced\tuced\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "600\t600\t6\t60\t600\t600\t0\t00\t600\t600\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueAtomic>\n" +
                "•\t•\t•\t•\t•\t•\t•\t•\t•\t•\tALLCAPS\tNODIGIT\t1\tNOPUNCT\t•\t•\t0\t0\tI-<unitLeft>\n" +
                "C\tc\tC\tC\tC\tC\tC\tC\tC\tC\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<unitLeft>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "simultaneous\tsimultaneous\ts\tsi\tsim\tsimu\ts\tus\tous\teous\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "deposition\tdeposition\td\tde\tdep\tdepo\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "took\ttook\tt\tto\ttoo\ttook\tk\tok\took\ttook\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "place\tplace\tp\tpl\tpla\tplac\te\tce\tace\tlace\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "10\t10\t1\t10\t10\t10\t0\t10\t10\t10\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueAtomic>\n" +
                "min\tmin\tm\tmi\tmin\tmin\tn\tin\tmin\tmin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "pressure\tpressure\tp\tpr\tpre\tpres\te\tre\ture\tsure\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "3\t3\t3\t3\t3\t3\t3\t3\t3\t3\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "4\t4\t4\t4\t4\t4\t4\t4\t4\t4\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
                "mTorr\tmtorr\tm\tmT\tmTo\tmTor\tr\trr\torr\tTorr\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXxxx\txXx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "increase\tincrease\ti\tin\tinc\tincr\te\tse\tase\tease\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "4\t4\t4\t4\t4\t4\t4\t4\t4\t4\tNOCAPS\tALLDIGIT\t1\tNOPUNCT\td\td\t0\t0\t<other>\n" +
                "mTorr\tmtorr\tm\tmT\tmTo\tmTor\tr\trr\torr\tTorr\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txXxxx\txXx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "sometimes\tsometimes\ts\tso\tsom\tsome\ts\tes\tmes\times\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "required\trequired\tr\tre\treq\trequ\td\ted\tred\tired\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "plasma\tplasma\tp\tpl\tpla\tplas\ta\tma\tsma\tasma\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "stability\tstability\ts\tst\tsta\tstab\ty\tty\tity\tlity\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ";\t;\t;\t;\t;\t;\t;\t;\t;\t;\tALLCAPS\tNODIGIT\t1\tPUNCT\t;\t;\t0\t0\t<other>\n" +
                "such\tsuch\ts\tsu\tsuc\tsuch\th\tch\tuch\tsuch\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "action\taction\ta\tac\tact\tacti\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "did\tdid\td\tdi\tdid\tdid\td\tid\tdid\tdid\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "not\tnot\tn\tno\tnot\tnot\tt\tot\tnot\tnot\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "change\tchange\tc\tch\tcha\tchan\te\tge\tnge\tange\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "stoichiometry\tstoichiometry\ts\tst\tsto\tstoi\ty\try\ttry\tetry\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "just\tjust\tj\tju\tjus\tjust\tt\tst\tust\tjust\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "non\tnon\tn\tno\tnon\tnon\tn\ton\tnon\tnon\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "essentially\tessentially\te\tes\tess\tesse\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "reduced\treduced\tr\tre\tred\tredu\td\ted\tced\tuced\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "deposition\tdeposition\td\tde\tdep\tdepo\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "speed\tspeed\ts\tsp\tspe\tspee\td\ted\teed\tpeed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ")\t)\t)\t)\t)\t)\t)\t)\t)\t)\tALLCAPS\tNODIGIT\t1\tENDBRACKET\t)\t)\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "gun\tgun\tg\tgu\tgun\tgun\tn\tun\tgun\tgun\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "power\tpower\tp\tpo\tpow\tpowe\tr\ter\twer\tower\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
                "250\t250\t2\t25\t250\t250\t0\t50\t250\t250\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueLeast>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "260\t260\t2\t26\t260\t260\t0\t60\t260\t260\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueMost>\n" +
                "W\tw\tW\tW\tW\tW\tW\tW\tW\tW\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\tI-<unitLeft>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "anode\tanode\ta\tan\tano\tanod\te\tde\tode\tnode\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "voltage\tvoltage\tv\tvo\tvol\tvolt\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "460\t460\t4\t46\t460\t460\t0\t60\t460\t460\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueLeast>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "605\t605\t6\t60\t605\t605\t5\t05\t605\t605\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\t<other>\n" +
                "V\tv\tV\tV\tV\tV\tV\tV\tV\tV\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "Re\tre\tR\tRe\tRe\tRe\te\tRe\tRe\tRe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "with\twith\tw\twi\twit\twith\th\tth\tith\twith\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "gun\tgun\tg\tgu\tgun\tgun\tn\tun\tgun\tgun\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "power\tpower\tp\tpo\tpow\tpowe\tr\ter\twer\tower\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
                "45\t45\t4\t45\t45\t45\t5\t45\t45\t45\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "90\t90\t9\t90\t90\t90\t0\t90\t90\t90\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\t<other>\n" +
                "W\tw\tW\tW\tW\tW\tW\tW\tW\tW\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "anode\tanode\ta\tan\tano\tanod\te\tde\tode\tnode\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "voltage\tvoltage\tv\tvo\tvol\tvolt\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "correspondingly\tcorrespondingly\tc\tco\tcor\tcorr\ty\tly\tgly\tngly\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "275\t275\t2\t27\t275\t275\t5\t75\t275\t275\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueLeast>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "325\t325\t3\t32\t325\t325\t5\t25\t325\t325\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tddd\td\t0\t0\tI-<valueMost>\n" +
                "V\tv\tV\tV\tV\tV\tV\tV\tV\tV\tALLCAPS\tNODIGIT\t1\tNOPUNCT\tX\tX\t1\t0\tI-<unitLeft>\n" +
                "for\tfor\tf\tfo\tfor\tfor\tr\tor\tfor\tfor\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>\n" +
                "Keeping\tkeeping\tK\tKe\tKee\tKeep\tg\tng\ting\tping\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "Re\tre\tR\tRe\tRe\tRe\te\tRe\tRe\tRe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "gun\tgun\tg\tgu\tgun\tgun\tn\tun\tgun\tgun\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "power\tpower\tp\tpo\tpow\tpowe\tr\ter\twer\tower\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
                "constant\tconstant\tc\tco\tcon\tcons\tt\tnt\tant\ttant\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ",\t,\t,\t,\t,\t,\t,\t,\t,\t,\tALLCAPS\tNODIGIT\t1\tCOMMA\t,\t,\t0\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "varying\tvarying\tv\tva\tvar\tvary\tg\tng\ting\tying\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "sputtering\tsputtering\ts\tsp\tspu\tsput\tg\tng\ting\tring\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "power\tpower\tp\tpo\tpow\tpowe\tr\ter\twer\tower\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t1\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "from\tfrom\tf\tfr\tfro\tfrom\tm\tom\trom\tfrom\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "case\tcase\tc\tca\tcas\tcase\te\tse\tase\tcase\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "case\tcase\tc\tca\tcas\tcase\te\tse\tase\tcase\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "allowed\tallowed\ta\tal\tall\tallo\td\ted\twed\towed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "us\tus\tu\tus\tus\tus\ts\tus\tus\tus\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "to\tto\tt\tto\tto\tto\to\tto\tto\tto\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t0\t0\t<other>\n" +
                "vary\tvary\tv\tva\tvar\tvary\ty\try\tary\tvary\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "values\tvalues\tv\tva\tval\tvalu\ts\tes\tues\tlues\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "x\tx\tx\tx\tx\tx\tx\tx\tx\tx\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "in\tin\ti\tin\tin\tin\tn\tin\tin\tin\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "composi\tcomposi\tc\tco\tcom\tcomp\ti\tsi\tosi\tposi\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "-\t-\t-\t-\t-\t-\t-\t-\t-\t-\tALLCAPS\tNODIGIT\t1\tHYPHEN\t-\t-\t0\t0\t<other>\n" +
                "tion\ttion\tt\tti\ttio\ttion\tn\ton\tion\ttion\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "Re\tre\tR\tRe\tRe\tRe\te\tRe\tRe\tRe\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "x\tx\tx\tx\tx\tx\tx\tx\tx\tx\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t0\t0\t<other>\n" +
                "Lu\tlu\tL\tLu\tLu\tLu\tu\tLu\tLu\tLu\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXx\tXx\t0\t0\t<other>\n" +
                "(\t(\t(\t(\t(\t(\t(\t(\t(\t(\tALLCAPS\tNODIGIT\t1\tOPENBRACKET\t(\t(\t0\t0\t<other>\n" +
                "see\tsee\ts\tse\tsee\tsee\te\tee\tsee\tsee\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "table\ttable\tt\tta\ttab\ttabl\te\tle\tble\table\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>"

        val sentences: MutableList<OffsetPosition> = ArrayList()
        sentences.add(OffsetPosition(0, 169))
        sentences.add(OffsetPosition(170, 261))
        sentences.add(OffsetPosition(262, 358))
        sentences.add(OffsetPosition(359, 513))
        sentences.add(OffsetPosition(514, 675))
        sentences.add(OffsetPosition(676, 816))
        sentences.add(OffsetPosition(817, 1053))
        sentences.add(OffsetPosition(1054, 1137))
        sentences.add(OffsetPosition(1138, 1579))
        sentences.add(OffsetPosition(1580, 1744))

        EasyMock.expect(mockValueParser.parseValue(EasyMock.anyObject())).andReturn(null).anyTimes()

        EasyMock.replay(mockValueParser)
        val measurements = target!!.extractMeasurement(tokens, result, sentences)

        assertThat(measurements, IsCollectionWithSize.hasSize(17))

        assertThat(measurements[8].type, `is`(UnitUtilities.Measurement_Type.INTERVAL_MIN_MAX))
        assertThat(measurements[8].quantityLeast.rawValue, `is`("5"))
        assertThat(measurements[8].quantityMost.rawValue, `is`("10"))
        assertThat(measurements[8].quantityMost.rawUnit.rawName, `is`("min"))

    }

    @Test
    fun testGetSentence_entityInsideSentence() {

        val sentences: List<OffsetPosition> = listOf(
            OffsetPosition(0, 3),
            OffsetPosition(4, 6),
            OffsetPosition(7, 10)
        )

        val entityPosition = OffsetPosition(1, 2)

        val foundSentence = target.findSentenceOffset(sentences, entityPosition)

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

        val foundSentence = target.findSentenceOffset(sentences, entityPosition)

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

        val foundSentence = target.findSentenceOffset(sentences, entityPosition)

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