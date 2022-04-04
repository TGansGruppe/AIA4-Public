package de.gansgruppe.aia4.compiler.stdpc;

import java.util.HashMap;

import de.gansgruppe.aia4.compiler.IPreCompiler;

/**
 * {@inheritDoc}
 * 
 * Precompiler for STDIO Instruction Set. Simply replaces
 * all mentions of the instruction with the "BYTE=&lt;value&gt;"
 * keyword.
 * */
public class BISPreCompiler implements IPreCompiler {
	public static HashMap<String, Byte> INSTRUCTION_MAP = new HashMap<>();
	
	static {
		INSTRUCTION_MAP.put("AND", (byte) 0x1a);
		INSTRUCTION_MAP.put("OR",  (byte) 0x1b);
		INSTRUCTION_MAP.put("XOR", (byte) 0x1c);
		INSTRUCTION_MAP.put("ROR", (byte) 0x1d);
		INSTRUCTION_MAP.put("ROL", (byte) 0x1e);
		INSTRUCTION_MAP.put("SMU", (byte) 0x1f);
		INSTRUCTION_MAP.put("CPB", (byte) 0x20);
		INSTRUCTION_MAP.put("CMU", (byte) 0x21);
	}
	
	@Override
	public String[] compile(String[] words) {
		boolean inString = false;
		
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			
			// Avoid replacing instruction name in String
			if (word.startsWith("\"") && !inString) inString = true;
			if (inString && word.endsWith("\""))    inString = false;
			
			word = word.toUpperCase();
			if (INSTRUCTION_MAP.containsKey(word) && !inString) {
				words[i] = "BYTE="+INSTRUCTION_MAP.get(word);
			}
		}
		
		return words;
	}

	public String getName() { return "BIS-PreCompiler"; }
}
