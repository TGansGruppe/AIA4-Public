package de.gansgruppe.aia4.runtime.syserr;

public class InvalidIdentifierError extends SystemError {
    public InvalidIdentifierError() {
        super();
        this.id = 0x100002;
        this.name = "Invalid Identifier Error";
    }
}
