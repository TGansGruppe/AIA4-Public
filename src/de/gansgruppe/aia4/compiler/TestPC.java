package de.gansgruppe.aia4.compiler;

import java.util.ArrayList;

/**
 * A simple example Pre-OldCompiler which just replaces
 * any instance of the word "TESTINST" with a log instruction.
 * 
 * @author Alan Goose
 * */
public class TestPC implements IPreCompiler {

	@Override
	public String[] compile(String[] words) {
		ArrayList<String> nWords = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			String s = words[i];
			if (s.matches("TESTINST")) {
				nWords.add("log");
				nWords.add("\"TESTINST");
				nWords.add("CALLED\"");
			} else {
				nWords.add(s);
			}
		}
		Object[] neWords = nWords.toArray();
		String[] out = new String[neWords.length];
		for (int i = 0; i < neWords.length; i++) {
			out[i] = (String) neWords[i];
		}
		
		return out;
	}

	public String getName() { return "TestPrecompiler"; }
}
