package de.gansgruppe.aia4.runtime;

import java.io.PrintStream;
import java.util.*;

import de.gansgruppe.aia4.Main;
import de.gansgruppe.aia4.runtime.eisp.BIS;
import de.gansgruppe.aia4.runtime.eisp.NIS;
import de.gansgruppe.aia4.runtime.eisp.STDIO;
import de.gansgruppe.aia4.runtime.exceptions.AIAException;
import de.gansgruppe.aia4.runtime.exceptions.InvalidInstructionException;
import de.gansgruppe.aia4.runtime.exceptions.InvalidProgramCounterException;
import de.gansgruppe.aia4.runtime.exceptions.InvalidRegisterException;
import de.gansgruppe.aia4.runtime.exceptions.NotFoundException;
import de.gansgruppe.aia4.runtime.exceptions.OutOfBoundsException;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.Instructions;
import de.gansgruppe.aia4.util.SystemLogger;

/**
 * The core of the AIA4 Runtime. The purpose of this class
 * is to execute each instruction or pass it to another Instruction Processor.
 *
 * @see <a href="https://tinyurl.com/AIAR4SPEC">AIA REVISION 4 SPECIFICATION</a>
 *
 * @author 0x1905
 * @author Alan Goose
 * */
@SuppressWarnings("WeakerAccess")
public class LangProcessor {
	/*
	   NOTE: These variables should remain public, as the data in LanguageProcessor
	   should be accessible by anyone within the java runtime so that it can
	   easily be used in a library.
	*/

	public String programFile; // Program file

	public int[] MEMORY;                          // Program Memory
	public byte[] data;                           // Static Data-Block
	public byte[] program;                        // The Program-Bytecode
	public int[] REGS = new int[16];              // All 16 32-Bit Registers (See AIA4 specification Chapter 5)
	public Stack<Integer> call_stack;             // Call Stack (not accessible by the program)
	public Stack<Integer> prgm_stack;             // General-Purpose-Stack for use by a program
	public int programCounter = 0;                // Points to current instruction in the program
	protected PrintStream outStream = System.out; // What stream to be used as stdout for the program

	public ArrayList<String> programErrors = new ArrayList<>(); // List of all AIAException-Messages that were thrown
	public Exception lastException = null;                      // Contains last Java Exception

	public boolean printJavaExceptions = false; // Sets if Java-Exceptions should be logged
	public boolean enableNIS = false;           // Enables Network Instruction Set (Is to be deprecated in version 0.5.0)
	public boolean isFinished = false;          // Sets if program is finished
	public boolean debugMode = false;           // Enables Debug-Mode

	protected int lastPc = 0;

	// Configuration Fields derived from optional config
	public static boolean iieDestructive = true;  // Sets if an occurrence of the InvalidInstructionException should cause the program to exit
	public static boolean ireDestructive = true;  // Sets if an occurrence of the InvalidRegisterException should cause the program to exit
	public static boolean oomDestructive = false; // Sets if an occurrence of the OutOfMemoryException should cause the program to exit
	public static boolean enfDestructive = true;  // Sets if an occurrence of the NotFoundException should cause the program to exit

	public static HashMap<Integer, Boolean> ERR_DESTRUCTIVE_CNF = new HashMap<>();

	// Instruction Map for getting Instruction Names
	public static HashMap<Byte, String> INSTRUCTION_MAP = Instructions.INST_NAMES;
	static {
		ERR_DESTRUCTIVE_CNF.put(0x000000, false);
		ERR_DESTRUCTIVE_CNF.put(0x000001, iieDestructive);
		ERR_DESTRUCTIVE_CNF.put(0x000002, true);
		ERR_DESTRUCTIVE_CNF.put(0x000003, ireDestructive);
		ERR_DESTRUCTIVE_CNF.put(0x000004, true);
		ERR_DESTRUCTIVE_CNF.put(0x000005, oomDestructive);
		ERR_DESTRUCTIVE_CNF.put(0x000006, enfDestructive);
	}

