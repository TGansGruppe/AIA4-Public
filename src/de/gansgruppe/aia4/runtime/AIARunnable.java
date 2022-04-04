package de.gansgruppe.aia4.runtime;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import de.gansgruppe.aia4.Main;
import de.gansgruppe.aia4.runtime.eisp.BIS;
import de.gansgruppe.aia4.runtime.exceptions.*;
import de.gansgruppe.aia4.runtime.syserr.InvalidIdentifierError;
import de.gansgruppe.aia4.runtime.syserr.InvalidParameterError;
import de.gansgruppe.aia4.runtime.syserr.SystemError;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.SystemLogger;
import de.gansgruppe.aia4.visualise.Visualise;

public class AIARunnable implements Runnable {
	public LangProcessor LANGUAGE_PROCESSOR;
	public static int    LANGUAGE_PRCCESSOR_ADDRESS_LENGTH = 4; // Only Regarded if Byte-Mode is enabled
	public String 		 PROGRAM;

	public HashMap<Integer, Class<? extends AIAException>> EXCEPTION_LOOKUP = new HashMap();
	public HashMap<Integer, Class<? extends SystemError>>  SYS_ERROR_LOOKUP = new HashMap();

	private Thread thread;
	private boolean isAlive;

	public int rawDataBlockSize;
	private int sTime;

	// bool-arg order:
	// 0 enableNIS
	// 1 pje
	// 2 debug
	// 3 visualise
	// 4 bytemode

	public AIARunnable() {
		EXCEPTION_LOOKUP.put(0x000000, AIAException.class);
		EXCEPTION_LOOKUP.put(0x000001, InvalidInstructionException.class);
		EXCEPTION_LOOKUP.put(0x000002, InvalidProgramCounterException.class);
		EXCEPTION_LOOKUP.put(0x000003, InvalidRegisterException.class);
		EXCEPTION_LOOKUP.put(0x000004, OutOfBoundsException.class);
		EXCEPTION_LOOKUP.put(0x000005, OutOfMemoryException.class);
		EXCEPTION_LOOKUP.put(0x000006, NotFoundException.class);

		SYS_ERROR_LOOKUP.put(0x100001, InvalidParameterError.class);
		SYS_ERROR_LOOKUP.put(0x100002, InvalidIdentifierError.class);
	}

	/**
	 * Starts program execution.
	 * 
	 * @param PROGRAM The file of the program's binary
	 * @param data The data-block of the program's binary
	 * @param program The program instructions
	 * @param MEM_SIZE The size of the Memory in MU
	 * @param args A list of boolean arguments
	 * */
	public void start(String PROGRAM, byte[] data, byte[] program, int MEM_SIZE, boolean[] args, int sTime) {
		this.isAlive = true;

		// Setup for Byte-Mode, if necessary
		if (args[4]) {
			this.LANGUAGE_PROCESSOR = new ByteLangProcessor(program, data, PROGRAM);

			if (MEM_SIZE > maxAddressRange(LANGUAGE_PRCCESSOR_ADDRESS_LENGTH)) {
				this.LANGUAGE_PROCESSOR.setMemSize(maxAddressRange(LANGUAGE_PRCCESSOR_ADDRESS_LENGTH));
				((ByteLangProcessor) this.LANGUAGE_PROCESSOR).setAddressLength(LANGUAGE_PRCCESSOR_ADDRESS_LENGTH);
			} else {
				this.LANGUAGE_PROCESSOR.setMemSize(MEM_SIZE);
			}
		} else {
			this.LANGUAGE_PROCESSOR = new LangProcessor(program, data, PROGRAM);
			this.LANGUAGE_PROCESSOR.setMemSize(MEM_SIZE);
		}

		if (args[3])
			Visualise.start();
		this.LANGUAGE_PROCESSOR.setNISEnabled(args[0]);
		this.LANGUAGE_PROCESSOR.setPrintJavaExceptions(args[1]);
		this.LANGUAGE_PROCESSOR.setDebugMode(args[2]);
		this.sTime = sTime;
		BIS.init(LANGUAGE_PROCESSOR);
		
		AIAException.AIA_RUNNABLE = this;
		
		Runtime.getRuntime().addShutdownHook(new AIARunnableShutdownHook());
		
		this.PROGRAM = PROGRAM;
		this.thread  = new Thread(this, "AIA");
		this.thread.start();
	}
	
