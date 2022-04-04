package de.gansgruppe.aia4.runtime.eisp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Scanner;

import de.gansgruppe.aia4.runtime.LangProcessor;
import de.gansgruppe.aia4.runtime.exceptions.OutOfBoundsException;
import de.gansgruppe.aia4.runtime.exceptions.OutOfMemoryException;
import de.gansgruppe.aia4.util.SystemLogger;

public class STDIO {
	// TODO(alan): Implement according to new STDIO-IS rev. (20-09-2021)
	// new revision not in AIA4 Spec.
	
	//
	// 0x10 STD_IN <address>
	// 0x11 FL_OPN <string>
	// 0x12 FL_WRT (bool:existing) <handle>
	// 0x13 FL_CLS <handle>
	// 0x28 FL_CRT <start_address> <end_address>
	//

	public static HashMap<Integer, FileObject> FILES = new HashMap<>();
	
	public static int process(byte inst, LangProcessor langProc) {
		switch (inst) {
		case 0x10:
			return stdinInst(langProc);
		case 0x11:
			return openFile(langProc);
		default:
			return -1;
		}
		
		//return 0;
	}
	
	private static int openFile(LangProcessor langProc) {
		FileObject file;

		String path;
		byte[] data;
		int file_handle_internal = -1;
		
		// Address Calculation
		// data.length / 4 = MU_SIZE
		// MEM_SIZE - MU_SIZE = ADDRESS
		int address;
		
		// Retrieve Address from program data
		
		
		return 0;
	}

	public static int stdinInst(LangProcessor langProc) {
		try {
			Scanner scn = new Scanner(System.in);
			String in = scn.nextLine();
			
			// Simply write all input bytes to memory
			int address = langProc.getNextInt(0);
			char[] bytes = in.toCharArray();
			
			// Check if data fits in Memory
			if (bytes.length + address > langProc.MEMORY.length) {
				langProc.saveException(new OutOfMemoryException(langProc.program[langProc.programCounter], new byte[] {langProc.program[langProc.programCounter+1], langProc.program[langProc.programCounter+2],
						langProc.program[langProc.programCounter+3], langProc.program[langProc.programCounter+4]}).throwException("STD_IN"));
			}

			// Write data to memory
			for (int i = 0; i < bytes.length; i++) {
				if (address+i >= langProc.MEMORY.length) {
					SystemLogger.warnln("STD_IN attempted to write data, which does not fit into memory");
					SystemLogger.warnln(String.format("Clipped data to fit into memory! (dlength=%s ; dpointer=%s ; ms=%s)", bytes.length, address, langProc.MEMORY.length));
					break;
				}
				langProc.MEMORY[address+i] = (byte) bytes[i];
			}
		} catch (IndexOutOfBoundsException e) {
			langProc.saveException(new OutOfBoundsException(langProc.program[langProc.programCounter], 
					new byte[] {langProc.program[langProc.programCounter+1], langProc.program[langProc.programCounter+2],
							langProc.program[langProc.programCounter+3], langProc.program[langProc.programCounter+4]}).throwException());
			System.out.println(langProc.printJavaExceptions);
			if (langProc.printJavaExceptions) {
				e.printStackTrace();
			}
			return -1;
		}
		return 5;
	}
	
	/**
	 * An object used to store file data, such as:
	 * 	1. Path
	 *  2. Size
	 *  3. Location in Memory
	 * */
	@SuppressWarnings("serial")
	public class FileObject extends File {
		private int size;
		private int address;
		private int endAddress;

		public FileObject(String path, int size, int address) {
			super(path);
			
			this.size = size;
			this.address = address;
			this.endAddress = address+size;
		}
		
		public FileObject(String path, int address) {
			super(path);
			
			this.address = address;
			this.size    = -1;
		}
		
		public byte[] getContentsFromDisk() throws IOException {
			byte[] data = Files.readAllBytes(this.toPath()); 
			this.size   = data.length;
			return data;
		}
		
		public void saveContentsToDisk(byte[] contents) {
			
		}
		
		public int getAddress() {
			return this.address;
		}
		public int getEndAddress() { return this.endAddress; }
		public void setAddress(int address) {
			this.address = address;
		}
		public int getSize() {
			return this.size;
		}
		public void setSize(int size) {
			this.size = size;
		}
	}
}
