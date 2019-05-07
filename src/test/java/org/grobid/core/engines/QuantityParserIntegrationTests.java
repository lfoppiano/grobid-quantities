package org.grobid.core.engines;

import org.apache.commons.io.IOUtils;
import org.grobid.core.analyzers.QuantityAnalyzer;
import org.grobid.core.data.Measurement;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.OffsetPosition;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 04.03.16.
 */
public class QuantityParserIntegrationTests {
    QuantityParser target;

    @BeforeClass
    public static void init() {
        LibraryLoader.load();
    }


    @Before
    public void setUp() {
        target = QuantityParser.getInstance(true);
    }

    @Test
    public void testNotNormalizedQuantity() throws Exception {
        List<Measurement> measurements = target.process("10 meters");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableQuantity() throws Exception {
        List<Measurement> measurements = target.process("10 km");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableWordsQuantity() throws Exception {
        List<Measurement> measurements = target.process("ten km");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testNormalizeableQuantity2() throws Exception {
        List<Measurement> measurements = target.process("10 m^1*s^-1");


        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
                System.out.println(measurement.toJson());
            }
        } else {
            System.out.println("No measurement found.");
        }

    }

    @Test
    public void testQuantityParser1() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test1.txt"), UTF_8);
        System.out.println("\ntest1.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser2() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test2.txt"), UTF_8);
        System.out.println("\ntest2.txt\n" + text + "\n");

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }

    @Test
    public void testQuantityParser3() throws Exception {

        String text = IOUtils.toString(
                this.getClass().getResourceAsStream("/test0.training.txt"), UTF_8);

        QuantityParser parser = QuantityParser.getInstance();

        List<Measurement> measurements = parser.process(text);
        if (measurements != null) {
            System.out.println("\n");
            for (Measurement measurement : measurements) {
                System.out.println(measurement.toString());
            }
        } else {
            System.out.println("No measurement found.");
        }
    }


    @Test
    public void testQuantityParser4() throws Exception {
//        String text = "\n\nFirst, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden.";
        String text = "A 20kg ingot is made in a high frequency induction melting furnace and forged to 30mm in thickness and " +
                "90mm in width at 850 to 1,150°C. Specimens No.2 to 4, 6 and 15 are materials embodying the invention. " +
                "Others are for comparison. No.1 is a material equivalent to ASTM standard A469-88 class 8 for generator rotor " +
                "shaft material. No. 5 is a material containing relatively high Al content. \n" +
                "\n" +
                "These specimens underwent heat treatment by simulating the conditions for the large size rotor shaft centre " +
                "of a large capacity generator. " +
                "First, it was heated to 840°C to form austenite structure and cooled at the speed of 100°C/hour to harden. " +
                "Then, the specimen was heated and held at 575 to 590°C for 32 hours and cooled at a speed of 15°C/hour. Tempering " +
                "was done at such a temperature to secure tensile strength in the range of 100 to 105kg/mm2 for each specimen.";
        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(10));
        assertThat(measurements.get(4).getQuantityAtomic().getRawValue(), is("840"));
        assertThat(measurements.get(5).getQuantityAtomic().getRawValue(), is("100"));

        int offsetStart0 = measurements.get(4).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(4).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("840"));

        int offsetStart1 = measurements.get(5).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(5).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("100"));
    }

    @Test
    public void testQuantityParser5() throws Exception {
        String text = "First, it was heated to 840°C to form austenite structure and cooled \n\nat the speed of 100°C/hour to harden.";

        List<Measurement> measurements = target.process(text);

        assertThat(measurements, hasSize(2));
        assertThat(measurements.get(0).getQuantityAtomic().getRawValue(), is("840"));
        assertThat(measurements.get(1).getQuantityAtomic().getRawValue(), is("100"));

        int offsetStart0 = measurements.get(0).getQuantityAtomic().getOffsetStart();
        int offsetEnd0 = measurements.get(0).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart0, offsetEnd0), is("840"));

        int offsetStart1 = measurements.get(1).getQuantityAtomic().getOffsetStart();
        int offsetEnd1 = measurements.get(1).getQuantityAtomic().getOffsetEnd();

        assertThat(text.substring(offsetStart1, offsetEnd1), is("100"));
    }


