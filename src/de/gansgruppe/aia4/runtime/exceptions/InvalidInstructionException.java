package de.gansgruppe.aia4.runtime.exceptions;

import de.gansgruppe.aia4.util.DataHelper;

/**
 * An InvalidInstructionException is thrown if an 
 * Instruction code corresponds to no implementation.
 * 
 * @since 1.1
 * @author 0x1905
 * */
public class InvalidInstructionException extends AIAException {
	public InvalidInstructionException(byte inst, byte[] params) {
		super(inst, params);
		this.name = "ERR_INVALID_INSTRUCTION";
		this.id = 0x000001;
	}

	public AIAException throwException() {
		print("An invalid instruction code was found in the program!", "No Instruction under code: "+DataHelper.byteToInt(inst));
		AIAException.AIA_RUNNABLE.LANGUAGE_PROCESSOR.programCounter++;
		
		return this;
	}
}
