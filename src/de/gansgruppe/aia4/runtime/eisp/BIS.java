package de.gansgruppe.aia4.runtime.eisp;

import de.gansgruppe.aia4.runtime.LangProcessor;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.SystemLogger;

/**
 * The BIS (Byte Instruction Set Processor) handles
 * the processing of every instruction within the BIS.
 *
 * @since 0.3.0
 * @author 0x1905
 * */
public class BIS {
	private static LangProcessor LANG_PROCESSOR;
	//public static byte[] LANG_PROCESSOR.MEMORY;
	
	public static void init(LangProcessor langProc) {
		LANG_PROCESSOR = langProc;
		//LANG_PROCESSOR.MEMORY = new byte[LANG_PROCESSOR.MEMORY.length*4];
	}
	
	//////////////////
	// INSTRUCTIONS //
	/////////////////////////////////////
	// $1a AND <address> <address>     //
	// $1b OR  <address> <address>     //
	// $1c XOR <address> <address>     //
	// $1d ROR <address>               //
	// $1e ROL <address>               //
	// $1f SMU <address> <s_address>   //
	// $20 CPB <address> <address>     //
	// $21 CMU <s_address> <address>   //
	/////////////////////////////////////
	
	public static int process(byte inst) {
		switch (inst) {
		case 0x1a: // AND
			LANG_PROCESSOR.REGS[4] = LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] & LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)];
			if (LANG_PROCESSOR.debugMode) {
				SystemLogger.debugf("BIS_AND INPUT: %s ; %s\n", LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)], LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]);
				SystemLogger.debugln("BIS_AND RESULT: "+(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] & LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]));
			}
			return 9;
		case 0x1b: // OR			
			LANG_PROCESSOR.REGS[4] = LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] | LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)];
			if (LANG_PROCESSOR.debugMode) {
				SystemLogger.debugf("BIS_OR INPUT: %s ; %s\n", LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)], LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]);
				SystemLogger.debugln("BIS_OR RESULT: "+(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] | LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]));
			}
			return 9;
		case 0x1c: // XOR
			LANG_PROCESSOR.REGS[4] = LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] ^ LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)];
			if (LANG_PROCESSOR.debugMode) {
				SystemLogger.debugf("BIS_XOR INPUT: %s ; %s\n", LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)], LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]);
				SystemLogger.debugln("BIS_XOR RESULT: "+(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] ^ LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)]));
			}
			return 9;
		case 0x1d: // ROR
			LANG_PROCESSOR.REGS[4] = LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] >> 1;
			if (LANG_PROCESSOR.debugMode) {
				SystemLogger.debugf("BIS_ROR INPUT: %s\n", LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)]);
				SystemLogger.debugln("BIS_ROR RESULT: "+(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] >> 1));
			}
			return 5;
		case 0x1e: // ROL
			LANG_PROCESSOR.REGS[4] = LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] << 1;
			if (LANG_PROCESSOR.debugMode) {
				SystemLogger.debugf("BIS_ROL INPUT: %s\n", LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)]);
				SystemLogger.debugln("BIS_ROL RESULT: "+(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)] << 1));
			}
			return 5;
		case 0x1f: // SMU
			byte[] bytes = DataHelper.convertToByteArray(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)]);
			int dest = LANG_PROCESSOR.getNextInt(4);
			for (int i = 0; i < 4; i++) {
				LANG_PROCESSOR.MEMORY[dest+i] = bytes[i];
			}
			return 9;
		case 0x20: // REV
			LANG_PROCESSOR.REGS[4] = Integer.reverse(LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(0)]);
			return 9;
		case 0x21: // CMU
			/*byte[] bytez = new byte[4];
			int start = LANG_PROCESSOR.getNextInt(0);
			for (int i = 0; i < 4; i++) {
				bytez[i] = LANG_PROCESSOR.MEMORY[start+i];
			}
			
			LANG_PROCESSOR.MEMORY[LANG_PROCESSOR.getNextInt(4)] = DataHelper.createInt(bytez);
			 */
			return 9;
		}
		return -1;
	}
}
