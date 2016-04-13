package org.grobid.core.lexicon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.map.HashedMap;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * Created by lfoppiano on 22/03/16.
 */
public class LexiconLoader {

    public static void readJsonFile(InputStream ist, String listName, Closure<JsonNode> onElement) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(ist);

            Iterator<JsonNode> it = rootNode.get(listName).elements();
            while(it.hasNext()){
                JsonNode node = it.next();

                onElement.execute(node);
            }
        } catch (JsonProcessingException e) {
            throw new
                    GrobidResourceException("Error when compiling lexicon matcher in vocabulary.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occurred while running GROBID.", e);
        } finally {
            closeStreams(ist, null, null);
        }


    }

    public static void readCsvFile(InputStream ist, Closure<String> onLine) {
        InputStreamReader isr = null;
        BufferedReader dis = null;
        try {
            isr = new InputStreamReader(ist, "UTF8");
            dis = new BufferedReader(isr);

            String l = null;
            while ((l = dis.readLine()) != null) {
                if (l.length() == 0) continue;

                onLine.execute(l);
            }
        } catch (PatternSyntaxException e) {
            throw new
                    GrobidResourceException("Error when compiling lexicon matcher in vocabulary.", e);
        } catch (IOException e) {
            throw new GrobidException("An exception occurred while running GROBID.", e);

        } finally {
            closeStreams(ist, isr, dis);
        }
    }

    private static void closeStreams(InputStream ist, InputStreamReader isr, BufferedReader dis) {
        try {
            if (ist != null)
                ist.close();
            if (isr != null)
                isr.close();
            if (dis != null)
                dis.close();
        } catch (Exception e) {
            throw new GrobidResourceException("Cannot close all streams.", e);
        }
    }


    public static Map<String, String> loadPrefixes(InputStream is) {
        Map<String, String> prefixes = new HashedMap<>();

        readCsvFile(is, input -> {
            String pieces[] = input.split("\t");
            if (pieces.length == 3) {

                String symbol = pieces[1].trim();
                String name = pieces[2].trim();

                prefixes.put(symbol, name);
            }
        });

        return prefixes;
    }


    public static Map<String, List<String>> loadInflections(InputStream is) {
        Map<String, List<String>> inflection = new HashedMap<>();

        readCsvFile(is, input -> {
            String pieces[] = input.split("\t");
            if (pieces.length == 2) {

                String name = pieces[0].trim();
                String inflections = pieces[1].trim();
                List<String> inflectionList = new ArrayList<>();
                String[] subInflection = inflections.split(",");

                for (String subFlection : subInflection) {
                    inflectionList.add(subFlection.trim());
                }

                if (inflectionList.size() > 0) {
                    inflection.put(name, inflectionList);
                }
            }
        }

        );

        return inflection;
    }

}
