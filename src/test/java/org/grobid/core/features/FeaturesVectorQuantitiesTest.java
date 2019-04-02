package org.grobid.core.features;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.regex.Pattern;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FeatureFactory.class)
public class FeaturesVectorQuantitiesTest {

    FeatureFactory featureFactoryMock;

    @Before
    public void setUp() throws Exception {
        featureFactoryMock = PowerMock.createMock(FeatureFactory.class);
        featureFactoryMock.isPunct = Pattern.compile("^[\\,\\:;\\?\\.]+$");
    }

    @Test
    public void testPrintVector1() throws Exception {
        String word = "Colorado";
        String label = "CITY";

        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital(word)).andReturn(false);
        expect(featureFactoryMock.test_first_capital(word)).andReturn(true);
        expect(featureFactoryMock.test_number(word)).andReturn(false);
        expect(FeatureFactory.test_digit(word)).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

        verify(FeatureFactory.class, featureFactoryMock);
        assertThat(target.printVector(), is("Colorado colorado C Co Col Colo o do ado rado INITCAP NODIGIT 0 NOPUNCT Xxxx Xx 1 0 CITY"));
    }

    @Test
    public void testPrintVector2() throws Exception {
        String word = "The";
        String label = "OTHER";

        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital(word)).andReturn(false);
        expect(featureFactoryMock.test_first_capital(word)).andReturn(true);
        expect(featureFactoryMock.test_number(word)).andReturn(false);
        expect(FeatureFactory.test_digit(word)).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

        verify(FeatureFactory.class, featureFactoryMock);
        assertThat(target.printVector(), is("The the T Th The The e he The The INITCAP NODIGIT 0 NOPUNCT Xxx Xx 1 0 OTHER"));
    }

    @Test
    public void testPrintVector3() throws Exception {
        String word = "a";
        String label = "OTHER";

        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital(word)).andReturn(false);
        expect(featureFactoryMock.test_first_capital(word)).andReturn(false);
        expect(featureFactoryMock.test_number(word)).andReturn(false);
        expect(FeatureFactory.test_digit(word)).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorQuantities target = FeaturesVectorQuantities.addFeaturesQuantities(word, label, true, true, false);

        verify(FeatureFactory.class, featureFactoryMock);
        assertThat(target.printVector(), is("a a a a a a a a a a NOCAPS NODIGIT 1 NOPUNCT x x 1 0 OTHER"));
    }
}