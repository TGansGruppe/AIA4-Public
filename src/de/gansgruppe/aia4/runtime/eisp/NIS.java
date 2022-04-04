// Network Instruction Set 1.0
package de.gansgruppe.aia4.runtime.eisp;

/**
 * NIS 1.0 implementation for AIA 4.
 * Designed by Alan Goose &amp; Henry Portsmith
 * Implemented by 0x1905 as part of AIA 4
 * 
 * Copyright (c) GansGruppe 2021
 * 
 * @since 1.0
 * @author 0x1905
 * */
public class NIS {
	public static int process(byte inst, byte[] program, int pc, byte[] data, int[] memory, int[] regs) {
		inst = (byte) (inst-15);
		
		switch (inst) {
		case 0:
			break;
		default:
			return -1;
		}
		
		return 0;
	}
}
