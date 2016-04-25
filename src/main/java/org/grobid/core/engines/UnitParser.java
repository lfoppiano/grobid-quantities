package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeaturesVectorUnit;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.lexicon.QuantityLexicon;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
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

    private UnitParser() {
        super(GrobidModels.UNITS);
        quantityLexicon = QuantityLexicon.getInstance();
    }

    /**
     * please use List<UnitBlock> tagUnit(String text, boolean hasUnitRightAttachment)
     */
    @Deprecated
    public List<UnitBlock> tagUnit(String text) {
        return tagUnit(text, false);
    }

    public List<UnitBlock> tagUnit(String text, boolean isUnitLeft) {
        if (isBlank(text)) {
            return null;
        }

        //Remove spaces. It's a workaround (to be check whether it is working) because spaces are causing troubles
        text = text.replaceAll(" ", "");
        List<UnitBlock> units = new ArrayList<>();

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

            ress = addFeatures(characters, unitTokenPositions, isUnitLeft);
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
    public List<UnitBlock> resultExtraction(String text,
                                            String result,
                                            List<LayoutToken> tokenizations) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.UNITS, result, tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        int pos = 0; // position in term of characters for creating the offsets

        boolean denominator = false;
        int currentPow = 1;
        boolean startUnit = false;
        TaggingLabel previousTag = null;

        List<UnitBlock> units = new ArrayList<>();
        UnitBlock unitBlock = new UnitBlock();

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
                        unitBlock = new UnitBlock();
                    }
                    unitBlock.setPrefix(clusterContent);

                    break;

                case UNIT_VALUE_BASE:
                    if (!startUnit) {
                        startUnit = true;
                        unitBlock = new UnitBlock();
                    } else {
                        if (!TaggingLabel.UNIT_VALUE_PREFIX.equals(previousTag)) {
                            units.add(unitBlock);
                            unitBlock = new UnitBlock();
                        }

                        if (denominator) {
                            unitBlock.setPow("-1");
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
                    } else if (clusterContent.equals("*")) {
                        //nothing to do
                    } else {
                        if (denominator == true) {
                            unitBlock.setPow("-" + clusterContent);
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
                               List<OffsetPosition> unitTokenPositions, boolean isUnitLeft) {

        StringBuilder result = new StringBuilder();
        try {
            for (String character : characters) {
                FeaturesVectorUnit featuresVector =
                        FeaturesVectorUnit.addFeaturesUnit(character, null, quantityLexicon.inUnitDictionary(character),
                                quantityLexicon.inPrefixDictionary(character), isUnitLeft);

                result.append(featuresVector.printVector())
                        .append("\n");
            }
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
        return result.toString();
    }
}
