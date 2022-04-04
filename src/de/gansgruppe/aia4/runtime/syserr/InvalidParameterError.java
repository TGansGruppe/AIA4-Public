package de.gansgruppe.aia4.runtime.syserr;

public class InvalidParameterError extends SystemError {
	public InvalidParameterError() {
		super();
		this.id = 0x100001;
		this.name = "Invalid Parameter Error";
	}
}