	//TODO(all): Implement instructions which take data either from program data or from memory.

	///////////////////////////////////////////
	// AIA MINIMAL INSTRUCTION SET (AIA MIS) //
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PADDRESS = Program-Address
	//
	// 0 SET <ADDRESS> <VALUE>			| Set a Value at a point in Memory																	                //
	// 1 CMP <ADDRESS> <ADDRESS>		| Compare two Numbers located at addresses in Memory, stores result in 1st 32bit register			                //
	// 2 JE  <PADDRESS>					| Jumps to Specified address if 1st 32bit register is 0 (CMP compare was equal)					 	                //
	// 3 JG  <PADDRESS>					| Jumps to Specified address if 1st 32bit register is less than 0 (CMP compare, 2nd value greater)                  //
	// 4 JL  <PADDRESS>					| Jumps to Specified address if 1st 32bit register is more than 0 (CMP compare, 2nd value smaller)                  //
	// 5 JMP <PADDRESS>					| Jumps to Specified address																		                //
	// 6 LOG <STRING>					| Prints out a String or Value																		                //
	// 7 ADD <ADDRESS> <ADDRESS>		| Adds a value from 2nd address to 1st (Result in 2nd 32bit register)								                //
	// 8 SUB <ADDRESS> <ADDRESS>		| Subtracts value of 2nd address from 1st (Result in 2nd 32bit register)							                //
	// 9 DIV <ADDRESS> <ADDRESS>		| Divides value of 1st address by 2nd (Result in 2nd 32bit register)								                //
	// 10 MUL <ADDRESS> <ADDRESS>		| Multiplies values of 1st and 2nd address (Result in 2nd 32bit register)							                //
	// 11 GET <ADDRESS> <ADDRESS>		| Gets a byte from static data and stores it to specified address									                //
	// 12 CPR <NREGISTER> <ADDRESS>		| Copies the value from a Register to a place in Memory												                //
	// 13 CPM <ADDRESS>	<ADDRESS>		| Copies a value from memory to another place in memory                                            				    //
	// 14 PGET <ADDRESS> <ADDRESS>      | Gets a byte from static data and stores it to specified address. The address for the data is specified in memory  //
	// 15 PCPM <ADDRESS> <ADDRESS>		| Copies a value from memory to another place in Memory. (POINTED VERSION)											//
	// 34 CAL  <PADDRESS>				| Calls the subroutine specified with the parameter
	// 35 RET							| Returns from a subroutine to the last address on the call_stack
	// 36 SREG <REGISTER> <VALUE>		| Loads a value onto the specified register
	// 37 POP  <ADDRESS>           		| Pops a value from the program stack to a place in memory
	// 38 PEEK <ADDRESS>		        | Peeks the value from the program stack to a place in memory
	// 39 PUSH <ADDRESS>		        | Pushes a value onto the program from a place in memory
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////
	// LANGUAGE PROCESSOR INTERNAL INSTRUCTIONS //
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// 253 SREG <NREGISTER> <VALUE>		| Sets the value of specified register							 //
	// 254 SYS_ERR <VALUE>				| Throws a system error according to the specified id.			 //
	// 255 STOP <VALUE>                 | Stops the program, is also automatically placed at end of file //								                //
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public LangProcessor() {}

	/**
	 * @param data The Static-Data block
	 * @param program The Program-Data (Instructions &amp; Parameters)
	 * @param programFile The path to the programFile for exceptions and saveStates
	 * */
	public LangProcessor(byte[] data, byte[] program, String programFile) {
		this.programFile = programFile;
		this.MEMORY = new int[1024];
		this.data = data;
		this.program = program;
		this.call_stack = new Stack<>();
		this.prgm_stack = new Stack<>();

		for (int i = Main.args.length-1; i >= 0; i--) {
			String s = Main.args[i];
			char[] cs = s.toCharArray();
			for (int j = cs.length-1; j >= 0; j--) {
				char c = cs[j];
				this.prgm_stack.push(((byte) c) & 0xFF);
			}
		}
	}

