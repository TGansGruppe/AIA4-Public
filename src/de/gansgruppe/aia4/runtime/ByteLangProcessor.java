package de.gansgruppe.aia4.runtime;

import de.gansgruppe.aia4.Main;
import de.gansgruppe.aia4.runtime.eisp.BIS;
import de.gansgruppe.aia4.runtime.eisp.NIS;
import de.gansgruppe.aia4.runtime.eisp.STDIO;
import de.gansgruppe.aia4.runtime.exceptions.*;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.SystemLogger;

import java.util.NoSuchElementException;

/**
 * {@inheritDoc}
 *
 * Minimal Byte-Mode compliant LangProcessor.
 * See AIA4 Specification r27-12-2021 Appendix B Chapter 3.
 * */
public class ByteLangProcessor extends LangProcessor {
	protected int addressLength = 4;

	public ByteLangProcessor(byte[] data, byte[] program, String programFile) {
		super(data, program, programFile);
	}

	/**
	 * Executes the next instruction of the program.
	 * */
	@Override
	public void step() {
		//TODO: FORMAT COMPLIANT IMPLEMENTATION!
		//TODO: DEBUG
		try {
			byte instruction = program[programCounter];

			if (debugMode) {
				if (programCounter-lastPc > 1)
					SystemLogger.debugf("Skipped %s bytes in program;\n", programCounter-lastPc);

				SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
						String.format("%06x", programCounter),
						String.format("%06x", instruction),
						INSTRUCTION_MAP.getOrDefault((int) instruction, "?"));
			}

			super.lastPc = programCounter;

			switch (DataHelper.byteToInt(instruction)) {
				case 0: // 0 SET
					MEMORY[getNextAddress(0)] = program[programCounter];
					programCounter += 9;
					break;
				case 1: // 1 CMP
					REGS[0] = MEMORY[getNextAddress(0)] - MEMORY[getNextAddress(4)];
					programCounter += 9;
					break;
				case 2: // 2 JE
					if (REGS[0] == 0) {
						programCounter = getNextInt(0);
						if (debugMode) {
							SystemLogger.debugf("PC [$%s]: INST = 0x%s NAME = %s ;\n",
									String.format("%06x", programCounter),
									String.format("%06x", instruction),
									INSTRUCTION_MAP.getOrDefault((int) instruction, "?"));
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
									String.format("%06x", programCounter),
									String.format("%06x", instruction),
									INSTRUCTION_MAP.getOrDefault((int) instruction, "?"));
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
									String.format("%06x", programCounter),
									String.format("%06x", instruction),
									INSTRUCTION_MAP.getOrDefault((int) instruction, "?"));
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
								String.format("%06x", programCounter),
								String.format("%06x", instruction),
								INSTRUCTION_MAP.getOrDefault((int) instruction, "?"));
						SystemLogger.debugf("JMP: Jumping to %s\n", programCounter);
					}
					break;
				case 6: // 6 LOG
					if (program[programCounter+1] != 34) {
						outStream.print(program[programCounter+1] == 0 ? String.valueOf((MEMORY[getNextAddress(1)])) : (char) MEMORY[getNextAddress(1)]);
						programCounter += 6;
					} else {
						String out = getStringFromPD(programCounter, false);
						programCounter += out.length()+3;
						outStream.print(out);
					}
					break;
				case 7: // ADD
					REGS[1] = MEMORY[getNextAddress(0)] + MEMORY[getNextAddress(4)];
					programCounter += 9;
					break; // SUB
				case 8:
					REGS[1] = MEMORY[getNextAddress(0)] - MEMORY[getNextAddress(4)];
					programCounter += 9;
					break;
				case 9: // DIV
					REGS[1] = MEMORY[getNextAddress(0)] / MEMORY[getNextAddress(4)];
					programCounter += 9;
					break;
				case 10: // MUL
					REGS[1] = MEMORY[getNextAddress(0)] * MEMORY[getNextAddress(4)];
					programCounter += 9;
					break;
				case 11: // GET
					MEMORY[getNextAddress(4)] = data[getNextInt(0)] & 0xFF;
					programCounter += 9;
					break;
				case 12: // CPR
					int reg = getNextInt(0);
					if (reg > 15) {
						saveException(new InvalidRegisterException(instruction, new byte[] {program[programCounter+1], program[programCounter+2], program[programCounter+3], program[programCounter+4], program[programCounter+5], program[programCounter+6], program[programCounter+7] ,program[programCounter+8]}).throwException(reg));
					} else {
						MEMORY[getNextAddress(4)] = REGS[reg];
					}
					programCounter += 9;
					break;
				case 13: // CPM
					MEMORY[getNextAddress(4)] = MEMORY[getNextAddress(0)];
					programCounter += 9;
					break;
				case 14: // PGET
					MEMORY[getNextAddress(4)] = data[MEMORY[getNextAddress(0)]] & 0xFF;
					programCounter += 9;
					break;
				case 15: // PCPM
					MEMORY[MEMORY[getNextAddress(4)]] = MEMORY[MEMORY[getNextAddress(0)]];
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
						REGS[(int) program[programCounter+2]] = MEMORY[getNextAddress(2)];
					} else {
						REGS[(int) program[programCounter+2]] = getNextInt(2);
					}
					programCounter += 7;
					break;
				case 37: // POP
					if (program[programCounter+1] == 0) {
						MEMORY[getNextAddress(1)] = prgm_stack.pop();
					} else if (program[programCounter+1] == 2) {
						REGS[getNextAddress(1)] = prgm_stack.pop();
					} else {
						MEMORY[MEMORY[getNextAddress(1)]] = prgm_stack.pop();
					}
					programCounter += 6;
					break;
				case 38: // PEEK
					if (program[programCounter+1] == 0) {
						MEMORY[getNextAddress(1)] = prgm_stack.peek();
					} else if (program[programCounter+1] == 2) {
						REGS[getNextAddress(1)] = prgm_stack.peek();
					} else {
						MEMORY[MEMORY[getNextAddress(1)]] = prgm_stack.peek();
					}
					programCounter += 6;
					break;
				case 39: // PUSH
					if (program[programCounter+1] == 0) {
						prgm_stack.push(MEMORY[getNextAddress(1)]);
					} else if (program[programCounter+1] == 2) {
						prgm_stack.push(REGS[getNextAddress(1)]);
					} else {
						prgm_stack.push(MEMORY[MEMORY[getNextAddress(1)]]);
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
						if (incrementor < 0) incrementor = BIS.process(instruction);
						if (incrementor < 0) {
							saveException(new InvalidInstructionException(instruction, new byte[] {0x0}).throwException());
							programCounter++;
						}
					} else {
						if (incrementor < 0) incrementor = BIS.process(instruction);
						if (incrementor < 0) {
							saveException(new InvalidInstructionException(instruction, new byte[] {0x0}).throwException());
							programCounter++;
						}
					}

					programCounter += incrementor;
					break;
			}
		} catch (IndexOutOfBoundsException e) {
			lastException = e;
			if (programCounter >= program.length) {
				saveException(new InvalidProgramCounterException((byte) 0xff, new byte[] {0x0}).throwException());
				isFinished = true;
			} else {
				saveException(new OutOfBoundsException(program[programCounter], new byte[] {0x0}).throwException());
				isFinished = true;
			}

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
	 * Grabs the next Address contained within the program.
	 * Complient with addressLength.
	 * @param offset Offset for Start of Address
	 * @return The Address
	 * */
	public int getNextAddress(int offset) {
		byte[] bytes = new byte[4];
		for (int i = 4-addressLength; i < addressLength; i++) {
			bytes[i] = program[programCounter+i+offset];
		}
		return DataHelper.createInt(bytes);
	}

	/**
	 * Sets the addressLength
	 * @param length The Address Length
	 * */
	public void setAddressLength(int length) {
		this.addressLength = length;
	}
}