//    @Test
//    public void test() throws Exception  {
//        String text = "Before the 1920s the number of stages was usually 15 at most and the riders enjoyed at least one day of rest after each stage.";
//
//        target.process(text);
//
//    }

    @Test
    @Ignore("Failing test, we should fix the issue ;-) ")
    public void test1() throws Exception {

        String text = "Before the 1920s the number of stages was usually 15 at most and the riders enjoyed at least one day of rest after each stage.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Before\tbefore\tB\tBe\tBef\tBefo\te\tre\tore\tfore\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "1920\t1920\t1\t19\t192\t1920\t0\t20\t920\t1920\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdddd\td\t0\t0\tI-<valueMost>\n" +
                "s\ts\ts\ts\ts\ts\ts\ts\ts\ts\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<valueMost>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "number\tnumber\tn\tnu\tnum\tnumb\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "stages\tstages\ts\tst\tsta\tstag\ts\tes\tges\tages\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "usually\tusually\tu\tus\tusu\tusua\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "most\tmost\tm\tmo\tmos\tmost\tt\tst\tost\tmost\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "and\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "riders\triders\tr\tri\trid\tride\ts\trs\ters\tders\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "enjoyed\tenjoyed\te\ten\tenj\tenjo\td\ted\tyed\toyed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "least\tleast\tl\tle\tlea\tleas\tt\tst\tast\teast\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "one\tone\to\ton\tone\tone\te\tne\tone\tone\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t1\tI-<valueLeast>\n" +
                "day\tday\td\tda\tday\tday\ty\tay\tday\tday\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "rest\trest\tr\tre\tres\trest\tt\tst\test\trest\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "after\tafter\ta\taf\taft\tafte\tr\ter\tter\tfter\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "each\teach\te\tea\teac\teach\th\tch\tach\teach\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "stage\tstage\ts\tst\tsta\tstag\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>";

        List<Measurement> measurementList = target.extractMeasurement(tokens, result);
//        List<Measurement> measurementList = target.extractMeasurement(tokens, result, target.getSentencesOffsets(tokens));

        assertThat(measurementList, hasSize(3));
    }

    @Test
    @Ignore("To be plugged in when we re-enable the sentence parser.")
    public void testReconstructionWithSentenceTokenizer() throws Exception {

        String text = "Before the 1920s the number of stages was usually 15 at most. The riders enjoyed at least one day of rest after each stage.";

        List<LayoutToken> tokens = QuantityAnalyzer.getInstance().tokenizeWithLayoutToken(text);

        String result = "Before\tbefore\tB\tBe\tBef\tBefo\te\tre\tore\tfore\tINITCAP\tNODIGIT\t0\tNOPUNCT\tXxxx\tXx\t0\t0\t<other>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "1920\t1920\t1\t19\t192\t1920\t0\t20\t920\t1920\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdddd\td\t0\t0\tI-<valueMost>\n" +
                "s\ts\ts\ts\ts\ts\ts\ts\ts\ts\tNOCAPS\tNODIGIT\t1\tNOPUNCT\tx\tx\t1\t0\t<valueMost>\n" +
                "the\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "number\tnumber\tn\tnu\tnum\tnumb\tr\ter\tber\tmber\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "stages\tstages\ts\tst\tsta\tstag\ts\tes\tges\tages\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "was\twas\tw\twa\twas\twas\ts\tas\twas\twas\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "usually\tusually\tu\tus\tusu\tusua\ty\tly\tlly\tally\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "15\t15\t1\t15\t15\t15\t5\t15\t15\t15\tNOCAPS\tALLDIGIT\t0\tNOPUNCT\tdd\td\t0\t0\tI-<valueMost>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "most\tmost\tm\tmo\tmos\tmost\tt\tst\tost\tmost\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\tand\ta\tan\tand\tand\td\tnd\tand\tand\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "The\tthe\tt\tth\tthe\tthe\te\the\tthe\tthe\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t0\t<other>\n" +
                "riders\triders\tr\tri\trid\tride\ts\trs\ters\tders\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "enjoyed\tenjoyed\te\ten\tenj\tenjo\td\ted\tyed\toyed\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "at\tat\ta\tat\tat\tat\tt\tat\tat\tat\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "least\tleast\tl\tle\tlea\tleas\tt\tst\tast\teast\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "one\tone\to\ton\tone\tone\te\tne\tone\tone\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t0\t1\tI-<valueLeast>\n" +
                "day\tday\td\tda\tday\tday\ty\tay\tday\tday\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxx\tx\t1\t0\tI-<unitLeft>\n" +
                "of\tof\to\tof\tof\tof\tf\tof\tof\tof\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txx\tx\t1\t0\t<other>\n" +
                "rest\trest\tr\tre\tres\trest\tt\tst\test\trest\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "after\tafter\ta\taf\taft\tafte\tr\ter\tter\tfter\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "each\teach\te\tea\teac\teach\th\tch\tach\teach\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                "stage\tstage\ts\tst\tsta\tstag\te\tge\tage\ttage\tNOCAPS\tNODIGIT\t0\tNOPUNCT\txxxx\tx\t0\t0\t<other>\n" +
                ".\t.\t.\t.\t.\t.\t.\t.\t.\t.\tALLCAPS\tNODIGIT\t1\tDOT\t.\t.\t0\t0\t<other>";

        List<OffsetPosition> sentences = Arrays.asList(new OffsetPosition(0, 61), new OffsetPosition(61, 123));
//        List<Measurement> measurementList = target.extractMeasurement(tokens, result, sentences);
        List<Measurement> measurementList = target.extractMeasurement(tokens, result);

        assertThat(measurementList, hasSize(3));

        assertThat(measurementList.get(0).getQuantityMost().getRawValue(), is("1920s"));
        assertThat(measurementList.get(1).getQuantityMost().getRawValue(), is("15"));
        assertThat(measurementList.get(2).getQuantityLeast().getRawValue(), is("one"));
    }
}