	/**
	 * Executes the next instruction of the program.
	 * */
	public void step() {
		try {
			byte instruction = program[programCounter];

			if (debugMode) {
				if (programCounter-lastPc > 1)
					SystemLogger.debugf("Skipped %s bytes in program;\n", programCounter-lastPc);

				SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
						String.format("%08x", programCounter),
						String.format("%08x", instruction),
						INSTRUCTION_MAP.getOrDefault(instruction, "?"));

				AIADebugHelper.memoryAccessDebug(instruction, this);
			}

			lastPc = programCounter;

			switch (DataHelper.byteToInt(instruction)) {
				case 0: // 0 SET
					MEMORY[getNextInt(0)] = getNextInt(4);
					programCounter += 9;
					break;
				case 1: // 1 CMP
					REGS[0] = MEMORY[getNextInt(0)] - MEMORY[getNextInt(4)];
					programCounter += 9;
					break;
				case 2: // 2 JE
					if (REGS[0] == 0) {
						programCounter = getNextInt(0);
						if (debugMode) {
							SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
									String.format("%08x", programCounter),
									String.format("%02x", instruction),
									INSTRUCTION_MAP.getOrDefault(instruction, "?"));
							SystemLogger.debugf("JE: Jumping to %s\n", programCounter);
						}
					} else {
						programCounter += 5;
					}
					break;
				case 3: // 3 JG
					if (REGS[0] > 0) {
						programCounter = getNextInt(0);
						if (debugMode) {
							SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
									String.format("%08x", programCounter),
									String.format("%02x", instruction),
									INSTRUCTION_MAP.getOrDefault(instruction, "?"));
							SystemLogger.debugf("JG: Jumping to %s\n", programCounter);
						}
					} else {
						programCounter += 5;
					}
					break;
				case 4: // 4 JL
					if (REGS[0] < 0) {
						programCounter = getNextInt(0);
						if (debugMode) {
							SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
									String.format("%08x", programCounter),
									String.format("%02x", instruction),
									INSTRUCTION_MAP.getOrDefault(instruction, "?"));
							SystemLogger.debugf("JL: Jumping to %s\n", programCounter);
						}
					} else {
						programCounter += 5;
					}
					break;
				case 5: // 5 JMP
					programCounter = getNextInt(0);
					if (debugMode) {
						SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
								String.format("%08x", programCounter),
								String.format("%02x", instruction),
								INSTRUCTION_MAP.getOrDefault(instruction, "?"));
						SystemLogger.debugf("JMP: Jumping to %s\n", programCounter);
					}
					break;
				case 6: // 6 LOG
					if (program[programCounter+1] != 34) {
						outStream.print(program[programCounter+1] == 0 ? String.valueOf((MEMORY[getNextInt(1)])) : (char) MEMORY[getNextInt(1)]);
						programCounter += 6;
					} else {
						String out = getStringFromPD(programCounter, false);
						programCounter += out.length()+3;
						outStream.print(out);
					}
					break;
				case 7: // ADD
					REGS[1] = MEMORY[getNextInt(0)] + MEMORY[getNextInt(4)];
					programCounter += 9;
					break; // SUB
				case 8:
					REGS[1] = MEMORY[getNextInt(0)] - MEMORY[getNextInt(4)];
					programCounter += 9;
					break;
				case 9: // DIV
					REGS[1] = MEMORY[getNextInt(0)] / MEMORY[getNextInt(4)];
					programCounter += 9;
					break;
				case 10: // MUL
					REGS[1] = MEMORY[getNextInt(0)] * MEMORY[getNextInt(4)];
					programCounter += 9;
					break;
				case 11: // GET
					MEMORY[getNextInt(4)] = data[getNextInt(0)] & 0xFF;
					programCounter += 9;
					break;
				case 12: // CPR
					int reg = getNextInt(0);
					if (reg > 15) {
						saveException(new InvalidRegisterException(instruction, new byte[] {program[programCounter+1], program[programCounter+2], program[programCounter+3], program[programCounter+4], program[programCounter+5], program[programCounter+6], program[programCounter+7] ,program[programCounter+8]}).throwException(reg));
					} else {
						MEMORY[getNextInt(4)] = REGS[reg];
					}
					programCounter += 9;
					break;
				case 13: // CPM
					MEMORY[getNextInt(4)] = MEMORY[getNextInt(0)];
					programCounter += 9;
					break;
				case 14: // PGET
					MEMORY[getNextInt(4)] = data[MEMORY[getNextInt(0)]] & 0xFF;
					programCounter += 9;
					break;
				case 15: // PCPM
					MEMORY[MEMORY[getNextInt(4)]] = MEMORY[MEMORY[getNextInt(0)]];
					programCounter += 9;
					break;
				case 34: // CAL
					int destination = getNextInt(0);
					call_stack.push(programCounter+5);
					programCounter = destination;
					break;
				case 35: // RET
					if (call_stack.isEmpty()) {
						this.isFinished = true;
						return;
					}
					programCounter = call_stack.pop();
					break;
				case 36: //SREG
					if (program[programCounter+1] == 0) {
						REGS[program[programCounter+2]] = MEMORY[getNextInt(2)];
					} else {
						REGS[program[programCounter+2]] = getNextInt(2);
					}
					programCounter += 7;
					break;
				case 37: // POP
					if (program[programCounter+1] == 0) {
						MEMORY[getNextInt(1)] = prgm_stack.pop();
					} else if (program[programCounter+1] == 2) {
						REGS[getNextInt(1)] = prgm_stack.pop();
					} else {
						MEMORY[MEMORY[getNextInt(1)]] = prgm_stack.pop();
					}
					programCounter += 6;
					break;
				case 38: // PEEK
					if (program[programCounter+1] == 0) {
						MEMORY[getNextInt(1)] = prgm_stack.peek();
					} else if (program[programCounter+1] == 2) {
						REGS[getNextInt(1)] = prgm_stack.peek();
					} else {
						MEMORY[MEMORY[getNextInt(1)]] = prgm_stack.peek();
					}
					programCounter += 6;
					break;
				case 39: // PUSH
					if (program[programCounter+1] == 0) {
						prgm_stack.push(MEMORY[getNextInt(1)]);
					} else if (program[programCounter+1] == 2) {
						prgm_stack.push(REGS[getNextInt(1)]);
					} else {
						prgm_stack.push(MEMORY[MEMORY[getNextInt(1)]]);
					}
					programCounter += 6;
					break;
				case 252: // NCL CALL HANDLING
					programCounter += NativeCodeLinker.call();
					break;
				case 254: // SYS_ERR
					String msg = getStringFromPD(programCounter+4, false);
					int len = msg.length()+2;
					Main.AIA_RUNTIME.throwError(getNextInt(0), msg, program[programCounter+4+len] > 0);
					programCounter += 6+len;
					break;
				case 255: // STOP
					this.isFinished = true;
					return;
				default:
					int incrementor = STDIO.process(instruction, this);

					if (enableNIS && incrementor == -1) {
						incrementor = NIS.process(instruction, program, programCounter, data, MEMORY, REGS);
					}
					if (incrementor < 0) incrementor = BIS.process(instruction);
					if (incrementor < 0) {
						saveException(new InvalidInstructionException(instruction, new byte[] {0x0}).throwException());
						programCounter++;
					}

					programCounter += incrementor;
					break;
			}
		} catch (IndexOutOfBoundsException e) {
			lastException = e;
			if (programCounter >= program.length) {
				saveException(new InvalidProgramCounterException((byte) 0xff, new byte[] {0x0}).throwException());
			} else {
				saveException(new OutOfBoundsException(program[programCounter], new byte[] {0x0}).throwException());
			}
			isFinished = true;

			if (printJavaExceptions) {
				e.printStackTrace();
			}
		} catch (NoSuchElementException e) {
			lastException = e;
			saveException(new NotFoundException(program[programCounter], new byte[] {0x0}, "No String was Found!").throwException());
			if (printJavaExceptions) e.printStackTrace();
			programCounter++;
		}
	}

	/**
	 * Retrieves a String from Program Data.
	 *
	 * @param start The starting point of the string. If the value of the byte at this point is
	 * not the char-code of double-quotes. It will search forward until it finds this value.
	 * @param includeQuotes Sets whether or not the double-quotes should be included in the string.
	 * @return The String retrieved from Program Data
	 * @throws NoSuchElementException Thrown if there are no double quotes found.
	 * */
	public String getStringFromPD(int start, boolean includeQuotes) throws NoSuchElementException {
		String str = "";

		boolean inString = program[start] == 0x22;
		do { // Search for the start of the string if not already at it.
			start++;
			if (program.length == start) {
				throw new NoSuchElementException("No Start of String was found in program data!");
			}
			if (program[start] == 0x22) inString = true;
		} while (!inString);

		// Add the beginning quotes if required.
		if (includeQuotes) str += (char) 0x22;
		start++;

		// Assemble the String
		StringBuilder strBuilder = new StringBuilder(str);
		for (int i = start; i < program.length; i++) {
			char cchar = (char) program[i];
			if (cchar == 0x0) {
				if (includeQuotes) strBuilder.append("\"");
				break;
			}

			strBuilder.append(cchar);
		}
		str = strBuilder.toString();

		return str;
	}

	/**
	 * Builds and returns an integer from the next 4 bytes in program-data
	 * @param offset Offset to the next 4 bytes
	 * @return The created integer
	 * */
	public int getNextInt(int offset) {
		return DataHelper.createInt(new byte[] {
				program[programCounter+1+offset],
				program[programCounter+2+offset],
				program[programCounter+3+offset],
				program[programCounter+4+offset]});
	}

	/**
	 * Sets the Memory Size in MU (1 MU = 4 Bytes)
	 *
	 * @param ms The Memory size (if less than 1 the default of 128 MU is applied)
	 * */
	public void setMemSize(int ms) {
		if (ms < 1) {
			SystemLogger.errln("[0x000001/IMS] Invalid Memory Size! Minimum is 1 MU!");
			this.MEMORY = new int[128];
			return;
		}
		this.MEMORY = new int[ms];
	}

	public void setNISEnabled(boolean enableNIS) {
		this.enableNIS = enableNIS;
	}

	@SuppressWarnings("unused")
	public void setOutputStream(PrintStream stream) {
		this.outStream = stream;
	}

	public void setPrintJavaExceptions(boolean enable) {
		this.printJavaExceptions = enable;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
		if (debugMode) SystemLogger.warnln("Enabled Debug-Mode will make any exception destructive!");
	}

	/**
	 * Saves an exception to the programErrors list for saving in
	 * a saveState file.
	 * @param ex The {@link de.gansgruppe.aia4.runtime.exceptions.AIAException AIAException} object
	 * */
	public void saveException(AIAException ex) {
		// Add the error message to the programErrors list
		String[] lines = ex.getMessageLines();
		programErrors.addAll(Arrays.asList(lines));

		if (debugMode) {
			if (printJavaExceptions && lastException != null) lastException.printStackTrace();
			SystemLogger.infoln("System in debug mode, exiting...");
			System.exit(1);
		}

		try {
			if (ERR_DESTRUCTIVE_CNF.get(ex.getID())) {
				isFinished = true;
			}
		} catch (Exception e) {
			SystemLogger.warnf("Invalid/Unregistered Exception-Id ("+String.format("%06x", ex.getID())+") ; Exiting by default...\n");
			isFinished = true;
		}
	}

	public boolean isFinished() {
		return isFinished;
	}
}
