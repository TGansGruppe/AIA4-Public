package de.gansgruppe.aia4.util;

/**
 * The SystemLogger is used by the AIARuntime to print out
 * information and errors. It can be enabled using the -sl
 * runtime argument.
 *
 * @since 0.1.0
 * */
public class SystemLogger {
	public static boolean loggerEnabled = false;
	
	public static void errln(String msg) {
		System.err.println("[SYSTEM] ERROR: "+msg);
	}
	
	public static void errf(String msg, Object... args) {
		System.err.printf("[SYSTEM] ERROR: "+msg , args);
	}
	
	public static void infoln(String msg) {
		println("[SYSTEM] INFO: "+msg);
	}
	
	public static void infof(String msg, Object... args) {
		printf("[SYSTEM] INFO: "+msg, args);
	}	
	
	public static void warnln(String msg) {
		println(ANSIColor.YELLOW+"[SYSTEM] WARNING: "+msg+ANSIColor.RESET);
	}
	
	public static void warnf(String msg, Object... args) {
		printf(ANSIColor.YELLOW+"[SYSTEM] WARNING: "+msg+ANSIColor.RESET, args);
	}
	
	public static void msgln(String msg) {
		println("# "+msg);
	}
	
	public static void msgf(String msg, Object... args) {
		printf("# "+msg, args);
	}
	
	public static void debugln(String msg) {
		println(ANSIColor.ITALIC+"?? "+msg+ANSIColor.RESET);
	}
	
	public static void debugf(String msg, Object... args) {
		printf(ANSIColor.ITALIC+"?? "+msg+ANSIColor.RESET, args);
	}
	
	private static void printf(String s, Object... args) {
		if (loggerEnabled) System.out.printf(s, args);
	}
	
	private static void println(String s) {
		if (loggerEnabled) System.out.println(s);
	}
	
	public static class ANSIColor {
		public static final String	RESET				= "\u001B[0m";

		public static final String	HIGH_INTENSITY		= "\u001B[1m";
		public static final String	LOW_INTENSITY		= "\u001B[2m";

		public static final String	ITALIC				= "\u001B[3m";
		public static final String	UNDERLINE			= "\u001B[4m";
		public static final String	BLINK				= "\u001B[5m";
		public static final String	RAPID_BLINK			= "\u001B[6m";
		public static final String	REVERSE_VIDEO		= "\u001B[7m";
		public static final String	INVISIBLE_TEXT		= "\u001B[8m";

		public static final String	BLACK				= "\u001B[30m";
		public static final String	RED					= "\u001B[31m";
		public static final String	GREEN				= "\u001B[32m";
		public static final String	YELLOW				= "\u001B[33m";
		public static final String	BLUE				= "\u001B[34m";
		public static final String	MAGENTA				= "\u001B[35m";
		public static final String	CYAN				= "\u001B[36m";
		public static final String	WHITE				= "\u001B[37m";

		public static final String	BACKGROUND_BLACK	= "\u001B[40m";
		public static final String	BACKGROUND_RED		= "\u001B[41m";
		public static final String	BACKGROUND_GREEN	= "\u001B[42m";
		public static final String	BACKGROUND_YELLOW	= "\u001B[43m";
		public static final String	BACKGROUND_BLUE		= "\u001B[44m";
		public static final String	BACKGROUND_MAGENTA	= "\u001B[45m";
		public static final String	BACKGROUND_CYAN		= "\u001B[46m";
		public static final String	BACKGROUND_WHITE	= "\u001B[47m";
	}
}
