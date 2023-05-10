package org.grobid.core.engines.utilities

import java.util.*
import java.util.stream.Collectors

class GenericUtils {
    companion object {
        @JvmStatic
        fun correctLabelling(resultLabelling: String): String? {
            val resultAsList: MutableList<MutableList<String>> = Arrays
                .stream(resultLabelling.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { i: String -> Arrays.asList(*i.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) }
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
                        || currentLabel.equals("<other>"))
                ) {
                    fixNext = false
                    fixValue = null
                }

                if (!fixNext
                    && previousLabel != null
                    && previousLabel.startsWith("I-")
                    && currentLabel != "<other>"
                    && !currentLabel.startsWith("I-")
                    && !previousLabel.replace("I-", "").equals(currentLabel)
                ) {

                    fixNext = true
                    fixValue = previousLabel.replace("I-", "")
                }

                println(fixNext)

                if (fixNext) {
                    fixValue?.let { item.set(indexLabel, it) }
                }

                previousLabel = currentLabel;
            }

            val fixedList = resultAsList
                .map { i -> i.joinToString(separator = "\t") }
                .toList().joinToString(separator = "\n")

            return fixedList
        }
    }
}
