package de.gansgruppe.aia4.runtime.exceptions;

public class NotFoundException extends AIAException {
	private String cause;
	
	public NotFoundException(byte inst, byte[] params, String cause) {
		super(inst, params);
		this.cause = cause;
		this.name  = "ERR_NOT_FOUND";
		this.id    = 0x000006;
	}

	public AIAException throwException() {
		print("An Instruction attempted to access something under an invalid address!", cause);
		return this;
	}
}
