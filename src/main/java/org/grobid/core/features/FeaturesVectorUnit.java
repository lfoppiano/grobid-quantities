package org.grobid.core.features;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Class representing the features for a unit recognition
 * <p>
 * Created by lfoppiano on 20.02.16.
 */
public class FeaturesVectorUnit {

    public String value;            // lexical feature
    public boolean isUpperCase = false;
    public boolean isDigit = false;
    public boolean isKnownUnitToken = false;
    public boolean isKnownPrefixToken = false;
    public boolean hasRightAttachment = false; // the unit has the quantity attached on the right (e.g. pH)
    public String label = null;             // label if known

    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, PUNCT (default)
    public String punctType = null;


    public String printVector() {
        if (isEmpty(value)) {
            return null;
        }
        StringBuffer res = new StringBuffer();

        res.append(value);

        if (isUpperCase) {
            res.append(" ").append(1);
        } else {
            res.append(" ").append(0);
        }

        if (isDigit) {
            res.append(" ").append(1);
        } else {
            res.append(" ").append(0);
        }

        if (isKnownUnitToken) {
            res.append(" ").append(1);
        } else {
            res.append(" ").append(0);
        }

        if (isKnownPrefixToken) {
            res.append(" ").append(1);
        } else {
            res.append(" ").append(0);
        }

        res.append(" ").append(punctType);

        if (hasRightAttachment) {
            res.append(" ").append(1);
        } else {
            res.append(" ").append(0);
        }

        if (isNotEmpty(label)) {
            res.append(" ").append(label);
        }

        return res.toString();
    }

    /**
     * Add the features for the chemical entity extraction model.
     */
    static public FeaturesVectorUnit addFeaturesUnit(String character,
                                                     String label,
                                                     boolean isKnownUnitToken,
                                                     boolean isKnownPrefixToken,
                                                     boolean hasRightAttachment) {

        FeatureFactory featureFactory = FeatureFactory.getInstance();

        FeaturesVectorUnit featuresVector = new FeaturesVectorUnit();

        featuresVector.value = character;
        featuresVector.label = label;

        if (featureFactory.test_all_capital(character)) {
            featuresVector.isUpperCase = true;
        } else {
            featuresVector.isUpperCase = false;
        }

        if (character.equals("(") || character.equals("[") || character.equals("{")) {
            featuresVector.punctType = "OPENBRACKET";
        } else if (character.equals(")") || character.equals("]") || character.equals("}")) {
            featuresVector.punctType = "ENDBRACKET";
        } else if (character.equals(".") || character.equals("·")) {
            featuresVector.punctType = "DOT";
        } else if (character.equals(",")) {
            featuresVector.punctType = "COMMA";
        } else if (character.equals("-")) {
            featuresVector.punctType = "HYPHEN";
        } else if (character.equals("\"") || character.equals("\'") || character.equals("`")) {
            featuresVector.punctType = "QUOTE";
        } else if (character.equals("/")) {
            featuresVector.punctType = "SLASH";
        } else if (character.equals("^")) {
            featuresVector.punctType = "EXPONENT";
        } else {
            featuresVector.punctType = "NOPUNCT";
        }

        if (featureFactory.test_number(character)) {
            featuresVector.isDigit = true;
        } else {
            featuresVector.isDigit = false;
        }

        featuresVector.hasRightAttachment = hasRightAttachment;
        featuresVector.isKnownUnitToken = isKnownUnitToken;
        featuresVector.isKnownPrefixToken = isKnownPrefixToken;

        return featuresVector;
    }


}
