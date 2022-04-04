package de.gansgruppe.aia4.runtime;

import de.gansgruppe.aia4.util.Instructions;
import de.gansgruppe.aia4.util.SystemLogger;

/**
 * Contains Functions for Debug Mode to declutter the
 * {@link de.gansgruppe.aia4.runtime.LangProcessor}.
 * */
public class AIADebugHelper {
	/**
	 * Prints Debug Information for Memory Accesses.
	 * @param instruction The instruction id
	 * @param langProc Language Processor Instance to debug for
	 * */
	public static void memoryAccessDebug(byte instruction, LangProcessor langProc) {
		String instName = Instructions.INST_NAMES.get(instruction).toLowerCase();
		switch (instName) {
			case "set":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x TO $%08x\n", langProc.programCounter,
						langProc.getNextInt(4), langProc.getNextInt(0));
				break;
			case "cpm":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x FROM $%08x TO $%08x\n", langProc.programCounter,
						langProc.MEMORY[langProc.getNextInt(0)], langProc.getNextInt(0), langProc.getNextInt(4));
				break;
			case "pcpm":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x FROM $%08x TO $%08x\n", langProc.programCounter,
						langProc.MEMORY[langProc.MEMORY[langProc.getNextInt(0)]],  // VALUE
						langProc.MEMORY[langProc.getNextInt(0)], // FROM
						langProc.MEMORY[langProc.getNextInt(4)]); // TO
				break;
			case "cpr":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x FROM r$%01x TO $%08x\n", langProc.programCounter,
				langProc.REGS[langProc.getNextInt(0)], langProc.getNextInt(0), langProc.getNextInt(4));
				break;
			case "pget":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x FROM s$%01x TO $%08x\n", langProc.programCounter,
						langProc.data[langProc.MEMORY[langProc.getNextInt(0)]], // VALUE
						langProc.MEMORY[langProc.getNextInt(0)], // FROM
						langProc.MEMORY[langProc.getNextInt(4)]); // TO
				break;
			case "get":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: WRITING VALUE 0x%08x FROM s$%01x TO $%08x\n", langProc.programCounter,
						langProc.data[langProc.getNextInt(0)], langProc.getNextInt(0), langProc.getNextInt(4));
				break;
			case "pop":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: POPPING VALUE 0x%08x FROM STACK TO [r,#,normal]$%08x\n", langProc.programCounter,
						langProc.prgm_stack.peek(), langProc.getNextInt(4));
				break;
			case "peek":
				SystemLogger.debugf("PC [$%08x]: MEMORY ACCESS: PEEKING VALUE 0x%08x FROM STACK TO [r,#,normal]$%08x\n", langProc.programCounter,
						langProc.prgm_stack.peek(), langProc.getNextInt(4));
				break;
		}
	}
}
