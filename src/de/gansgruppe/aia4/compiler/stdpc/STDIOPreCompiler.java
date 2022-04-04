package de.gansgruppe.aia4.compiler.stdpc;

import java.util.HashMap;

import de.gansgruppe.aia4.compiler.IPreCompiler;

/**
 * {@inheritDoc}
 * 
 * Precompiler for STDIO Instruction Set. Simply replaces
 * all mentions of the instruction with the "BYTE=&lt;value&gt;"
 * keyword. The Instruction processor can be found {@link de.gansgruppe.aia4.runtime.eisp.STDIO here}
 *
 * @see <a href="https://docs.google.com/document/d/1HDlPl65cgdLYJK3b4fkAps4OvTiHvNVWGJqqc8YygoM/edit?usp=sharing">STDIO-IS Spec.</a>
 * */
public class STDIOPreCompiler implements IPreCompiler {
	public static HashMap<String, Byte> INSTRUCTION_MAP = new HashMap<>();
	
	static {
		INSTRUCTION_MAP.put("STD_IN", (byte) 0x10);
		INSTRUCTION_MAP.put("FL_OPN", (byte) 0x11);
		INSTRUCTION_MAP.put("FL_WRT", (byte) 0x12);
		INSTRUCTION_MAP.put("FL_CLS", (byte) 0x13);
		INSTRUCTION_MAP.put("FL_CRT", (byte) 0x14);
		INSTRUCTION_MAP.put("FL_APP", (byte) 0x15);
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

	public String getName() { return "STDIO-PreCompiler"; }
}
