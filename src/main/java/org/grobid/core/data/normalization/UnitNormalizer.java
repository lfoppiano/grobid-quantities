package org.grobid.core.data.normalization;

import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.UnitParser;
import org.grobid.core.lexicon.QuantityLexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by lfoppiano on 23/03/16.
 */
public class UnitNormalizer {

    private UnitParser unitParser;
    private QuantityLexicon quantityLexicon;

    public UnitNormalizer() {
        unitParser = UnitParser.getInstance();
        quantityLexicon = QuantityLexicon.getInstance();
    }


    public List<UnitBlock> parseToProduct(String rawUnit, boolean isUnitLeft) {
        String unitName = quantityLexicon.getNameByInflection(rawUnit);

        List<UnitBlock> unitBlockList = new ArrayList<>();
        if (unitName == null) {
            unitBlockList = unitParser.tagUnit(rawUnit, isUnitLeft);
        } else {
            unitBlockList.add(new UnitBlock(null, unitName, null));
        }

        return unitBlockList;
    }

    /**
     * Unit parsing:
     * - infer the name (name + decomposition) from the written
     * form (including inflections), e.g. m <- meters, A/V <- volt per meter
     * - if not found, parse the unit using the Unit CRF model
     */
    public Unit parseUnit(Unit rawUnit) throws NormalizationException {
        List<UnitBlock> blocks = parseToProduct(rawUnit.getRawName(), rawUnit.hasUnitRightAttachment());

        Unit parsedUnit = new Unit();
        parsedUnit.setOffsetStart(rawUnit.getOffsetStart());
        parsedUnit.setOffsetEnd(rawUnit.getOffsetEnd());
        parsedUnit.setProductBlocks(blocks);
        final List<UnitBlock> decomposedBlocks = decomposeBlocks(blocks);
        final String reformatted = UnitBlock.asString(decomposedBlocks);
        parsedUnit.setRawName(reformatted);
        parsedUnit.setUnitRightAttachment(rawUnit.hasUnitRightAttachment());

        UnitDefinition def = quantityLexicon.getUnitByNotation(reformatted);
        if (def == null) {
            def = quantityLexicon.getUnitbyName(reformatted);
        }
        parsedUnit.setUnitDefinition(def);
        return parsedUnit;
    }

    /**
     * Try to find the notation of each blocks
     */
    public List<UnitBlock> decomposeBlocks(List<UnitBlock> blocks) {
        // Try to lookup the single element in the blocks

        return blocks.stream().map(b -> {
            if (isNotEmpty(b.getBase()) && isEmpty(b.getPrefix())) {
                String newName = quantityLexicon.getNameByInflection(b.getBase());

                if (isNotEmpty(newName)) {
                    return new UnitBlock(b.getPrefix(), newName, b.getPow());
                }
            }
            return b;
        }).collect(Collectors.toList());

    }

    public UnitDefinition findDefinition(Unit unit) {
        return quantityLexicon.lookup(unit);
    }

    public Unit findDefinitionAndUpdate(Unit unit) {
        UnitDefinition definition = findDefinition(unit);

        if (definition != null) {
            unit.setUnitDefinition(definition);
        }
        return unit;
    }


    public void setUnitParser(UnitParser unitParser) {
        this.unitParser = unitParser;
    }

    public void setQuantityLexicon(QuantityLexicon quantityLexicon) {
        this.quantityLexicon = quantityLexicon;
    }
}
