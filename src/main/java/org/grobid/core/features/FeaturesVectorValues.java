package org.grobid.core.features;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FeaturesVectorValues {
    public String value;                        // lexical feature
    public boolean isUpperCase = false;
    public boolean isDigit = false;
    //    public boolean isKnownOperationToken = false;
    public String label = null;                 // label if known

    // one of NOPUNCT, OPENBRACKET, ENDBRACKET, DOT, COMMA, HYPHEN, QUOTE, ASTERISK, PUNCT (default)
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

        // punctuation information (1)
        res.append(" " + punctType); // in case the token is a punctuation (NO otherwise)

//        if (isKnownOperationToken) {
//            res.append(" ").append(1);
//        } else {
//            res.append(" ").append(0);
//        }

        if (isNotEmpty(label)) {
            res.append(" ").append(label);
        }

        return res.toString();
    }

    /**
     * Add the features for the chemical entity extraction model.
     */
    public static FeaturesVectorValues addFeatures(String character, String label) {

        FeatureFactory featureFactory = FeatureFactory.getInstance();

        FeaturesVectorValues featuresVector = new FeaturesVectorValues();

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
        } else if (character.equals("-") || character.equals("−") || character.equals("–")) {
            featuresVector.punctType = "HYPHEN";
        } else if (character.equals("\"") || character.equals("\'") || character.equals("`")) {
            featuresVector.punctType = "QUOTE";
        } else if (character.equals("/") || character.equals("∕")) {
            featuresVector.punctType = "SLASH";
        } else if (character.equals("^")) {
            featuresVector.punctType = "EXPONENT";
        } else if (character.equals("*")) {
            featuresVector.punctType = "ASTERISK";
        } else {
            featuresVector.punctType = "NOPUNCT";
        }

        if (featureFactory.test_number(character)) {
            featuresVector.isDigit = true;
        } else {
            featuresVector.isDigit = false;
        }

//        featuresVector.isKnownOperationToken = ;

        return featuresVector;
    }
}
