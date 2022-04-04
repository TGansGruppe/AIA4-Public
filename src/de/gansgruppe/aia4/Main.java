package de.gansgruppe.aia4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import de.gansgruppe.aia4.compiler.stdpc.BISPreCompiler;
import de.gansgruppe.aia4.compiler.stdpc.NISPreCompiler;
import de.gansgruppe.aia4.compiler.stdpc.STDIOPreCompiler;
import de.gansgruppe.aia4.runtime.AIARunnable;
import de.gansgruppe.aia4.runtime.LangProcessor;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.SystemLogger;

public class Main {
	private static Exception LAST_EXCEPTION = null;
	public static AIARunnable AIA_RUNTIME;
	public static String SAVE_STATE_FILE;
	public static String[] args;

	private static String aiavers = "4.1 r27-12-2021";
	private static String version = String.format("AIA R%s Program Interpreter 0.4.5 (2021-pr1)", aiavers);
	
	// ARGS: -f datatest.bin -nis -ms 2048 -saveSate langproc_debug_log.txt -pje -debug -sl
	public static void main(String[] args) {
		Main.args = args;
		for (String s : args) {
			if (s.matches("-sl")) {
				SystemLogger.loggerEnabled = true;
				break;
			}
		}
		
		SystemLogger.msgf("AIA REVISION %s V1 PR ; DESIGNED & IMPLEMENTED BY GANSGRUPPE; (C) 2021 GansGruppe\n", aiavers);
		SystemLogger.msgln("AIA LangProcessor using AIA IS v1.1");
		
		// Pre-Program Checks
		if (args.length == 0) {
			giveHelp();
			System.exit(0);
		}
		
		int MEM_SIZE = -1;
		String file = null;
		boolean enableNIS = false;
		boolean pje = false;
		boolean debugMode = false;
		boolean visualise = false;
		String saveState = null;
		int sTime = 0;

		int i = 0;

		// Parse Arguments
		for (i = 0; i < args.length; i++) {
			// File for Execution
			if (args[i].matches("-f")) {
				if (args[i+1].startsWith("\"")) {
					file = DataHelper.assembleStringFromArray(i, args, "\"", true);
				} else {
					file = args[i+1];
				}
				i += file.split(" ").length;
				break;
				// Enable Network Instruction Set
			} else if (args[i].matches("-nis")) {
				enableNIS = true;
				continue;
				// Set Memory Size
			} else if (args[i].matches("-ms")) {
				MEM_SIZE = Integer.parseInt(args[i + 1]);
				continue;
			} else if (args[i].matches("-v")) {
				System.out.println(version);
				System.exit(0);
			} else if (args[i].matches("-saveState")) {
				saveState = args[i + 1];
				i++;
				continue;
			} else if (args[i].matches("-pje")) {
				pje = true;
				continue;
			} else if (args[i].matches("-instructions")) {
				listInstructions();
				System.exit(0);
			} else if (args[i].matches("-debug")) {
				debugMode = true;
				continue;
			} else if (args[i].matches("-visualise")) {
				visualise = true;
				continue;
			} else if (args[i].matches("-stime")) {
				sTime = Integer.parseInt(args[i + 1]);
				i++;
				continue;
			}
		}
		SAVE_STATE_FILE = saveState;
		if (args.length-i > 0) {
			Main.args = new String[args.length - i];

			for (i = i; i < args.length; i++) {
				Main.args[args.length-i-1] = args[i];
			}
		} else {
			Main.args = new String[] {" "};
		}

		if (file == null) {
			SystemLogger.errln("MISSING FILE FOR EXECUTION (\"-f <filename>\")");
			System.exit(-1);
		}
		
		SystemLogger.msgln("File = "+file);
		SystemLogger.msgln("NIS  = " + enableNIS);
		if (MEM_SIZE != -1) SystemLogger.msgln("MEM_SIZE = " + MEM_SIZE);
		
		if (debugMode) {
			SystemLogger.msgln("Debug-Mode enabled!");
		}
		
		// Program Loading and parsing

		byte[] raw = openFile(file);
		if (raw == null) {
			SystemLogger.errf("%s: File \"%s\" not found!", LAST_EXCEPTION.toString().split(":")[0], file);
			System.exit(-1);
		}

		try {
			raw = DataHelper.decompressBytes(raw);
		} catch (IOException e) {
			SystemLogger.infoln("Failed to decompress program, assuming program is not compressed.");
		}
		
		// Read raw program bytes into lists
		
		/////////////////////////////////////////
		// BYTE 8 = IS DATA SEGMENT OR PROGRAM //
		//					0x0			0x1	   //
		/////////////////////////////////////////
		
		ArrayList<Byte> PRGRM = new ArrayList<>();
		ArrayList<Byte> DATA = new ArrayList<>();
		boolean readingDataBlock = true;
		int segmentIndex = 0; // Byte Index in current Data Segment
		int totalSegments = 0;

		for (i = 0; i < raw.length; i++) {
			if (readingDataBlock) {
				// The byte at Segment index 8 determines 
				// if the following bytes is program data or another segment
				if (segmentIndex == 8) {
					totalSegments++;
					if (raw[i] == 0) {
						segmentIndex = 0;
						continue;
					} else {
						readingDataBlock = false;
						continue;
					}
				}
				
				DATA.add(raw[i]);
				segmentIndex++;
			} else {
				PRGRM.add(raw[i]);
			}
		}

		boolean[] boolargs = new boolean[] {
				enableNIS,
				pje,
				debugMode,
				visualise,
				PRGRM.get(0) == 0x01
		};

		if (boolargs[4]) {
			AIARunnable.LANGUAGE_PRCCESSOR_ADDRESS_LENGTH = PRGRM.get(1);
			PRGRM.remove(0);
		}
		PRGRM.remove(0);

		AIA_RUNTIME = new AIARunnable();
		AIA_RUNTIME.rawDataBlockSize = DATA.size()+totalSegments;
		AIA_RUNTIME.start(file, DataHelper.toByteArray(PRGRM.toArray()), DataHelper.toByteArray(DATA.toArray()), MEM_SIZE, boolargs, sTime);

		// Wait for Program to Complete
		while (AIA_RUNTIME.isAlive()) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
		
		if (saveState != null)
			AIA_RUNTIME.saveState(saveState);
	}
	