	@Override
	public void run() {
		while (!this.LANGUAGE_PROCESSOR.isFinished()) {
			this.LANGUAGE_PROCESSOR.step();

			try {
				Thread.sleep(sTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.isAlive = false;
	}
	
	/**
	 * Save the LangProcessors State to a File.
	 * 
	 * @param file The file for it to be saved to
	 * */
	public void saveState(String file) {
		int[]  MEMORY 		  = LANGUAGE_PROCESSOR.MEMORY;
		int    programCounter = LANGUAGE_PROCESSOR.programCounter;
		byte[] program		  = LANGUAGE_PROCESSOR.program;
		byte[] data 		  = LANGUAGE_PROCESSOR.data;
		int[]  REGS 	      = LANGUAGE_PROCESSOR.REGS;
		int[]  call_stack     = DataHelper.toIntArray(LANGUAGE_PROCESSOR.call_stack.toArray());
		int[]  prgm_stack     = DataHelper.toIntArray(LANGUAGE_PROCESSOR.prgm_stack.toArray());

		ArrayList<String> programErrors = LANGUAGE_PROCESSOR.programErrors;
		
		String text = "AIA R4 LangProcessor Data Dump;\n"
				+ "Program: "+PROGRAM+"\n"
				+ String.format("Allocated Memory: %s MU (%s bytes)\n", +MEMORY.length, +MEMORY.length*4);
		
		
		text += "\nProgram Counter: "+programCounter;
		text += "\n32bit Registers: \n";
		for (int i = 0; i < REGS.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(REGS[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != REGS.length-1)
				text += "\n";
		}

		text += "\n\nData Block: \n";
		for (int i = 0; i < data.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(data[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != data.length-1)
				text += "\n";
		}
		text += "\n\n";

		text += "Program Instructions: \n";
		for (int i = 0; i < program.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(program[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != program.length-1)
				text += "\n";
		}
		text += "\n\n";

		text += "MEMORY: \n";
		for (int i = 0; i < MEMORY.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(MEMORY[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != MEMORY.length-1)
				text += "\n";
		}
		text += "\n\n";

		text += "Call Stack: \n";
		for (int i = 0; i < call_stack.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(call_stack[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != call_stack.length-1)
				text += "\n";
		}
		text += "\n\n";

		text += "Program Stack: \n";
		for (int i = 0; i < prgm_stack.length; i++) {
			text += "$"+Integer.toHexString(i)+": 0x"+Integer.toHexString(prgm_stack[i])+"; ";
			if (i % 5 == 0 && i != 0 && i != prgm_stack.length-1)
				text += "\n";
		}
		text += "\n\n";

		text += "Program Errors:\n";
		Iterator<String> i = programErrors.iterator();
		while (i.hasNext()) {
			text += i.next();
		}
		
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the AIARunnable is Alive
	 * @return Is the AIARunnable alive?
	 * */
	public boolean isAlive() {
		System.out.print(""); // For some reason, this print statement is needed
		                      // for this function to work.
		return isAlive;
	}

	/**
	 * Throws a System Error or an Exception corresponding to an ID.
	 * The ID Space for AIAExceptions is anywhere from 0x000000 to
	 * 0x100000. After this, the ID Space for System Errors starts.
	 *
	 * @param errCode The Code of the Exception/Error to be thrown
	 * @param msg     The Message for the Exception/Error
	 * @param fatal	  Should the throwing exit the program?
	 * */
	public boolean throwError(int errCode, String msg, boolean fatal) {
		if (errCode < 0x100000) {
			if (!EXCEPTION_LOOKUP.containsKey(errCode)) {
				_throwError(InvalidIdentifierError.class, "Invalid Exception-Code 0x"+Integer.toHexString(errCode)+"\n", false);
				return false;
			}

			_throwException(EXCEPTION_LOOKUP.get(errCode), msg);
		} else {
			if (!SYS_ERROR_LOOKUP.containsKey(errCode)) {
				_throwError(InvalidIdentifierError.class, "Invalid Error-Code 0x"+Integer.toHexString(errCode)+"\n", false);
				return false;
			}

			_throwError(SYS_ERROR_LOOKUP.get(errCode), msg, fatal);
		}
		return true;
	}

	/**
	 * Throws a {@link de.gansgruppe.aia4.runtime.syserr.SystemError System Error}.
	 *
	 * @param errClass The Class of the Error to be thrown
	 * @param msg 	   The Error Message
	 * @param fatal    Should the error exit the program?
	 * */
	public void _throwError(Class<? extends SystemError> errClass, String msg, boolean fatal) {
		try {
			errClass.newInstance().throwError(msg, fatal);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Throws an {@link de.gansgruppe.aia4.runtime.exceptions.AIAException AIAException}.
	 *
	 * @param errClass The Class of the Exception to be thrown
	 * @param msg 	   The Exception Message
	 * */
	public void _throwException(Class<? extends  AIAException> errClass, String msg) {
		try {
			errClass.getDeclaredConstructor(byte.class, byte[].class).newInstance((byte) 0x0, new byte[]{0x0}).throwException(msg);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Used to calculate the highest reachable address with a certain amount
	 * of bytes.
	 *
	 * @param addressByteNum Amount of Bytes
	 * @return The highest address
	 * */
	public static int maxAddressRange(int addressByteNum) {
		return (int) Math.pow(2, addressByteNum*8);
	}

	public class AIARunnableShutdownHook extends Thread {
		public AIARunnableShutdownHook() {
			super("AIA-RUNNABLE-S_HOOK");
		}
		
		@Override
		public void run() {
			SystemLogger.infoln("Running AIA Cleanup Procedures [JVM-Shutdown-Hook]");
			if (Main.SAVE_STATE_FILE != null)
				Main.AIA_RUNTIME.saveState(Main.SAVE_STATE_FILE);
		}
	}
}
