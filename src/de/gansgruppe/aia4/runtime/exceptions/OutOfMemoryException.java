package de.gansgruppe.aia4.runtime.exceptions;

public class OutOfMemoryException extends AIAException {
	public OutOfMemoryException(byte inst, byte[] params) {
		super(inst, params);
		this.name = "ERR_OUT_OF_MEMORY";
		this.id = 0x000005;
	}
	
	public AIAException throwException(String instname) {
		print("An instruction attempted to write more, than can fit in memory!", "INST_NAME = "+instname);
		return this;
	}
}
