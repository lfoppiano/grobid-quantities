package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Unit;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorUnit;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.tokenization.LabeledTokensContainer;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.MeasurementUtilities;
import org.grobid.core.utilities.OffsetPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by lfoppiano on 20.02.16.
 */
public class UnitParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnitParser.class);

    private static volatile UnitParser instance;

    public static UnitParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new UnitParser();
    }

    private QuantityLexicon quantityLexicon = null;
    private MeasurementUtilities measurementUtilities = null;

    private UnitParser() {
        super(GrobidModels.UNITS);
        quantityLexicon = QuantityLexicon.getInstance();
        measurementUtilities = new MeasurementUtilities();
    }

    /**
     * Extract all occurences of measurement/quantities from a simple piece of text.
     */
    public List<Unit.UnitBlock> tagUnit(String text) throws Exception {
        if (isBlank(text)) {
            return null;
        }
        List<Unit.UnitBlock> units = new ArrayList<>();

        try {
            text = text.replace("\n", " ");
            List<LayoutToken> tokenizations = new ArrayList<>();

            String ress = null;
            List<String> characters = new ArrayList<>();
            List<OffsetPosition> unitTokenPositions = new ArrayList<>();
            for (char character : text.toCharArray()) {
                characters.add("" + character);
                OffsetPosition position = new OffsetPosition();
                position.start = text.indexOf(character);
                position.end = text.indexOf(character) + 1;
                LayoutToken lt = new LayoutToken("" + character);
                tokenizations.add(lt);

                unitTokenPositions.add(position);
                //unitTokenPositions.add(new OffsetPosition(text.indexOf(character), text.indexOf(character) + 1));
            }

            ress = addFeatures(characters, unitTokenPositions);
            String res;
            try {
                res = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for quantity parsing failed.", e);
            }
            units = resultExtraction(text, res, tokenizations);
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }

        return units;
    }

    /**
     * Extract identified quantities from a labelled text.
     */
    public List<Unit.UnitBlock> resultExtraction(String text,
                                                 String result,
                                                 List<LayoutToken> tokenizations) {
        List<Unit> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.UNITS, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        Unit currentUnit = new Unit();
        int pos = 0; // position in term of characters for creating the offsets

        boolean denominator = false;
        int currentPow = 1;
        boolean startUnit = false;
        TaggingLabel previousTag = null;

        List<Unit.UnitBlock> units = new ArrayList<>();
        Unit.UnitBlock unitBlock = new Unit().new UnitBlock();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens());

            switch (clusterLabel) {
                case UNIT_VALUE_PREFIX:
                    if (!startUnit) {
                        startUnit = true;
                    } else {
                        units.add(unitBlock);
                        unitBlock = new Unit().new UnitBlock();
                    }
                    unitBlock.setPrefix(clusterContent);

                    break;

                case UNIT_VALUE_BASE:
                    if (!startUnit) {
                        startUnit = true;
                        unitBlock = new Unit().new UnitBlock();
                    } else {
                        if (!TaggingLabel.UNIT_VALUE_PREFIX.equals(previousTag)) {
                            units.add(unitBlock);
                            unitBlock = new Unit().new UnitBlock();
                        }
                    }
                    unitBlock.setBase(clusterContent);
                    System.out.print(clusterContent + "(B)");
                    break;

                case UNIT_VALUE_OTHER:
                    System.out.print(clusterContent + "(O)");
                    break;

                case UNIT_VALUE_POW:
                    if (clusterContent.equals("/")) {
                        denominator = true;
                    } else if (clusterContent.endsWith("/")) {
                        denominator = true;
                        unitBlock.setPow(clusterContent.replace("/", ""));
                    } else {
                        if (denominator == true) {
                            String pow = "-" + clusterContent;

                            unitBlock.setPow(pow.replace("/", ""));
                        } else {
                            unitBlock.setPow(clusterContent);
                        }
                    }
                    System.out.print(clusterContent + "(P)");
                    break;

            }
            previousTag = clusterLabel;
        }
        units.add(unitBlock);
        System.out.println("]");

        return units;
    }


    @SuppressWarnings({"UnusedParameters"})
    private String addFeatures(List<String> characters,
                               List<OffsetPosition> unitTokenPositions) {
        int totalCharacters = characters.size();
        int posit = 0;
        int currentQuantityIndex = 0;
        List<OffsetPosition> localPositions = unitTokenPositions;

        StringBuilder result = new StringBuilder();
        try {
            for (String character : characters) {
                FeaturesVectorUnit featuresVector =
                        FeaturesVectorUnit.addFeaturesUnit(character, null, quantityLexicon.inUnitDictionary(character),
                                quantityLexicon.inPrefixDictionary(character));

                result.append(featuresVector.printVector());
                result.append("\n");
                posit++;
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }
}