	private static void listInstructions() {
		System.out.println("Instruction List");
		System.out.printf("Color Formatting: MIS/IIS - BLACK ; %s STDIO - GREEN %s ; %s NIS - YELLOW %s ; %s BIS - BLUE %s ; %s IIS - RED %s\n\n",
				SystemLogger.ANSIColor.GREEN, SystemLogger.ANSIColor.RESET, SystemLogger.ANSIColor.YELLOW, SystemLogger.ANSIColor.RESET, SystemLogger.ANSIColor.BLUE, SystemLogger.ANSIColor.RESET, SystemLogger.ANSIColor.RED, SystemLogger.ANSIColor.RESET);
		
		String[] instructions = DataHelper.toStringArray(LangProcessor.INSTRUCTION_MAP.values().toArray());
		byte[] icodes = DataHelper.toByteArray(LangProcessor.INSTRUCTION_MAP.keySet().toArray());
		for (int i = 0; i < instructions.length-1; i++) {
			System.out.printf("%s: %s\n", icodes[i], instructions[i]);
		}
		
		System.out.print(SystemLogger.ANSIColor.GREEN);
		instructions = DataHelper.toStringArray(STDIOPreCompiler.INSTRUCTION_MAP.keySet().toArray());
		byte[] bicodes = DataHelper.toByteArray(STDIOPreCompiler.INSTRUCTION_MAP.values().toArray());
		for (int i = 0; i < instructions.length; i++) {
			System.out.printf("%s: %s\n", bicodes[i], instructions[i]);
		}
		System.out.print(SystemLogger.ANSIColor.RESET);

		System.out.print(SystemLogger.ANSIColor.YELLOW);
		instructions = DataHelper.toStringArray(NISPreCompiler.INSTRUCTION_MAP.keySet().toArray());
		bicodes = DataHelper.toByteArray(NISPreCompiler.INSTRUCTION_MAP.values().toArray());
		for (int i = 0; i < instructions.length; i++) {
			System.out.printf("%s: %s\n", bicodes[i], instructions[i]);
		}
		System.out.print(SystemLogger.ANSIColor.RESET);

		System.out.print(SystemLogger.ANSIColor.BLUE);
		instructions = DataHelper.toStringArray(BISPreCompiler.INSTRUCTION_MAP.keySet().toArray());
		bicodes = DataHelper.toByteArray(BISPreCompiler.INSTRUCTION_MAP.values().toArray());
		for (int i = 0; i < instructions.length; i++) {
			System.out.printf("%s: %s\n", bicodes[i], instructions[i]);
		}
		System.out.print(SystemLogger.ANSIColor.RED);
		System.out.println("255: STOP");
		System.out.print(SystemLogger.ANSIColor.RESET);
	}

	public static byte[] openFile(String filename) {
		try {
			return Files.readAllBytes(Paths.get(filename));
		} catch (IOException e) {
			LAST_EXCEPTION = e;
			return null;
		}
	}
	
	private static void giveHelp() {
		 System.out.println("AIA 4 RUNTIME ARGUMENTS: ");
		 System.out.println();
		 System.out.println("-f <file>          | Specify the program for execution");
		 System.out.println("-nis               | Enable the Network Instruction Set");
		 System.out.println("-ms <int>          | Specify the LangProcessor Memory Size");
		 System.out.println("-v                 | Gives the current version");
		 System.out.println("-saveState <file>  | Writes a file containing all values of the LangProcessor");
		 System.out.println("-pje               | Enables the printing of Java Exceptions");
		 System.out.println("-debug             | Enables program debug mode");
		 System.out.println("-sl                | Enables the SystemLogger");
		 System.out.println("-visualise         | Enables the AIA4 Visualiser");
	}
}
