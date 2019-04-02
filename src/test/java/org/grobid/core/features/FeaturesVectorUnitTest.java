package org.grobid.core.features;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FeatureFactory.class)
public class FeaturesVectorUnitTest {

    FeatureFactory featureFactoryMock;

    @Before
    public void setUp() throws Exception {
        featureFactoryMock = PowerMock.createMock(FeatureFactory.class);
    }

    @Test
    public void testPrintVector_sample1() throws Exception {

        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital("c")).andReturn(false);
        expect(featureFactoryMock.test_number("c")).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("c", "LABEL", false, true, false);
        verify(FeatureFactory.class, featureFactoryMock);

        String outputString = output.printVector();
        assertThat(outputString, is("c 0 0 0 1 NOPUNCT 0 LABEL"));
    }

    @Test
    public void testPrintVector_sample2() throws Exception {
        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital("2")).andReturn(true);
        expect(featureFactoryMock.test_number("2")).andReturn(true);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("2", "LABEL", false, true, true);
        verify(FeatureFactory.class, featureFactoryMock);

        String outputString = output.printVector();
        assertThat(outputString, is("2 1 1 0 1 NOPUNCT 1 LABEL"));
    }

    @Test
    public void testAddFeaturesUnit() throws Exception {
        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital("c")).andReturn(false);
        expect(featureFactoryMock.test_number("c")).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("c", null, false, true, false);
        verify(FeatureFactory.class, featureFactoryMock);

        assertNotNull(output.isDigit);
        assertNotNull(output.isKnownUnitToken);
        assertNotNull(output.isUpperCase);
        assertNotNull(output.punctType);
        assertNotNull(output.hasRightAttachment);
        assertNull(output.label);
    }

    @Test
    public void testAddFeaturesUnit_prefix1() throws Exception {
        mockStatic(FeatureFactory.class);
        expect(FeatureFactory.getInstance()).andReturn(featureFactoryMock);
        expect(featureFactoryMock.test_all_capital("G")).andReturn(true);
        expect(featureFactoryMock.test_number("G")).andReturn(false);
        replay(FeatureFactory.class, featureFactoryMock);

        FeaturesVectorUnits output = FeaturesVectorUnits.addFeaturesUnit("G", null, false, true, true);
        verify(FeatureFactory.class, featureFactoryMock);

        assertNotNull(output.isDigit);
        assertNotNull(output.isKnownUnitToken);
        assertNotNull(output.isUpperCase);
        assertNotNull(output.punctType);
        assertTrue(output.hasRightAttachment);
        assertNull(output.label);
    }
}