package org.grobid.core.engines.label;

import org.grobid.core.engines.QuantitiesModels;

/**
 * Created by lfoppiano on 28/11/16.
 */
public class QuantitiesTaggingLabels extends TaggingLabels {
    private QuantitiesTaggingLabels() {
        super();
    }

    public static final String QUANTITY_VALUE_ATOMIC_LABEL = "<valueAtomic>";
    public static final String QUANTITY_VALUE_LEAST_LABEL = "<valueLeast>";
    public static final String QUANTITY_VALUE_MOST_LABEL = "<valueMost>";
    public static final String QUANTITY_VALUE_LIST_LABEL = "<valueList>";
    public static final String QUANTITY_UNIT_LEFT_LABEL = "<unitLeft>";
    public static final String QUANTITY_UNIT_RIGHT_LABEL = "<unitRight>";
    public static final String QUANTITY_VALUE_BASE_LABEL = "<valueBase>";
    public static final String QUANTITY_VALUE_RANGE_LABEL = "<valueRange>";
    public static final String QUANTITY_OTHER_LABEL = "<other>";

    private static final String UNIT_VALUE_BASE_LABEL = "<base>";
    private static final String UNIT_VALUE_POW_LABEL = "<pow>";
    private static final String UNIT_VALUE_PREFIX_LABEL = "<prefix>";
    private static final String UNIT_OTHER_LABEL = QUANTITY_OTHER_LABEL;

    private static final String VALUE_VALUE_NUMBER_LABEL = "<number>";
    private static final String VALUE_VALUE_ALPHA_LABEL = "<alpha>";
    private static final String VALUE_VALUE_TIME_LABEL = "<time>";
    private static final String VALUE_VALUE_EXP_LABEL = "<exp>";
    private static final String VALUE_VALUE_BASE_LABEL = "<base>";
    private static final String VALUE_VALUE_POW_LABEL = "<pow>";
    private static final String VALUE_OTHER_LABEL = QUANTITY_OTHER_LABEL;

    private static final String QUANTIFIED_OBJECT_LEFT_LABEL = "<quantifiedObject_left>";
    private static final String QUANTIFIED_OBJECT_RIGHT_LABEL = "<quantifiedObject_right>";
    private static final String QUANTIFIED_OBJECT_OTHER_LABEL = QUANTITY_OTHER_LABEL;

    public static final TaggingLabel QUANTITY_VALUE_ATOMIC = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_ATOMIC_LABEL);
    public static final TaggingLabel QUANTITY_VALUE_LEAST = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_LEAST_LABEL);
    public static final TaggingLabel QUANTITY_VALUE_MOST = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_MOST_LABEL);
    public static final TaggingLabel QUANTITY_VALUE_LIST = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_LIST_LABEL);
    public static final TaggingLabel QUANTITY_UNIT_LEFT = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_UNIT_LEFT_LABEL);
    public static final TaggingLabel QUANTITY_UNIT_RIGHT = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_UNIT_RIGHT_LABEL);
    public static final TaggingLabel QUANTITY_VALUE_BASE = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_BASE_LABEL);
    public static final TaggingLabel QUANTITY_VALUE_RANGE = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, QUANTITY_VALUE_RANGE_LABEL);
    public static final TaggingLabel QUANTITY_OTHER = new TaggingLabelImpl(QuantitiesModels.QUANTITIES, OTHER_LABEL);

    public static final TaggingLabel UNIT_VALUE_BASE = new TaggingLabelImpl(QuantitiesModels.UNITS, UNIT_VALUE_BASE_LABEL);
    public static final TaggingLabel UNIT_VALUE_POW = new TaggingLabelImpl(QuantitiesModels.UNITS, UNIT_VALUE_POW_LABEL);
    public static final TaggingLabel UNIT_VALUE_PREFIX = new TaggingLabelImpl(QuantitiesModels.UNITS, UNIT_VALUE_PREFIX_LABEL);
    public static final TaggingLabel UNIT_VALUE_OTHER = new TaggingLabelImpl(QuantitiesModels.UNITS, UNIT_OTHER_LABEL);

    public static final TaggingLabel VALUE_VALUE_NUMBER = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_NUMBER_LABEL);
    public static final TaggingLabel VALUE_VALUE_ALPHA = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_ALPHA_LABEL);
    public static final TaggingLabel VALUE_VALUE_TIME = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_TIME_LABEL);
    public static final TaggingLabel VALUE_VALUE_EXP = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_EXP_LABEL);
    public static final TaggingLabel VALUE_VALUE_BASE = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_BASE_LABEL);
    public static final TaggingLabel VALUE_VALUE_POW = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_VALUE_POW_LABEL);
    public static final TaggingLabel VALUE_VALUE_OTHER = new TaggingLabelImpl(QuantitiesModels.VALUES, VALUE_OTHER_LABEL);

    public static final TaggingLabel QUANTIFIED_OBJECT_LEFT = new TaggingLabelImpl(QuantitiesModels.QUANTIFIED_OBJECT, QUANTIFIED_OBJECT_LEFT_LABEL);
    public static final TaggingLabel QUANTIFIED_OBJECT_RIGHT = new TaggingLabelImpl(QuantitiesModels.QUANTIFIED_OBJECT, QUANTIFIED_OBJECT_RIGHT_LABEL);
    public static final TaggingLabel QUANTIFIED_OBJECT_OTHER = new TaggingLabelImpl(QuantitiesModels.QUANTIFIED_OBJECT, QUANTIFIED_OBJECT_OTHER_LABEL);

    static {
        //Quantity
        register(QUANTITY_VALUE_ATOMIC);
        register(QUANTITY_VALUE_LEAST);
        register(QUANTITY_VALUE_MOST);
        register(QUANTITY_VALUE_LIST);
        register(QUANTITY_UNIT_LEFT);
        register(QUANTITY_UNIT_RIGHT);
        register(QUANTITY_VALUE_BASE);
        register(QUANTITY_VALUE_RANGE);
        register(QUANTITY_OTHER);

        //units
        register(UNIT_VALUE_BASE);
        register(UNIT_VALUE_POW);
        register(UNIT_VALUE_PREFIX);
        register(UNIT_VALUE_OTHER);

        //value
        register(VALUE_VALUE_NUMBER);
        register(VALUE_VALUE_ALPHA);
        register(VALUE_VALUE_TIME);
        register(VALUE_VALUE_EXP);
        register(VALUE_VALUE_BASE);
        register(VALUE_VALUE_POW);
        register(VALUE_VALUE_OTHER);

        //quantified object
        register(QUANTIFIED_OBJECT_LEFT);
        register(QUANTIFIED_OBJECT_RIGHT);
        register(QUANTIFIED_OBJECT_OTHER);
    }
}
