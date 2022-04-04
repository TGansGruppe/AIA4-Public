package de.gansgruppe.aia4.runtime.exceptions;

import java.util.Arrays;

import de.gansgruppe.aia4.runtime.AIARunnable;
import de.gansgruppe.aia4.runtime.LangProcessor;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.SystemLogger;

/**
 * Superclass for all AIA Language Exceptions.
 * 
 * @since 1.1
 * @author 0x1905
 * */
public class AIAException {
	public static AIARunnable AIA_RUNNABLE = null;
	
	protected String name = "ERR_STD_EXCEPTION";
	protected int id = 0x000000;
	protected byte inst;
	protected byte[] params;
	protected int pc;
	protected String programFile;
	
	protected String[] messageLines;
	
	/**
	 * @param inst Instruction causing the Exception
	 * @param params Parameters of the instruction
	 * */
	public AIAException(byte inst, byte[] params) {
		this.inst = inst;
		this.params = params;
		this.pc = AIA_RUNNABLE.LANGUAGE_PROCESSOR.programCounter;
		this.programFile = AIA_RUNNABLE.PROGRAM;
	}
	
	/**
	 * Throws the exception.
	 * 
	 * @param cause The cause of the Exception
	 * @return The exception object
	 * */
	public AIAException throwException(String cause) {
		print(cause, "///");
		return this;
	}
	
	/**
	 * Prints the exception message to errout.
	 * 
	 * @param cause The cause of the exception
	 * @param msg Message explaining or detailing the cause
	 * */
	protected void print(String cause, String msg) {
		messageLines = new String[] {
				String.format("!! EXCEPTION: 0x%s\n", String.format("%06x", id)),
				String.format("!! An exception occurred whilst executing program \"%s\":\n", programFile),
				String.format("!! $%s | %s (%s) %s\n", Integer.toHexString(pc), DataHelper.byteToInt(inst), LangProcessor.INSTRUCTION_MAP.get((int) inst), Arrays.toString(DataHelper.convertToIntArray(params))),
				"!! de.gansgruppe.aia4 "+name+" : "+cause+"\n",
				"!! "+msg+"\n"
		};
		
		System.err.printf("!! EXCEPTION ID: 0x%s\n", String.format("%06x", id));
		System.err.printf("!! An exception occurred whilst executing program \"%s\":\n", programFile);
		System.err.printf("!! $%s | %s (%s) %s\n", Integer.toHexString(pc), DataHelper.byteToInt(inst), LangProcessor.INSTRUCTION_MAP.get((int) inst), Arrays.toString(DataHelper.convertToIntArray(params)));
		System.err.println("!! de.gansgruppe.aia4 "+name+" : "+cause);
		if (msg != null || msg != "") System.err.println("!! "+msg);

		if (AIA_RUNNABLE.LANGUAGE_PROCESSOR.debugMode) {
			int pif = AIA_RUNNABLE.rawDataBlockSize+pc;
			SystemLogger.debugf("Point in File: %s/0x%s\n", pif, Integer.toHexString(pif));
		}
	}
	
	/**
	 * @return All lines of the Exception message. (Only works if exception message was printed)
	 * */
	public String[] getMessageLines() {
		return messageLines;
	}
	
	/**
	 * @return The ID of the Exception
	 * */
	public int getID() {
		return this.id;
	}
}
