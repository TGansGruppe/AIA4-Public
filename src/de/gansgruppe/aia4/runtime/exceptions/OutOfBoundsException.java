package de.gansgruppe.aia4.runtime.exceptions;

public class OutOfBoundsException extends AIAException {
	public OutOfBoundsException(byte inst, byte[] params) {
		super(inst, params);
		this.name = "ERR_OUT_OF_BOUNDS";
		this.id = 0x000004;
	}
	
	public AIAException throwException() {
		print("An Instruction attempted to access something under an invalid address!", "An Instruction attempted to access something under an invalid address!");
		return this;
	}
}
