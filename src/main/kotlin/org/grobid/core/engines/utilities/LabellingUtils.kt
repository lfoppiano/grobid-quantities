package org.grobid.core.engines.utilities

import org.grobid.core.engines.label.QuantitiesTaggingLabels
import org.grobid.core.engines.label.TaggingLabels
import java.util.*
import java.util.stream.Collectors

class LabellingUtils {
    companion object {

        /**
         * Corrects the (rare) sequence of labels that are inconsistent (and for which I did not understand the cause)
         * The sequence is recognised when an initial (I-<label>) sequence is followed by a non-initial sequence of a different label
         */
        @JvmStatic
        fun correctLabelling(resultLabelling: String): String? {
            val resultAsList: MutableList<MutableList<String>> = Arrays
                .stream(resultLabelling.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { i: String ->
                    Arrays.asList(*i.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                }
                .collect(Collectors.toList())

            var previousLabel: String? = null;
            var fixNext: Boolean = false;
            var fixValue: String? = null
            for (i in 0..resultAsList.size - 1) {
                var item: MutableList<String> = resultAsList.get(i);

                val indexLabel = item.size - 1
                val currentLabel: String = item.get(indexLabel);

                if (fixNext
                    && (currentLabel.startsWith("I-")
                        || currentLabel.equals(TaggingLabels.OTHER_LABEL)
                        || currentLabel.equals(QuantitiesTaggingLabels.QUANTITY_VALUE_RANGE_LABEL))
                ) {
                    fixNext = false
                    fixValue = null
                }

                if (!fixNext
                    && previousLabel != null
//                    && previousLabel.startsWith("I-")
                    && currentLabel != TaggingLabels.OTHER_LABEL
                    && !(currentLabel == QuantitiesTaggingLabels.QUANTITY_VALUE_RANGE_LABEL
                        || previousLabel == QuantitiesTaggingLabels.QUANTITY_VALUE_RANGE_LABEL)
                    && !currentLabel.startsWith("I-")
                    && !previousLabel.replace("I-", "").equals(currentLabel)
                ) {

                    fixNext = true
                    fixValue = previousLabel.replace("I-", "")
                }

//                println(fixNext)

                if (fixNext) {
                    fixValue?.let { item.set(indexLabel, it) }
                }

                previousLabel = currentLabel;
            }

            val fixedListAsString = resultAsList
                .map { i -> i.joinToString(separator = "\t") }
                .toList().joinToString(separator = "\n")

            return fixedListAsString
        }
    }
}
