package de.gansgruppe.aia4.runtime.exceptions;

/**
 * An InvalidProgramCounterException is thrown if the 
 * Program Counter exceeds the Program Size.
 * 
 * @since 1.1
 * @author 0x1905
 * */
public class InvalidProgramCounterException extends AIAException {
	public InvalidProgramCounterException(byte inst, byte[] params) {
		super(inst, params);
		this.name = "ERR_INTERNAL_INVALID_PC";
		this.id = 0x000002;
	}
	
	public AIAException throwException() {
		print("Program Counter is too high!", String.format("(Program Counter = byte %s ; Program Size = %s bytes)", AIAException.AIA_RUNNABLE.LANGUAGE_PROCESSOR.programCounter+1, AIAException.AIA_RUNNABLE.LANGUAGE_PROCESSOR.program.length));
		return this;
	}
}
