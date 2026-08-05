package org.grobid.core.features;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class FeaturesVectorQuantitiesTest {

    FeatureFactory featureFactoryMock;

    @Before
    public void setUp() throws Exception {
        featureFactoryMock = mock(FeatureFactory.class);
        featureFactoryMock.isPunct = Pattern.compile("^[\\,\\:;\\?\\.]+$");
    }

    @Test
    public void testPrintVector1() throws Exception {
        String word = "Colorado";
        String label = "CITY";

        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital(word)).thenReturn(false);
            when(featureFactoryMock.test_first_capital(word)).thenReturn(true);
            when(featureFactoryMock.test_number(word)).thenReturn(false);
            when(featureFactoryMock.test_digit(word)).thenReturn(false);

            FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

            assertThat(target.printVector(), is("Colorado colorado C Co Col Colo o do ado rado INITCAP NODIGIT 0 NOPUNCT Xxxx Xx 1 0 CITY"));
        }
    }

    @Test
    public void testPrintVector2() throws Exception {
        String word = "The";
        String label = "OTHER";

        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital(word)).thenReturn(false);
            when(featureFactoryMock.test_first_capital(word)).thenReturn(true);
            when(featureFactoryMock.test_number(word)).thenReturn(false);
            when(featureFactoryMock.test_digit(word)).thenReturn(false);

            FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

            assertThat(target.printVector(), is("The the T Th The The e he The The INITCAP NODIGIT 0 NOPUNCT Xxx Xx 1 0 OTHER"));
        }
    }

    @Test
    public void testPrintVector3() throws Exception {
        String word = "a";
        String label = "OTHER";

        try (MockedStatic<FeatureFactory> ff = mockStatic(FeatureFactory.class)) {
            ff.when(FeatureFactory::getInstance).thenReturn(featureFactoryMock);
            when(featureFactoryMock.test_all_capital(word)).thenReturn(false);
            when(featureFactoryMock.test_first_capital(word)).thenReturn(false);
            when(featureFactoryMock.test_number(word)).thenReturn(false);
            when(featureFactoryMock.test_digit(word)).thenReturn(false);

            FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

            assertThat(target.printVector(), is("a a a a a a a a a a NOCAPS NODIGIT 1 NOPUNCT x x 1 0 OTHER"));
        }
    }
}
