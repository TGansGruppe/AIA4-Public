package de.gansgruppe.aia4.compiler.stdpc;

import java.util.HashMap;

import de.gansgruppe.aia4.compiler.IPreCompiler;
/** 
 * {@inheritDoc}
 * 
 * Precompiler for Network Instruction Set. Simply replaces
 * all mentions of the instruction with the "BYTE=&lt;value&gt;"
 * keyword.
 * */
public class NISPreCompiler implements IPreCompiler {
public static HashMap<String, Byte> INSTRUCTION_MAP = new HashMap<>();
	
	static {
		INSTRUCTION_MAP.put("TCP_CNCT",  (byte) 0x16);
		INSTRUCTION_MAP.put("TCP_DCNCT", (byte) 0x17);
		INSTRUCTION_MAP.put("TCP_SEND",  (byte) 0x18);
		INSTRUCTION_MAP.put("TCP_RECV",  (byte) 0x19);
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

	public String getName() { return "NIS-PreCompiler"; }
}