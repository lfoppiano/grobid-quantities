package org.grobid.core.engines.label;

import org.grobid.core.engines.QuantitiesModels;

/**
 * Created by lfoppiano on 28/11/16.
 */
public class QuantitiesTaggingLabels extends TaggingLabels {
    private QuantitiesTaggingLabels() {
        super();
    }

    private static final String QUANTITY_VALUE_ATOMIC_LABEL = "<valueAtomic>";
    private static final String QUANTITY_VALUE_LEAST_LABEL = "<valueLeast>";
    private static final String QUANTITY_VALUE_MOST_LABEL = "<valueMost>";
    private static final String QUANTITY_VALUE_LIST_LABEL = "<valueList>";
    private static final String QUANTITY_UNIT_LEFT_LABEL = "<unitLeft>";
    private static final String QUANTITY_UNIT_RIGHT_LABEL = "<unitRight>";
    private static final String QUANTITY_VALUE_BASE_LABEL = "<valueBase>";
    private static final String QUANTITY_VALUE_RANGE_LABEL = "<valueRange>";
    private static final String QUANTITY_OTHER_LABEL = "<other>";

    private static final String UNIT_VALUE_BASE_LABEL = "<base>";
    private static final String UNIT_VALUE_POW_LABEL = "<pow>";
    private static final String UNIT_VALUE_PREFIX_LABEL = "<prefix>";
    private static final String UNIT_OTHER_LABEL = QUANTITY_OTHER_LABEL;

    private static final String VALUE_VALUE_VALUE_LABEL = "<val>";
    private static final String VALUE_VALUE_OPERATION_LABEL = "<operation>";
    private static final String VALUE_VALUE_BASE_LABEL = "<base>";
    private static final String VALUE_VALUE_POW_LABEL = "<pow>";
    private static final String VALUE_OTHER_LABEL = QUANTITY_OTHER_LABEL;

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

    public static final TaggingLabel VALUE_VALUE_VALUE = new TaggingLabelImpl(QuantitiesModels.VALUE, VALUE_VALUE_VALUE_LABEL);
    public static final TaggingLabel VALUE_VALUE_OPERATION = new TaggingLabelImpl(QuantitiesModels.VALUE, VALUE_VALUE_OPERATION_LABEL);
    public static final TaggingLabel VALUE_VALUE_BASE = new TaggingLabelImpl(QuantitiesModels.VALUE, VALUE_VALUE_BASE_LABEL);
    public static final TaggingLabel VALUE_VALUE_POW = new TaggingLabelImpl(QuantitiesModels.VALUE, VALUE_VALUE_POW_LABEL);
    public static final TaggingLabel VALUE_VALUE_OTHER = new TaggingLabelImpl(QuantitiesModels.VALUE, VALUE_OTHER_LABEL);

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
        register(VALUE_VALUE_VALUE);
        register(VALUE_VALUE_OPERATION);
        register(VALUE_VALUE_BASE);
        register(VALUE_VALUE_POW);
        register(VALUE_VALUE_OTHER);

    }
}
