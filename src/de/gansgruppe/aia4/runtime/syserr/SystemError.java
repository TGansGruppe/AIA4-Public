package de.gansgruppe.aia4.runtime.syserr;

import de.gansgruppe.aia4.runtime.AIARunnable;
import de.gansgruppe.aia4.util.SystemLogger;

public class SystemError {
    public static AIARunnable AIA_RUNNABLE = null;

    protected String name = "SYS_ERR_STD";
    protected int id = 0x000000;

    /**
     * Throws the exception.
     *
     * @param msg The cause of the Exception
     * @return The exception object
     * */
    public SystemError throwError(String msg, boolean fatal) {
        print(msg);
        if (fatal) System.exit(0);
        return this;
    }

    /**
     * Prints the exception message to errout.
     *
     * @param msg Message explaining or detailing the cause
     * */
    protected void print(String msg) {
        SystemLogger.errf("SYS_ERR %s (0x%s)\n", name, String.format("%06x", id));
        SystemLogger.errf(msg);
    }

    /**
     * @return The ID of the Exception
     * */
    public int getID() {
        return this.id;
    }
}
