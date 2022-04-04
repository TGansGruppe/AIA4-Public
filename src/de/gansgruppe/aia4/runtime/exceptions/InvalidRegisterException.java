package de.gansgruppe.aia4.runtime.exceptions;

/**
 * An InvalidRegisterException is thrown if an 
 * Instruction tries to access a Register under
 * an address outside of (dec) 0-15.
 * 
 * @since 1.1
 * @author 0x1905
 * */
public class InvalidRegisterException extends AIAException {
	public InvalidRegisterException(byte inst, byte[] params) {
		super(inst, params);
		this.name = "ERR_INVALID_REGISTER";
		this.id = 0x000003;
	}

	public AIAException throwException(int nReg) {
		print("The program attemped to access a register under an invalid address!", "Number "+nReg+" is too large! Highest address is 15");
		
		return this;
	}
}
