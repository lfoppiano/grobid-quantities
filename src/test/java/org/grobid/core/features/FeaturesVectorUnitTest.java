package org.grobid.core.features;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class FeaturesVectorUnitTest {

    FeatureFactory featureFactoryMock;

    @Before
    public void setUp() throws Exception {
        featureFactoryMock = mock(FeatureFactory.class);
    }

    @Test
    public void testPrintVector_sample1() throws Exception {
        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital("c")).thenReturn(false);
            when(featureFactoryMock.test_number("c")).thenReturn(false);

            FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("c", "LABEL", false, true, false);

            String outputString = output.printVector();
            assertThat(outputString, is("c 0 0 0 1 NOPUNCT 0 LABEL"));
        }
    }

    @Test
    public void testPrintVector_sample2() throws Exception {
        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital("2")).thenReturn(true);
            when(featureFactoryMock.test_number("2")).thenReturn(true);

            FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("2", "LABEL", false, true, true);

            String outputString = output.printVector();
            assertThat(outputString, is("2 1 1 0 1 NOPUNCT 1 LABEL"));
        }
    }

    @Test
    public void testPrintVector_sample3() throws Exception {
        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital("/")).thenReturn(false);
            when(featureFactoryMock.test_number("/")).thenReturn(false);

            FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("/", "LABEL", false, true, true);

            String outputString = output.printVector();
            assertThat(outputString, is("/ 0 0 0 1 SLASH 1 LABEL"));
        }
    }

    @Test
    public void testAddFeaturesUnit() throws Exception {
        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital("c")).thenReturn(false);
            when(featureFactoryMock.test_number("c")).thenReturn(false);

            FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("c", null, false, true, false);

            assertNotNull(output.isDigit);
            assertNotNull(output.isKnownUnitToken);
            assertNotNull(output.isUpperCase);
            assertNotNull(output.punctType);
            assertNotNull(output.hasRightAttachment);
            assertNull(output.label);
        }
    }

    @Test
    public void testAddFeaturesUnit_prefix1() throws Exception {
        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital("G")).thenReturn(true);
            when(featureFactoryMock.test_number("G")).thenReturn(false);

            FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("G", null, false, true, true);

            assertNotNull(output.isDigit);
            assertNotNull(output.isKnownUnitToken);
            assertNotNull(output.isUpperCase);
            assertNotNull(output.punctType);
            assertTrue(output.hasRightAttachment);
            assertNull(output.label);
        }
    }
}
