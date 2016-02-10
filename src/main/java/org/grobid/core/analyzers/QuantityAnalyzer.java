package org.grobid.core.analyzers;

import org.grobid.core.layout.LayoutToken;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
	
/**
 * Default tokenizer adequate for all Indo-European languages.
 *
 * @author Patrice Lopez
 */

public class QuantityAnalyzer {

    public static final String delimiters = " \n\r\t([^%‰°,:;?.!/)-–\"“”‘’'`$]*\u2666\u2665\u2663\u2660\u00A0";

	public static List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(text, delimiters, true);
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			// in addition we split "letter" characters and digits
			String[] subtokens = token.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=\\D)");
			for(int i=0; i<subtokens.length; i++)
				result.add(subtokens[i]);
		}

		return result;
	}

	public static List<LayoutToken> tokenizeWithLayoutToken(String text) {
		List<LayoutToken> result = new ArrayList<LayoutToken>();
		StringTokenizer st = new StringTokenizer(text, delimiters, true);
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			// in addition we split "letter" characters and digits
			String[] subtokens = token.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=\\D)");
			for(int i=0; i<subtokens.length; i++) {
				LayoutToken layoutToken = new LayoutToken();
				layoutToken.setText(subtokens[i]);
				result.add(layoutToken);
			}
		}

		return result;
	}
	
	public static List<String> retokenize(List<String> chunks) {
		StringTokenizer st = null;
		List<String> result = new ArrayList<String>();
		for(String chunk : chunks) {
			st = new StringTokenizer(chunk, delimiters, true);
			while(st.hasMoreTokens()) {
				// in addition we split "letter" characters and digits
				String token = st.nextToken();
				// in addition we split "letter" characters and digits
				String[] subtokens = token.split("(?<=[a-zA-Z])(?=\\d)|(?<=\\d)(?=\\D)");
				for(int i=0; i<subtokens.length; i++) 
					result.add(subtokens[i]);
			}
		}
		return result;
	}
}