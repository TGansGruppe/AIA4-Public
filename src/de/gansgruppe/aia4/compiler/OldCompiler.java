package de.gansgruppe.aia4.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import de.gansgruppe.aia4.util.CLST;
import de.gansgruppe.aia4.util.DataHelper;

/**
 * A basic, non-optimized compiler for AIA 4.
 * It does not optimize code, rather it's purpose
 * is to translate the source code in to binary code
 * so it behaves just like it is coded to.
 * 
 * @author 0x1905
 * @deprecated
 * */
@Deprecated
public class OldCompiler {
	private CLST pcFiles;
	private IPreCompiler[] precompilers;
	
	// Create & assign standard instructions
	public static HashMap<String, Byte> STD_INSTRUCTION_MAP = new HashMap<>();
	static {
		STD_INSTRUCTION_MAP.put("SET",  (byte) 0);
		STD_INSTRUCTION_MAP.put("CMP",  (byte) 1);
		STD_INSTRUCTION_MAP.put("JE",   (byte) 2);
		STD_INSTRUCTION_MAP.put("JG",   (byte) 3);
		STD_INSTRUCTION_MAP.put("JL",   (byte) 4);
		STD_INSTRUCTION_MAP.put("JMP",  (byte) 5);
		STD_INSTRUCTION_MAP.put("LOG",  (byte) 6);
		STD_INSTRUCTION_MAP.put("ADD",  (byte) 7);
		STD_INSTRUCTION_MAP.put("SUB",  (byte) 8);
		STD_INSTRUCTION_MAP.put("DIV",  (byte) 9);
		STD_INSTRUCTION_MAP.put("MUL",  (byte) 10);
		STD_INSTRUCTION_MAP.put("GET",  (byte) 11);
		STD_INSTRUCTION_MAP.put("CPR",  (byte) 12);
		STD_INSTRUCTION_MAP.put("CPM",  (byte) 13);
		STD_INSTRUCTION_MAP.put("PGET", (byte) 14);
		STD_INSTRUCTION_MAP.put("PCPM", (byte) 15);
		STD_INSTRUCTION_MAP.put("STOP", (byte) 255);
	}
	
	/**
	 * @param precompilers A list of .class files implementing the IPreCompiler
	 * */
	public OldCompiler(CLST precompilers) {
		this.pcFiles = precompilers;
		this.precompilers = new IPreCompiler[pcFiles.getColumns().size()];
	}
	
	public void init() {
		// Load Precompiler Classes
		try {
			ClassLoader cl = null;
			for (int i = 0; i < precompilers.length; i++) {
			    URL url = new File(pcFiles.getEntry(i, 0)).toURI().toURL();
			    URL[] urls = new URL[]{url};
			    cl = new URLClassLoader(urls);
			    precompilers[i] = (IPreCompiler) cl.loadClass(pcFiles.getEntry(i, 1)).newInstance();
			}
		} catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Fully compiles a file into bytecode.
	 * 
	 * @param file The file to be compiled
	 * @return An array of bytes contatining the bytecode
	 * */
	public byte[] compileFile(String file, String dFile) {
		String[] lines = readFile(file).replaceAll("\t", " ").split("\r\n");
		ArrayList<String> ncWords = new ArrayList<>();
		
		//Remove Comments and Clean Code
		for (int i = 0; i < lines.length; i++) {
			String[] lsplit = lines[i].split(" ");
			
			for (int j = 0; j < lsplit.length; j++) {
				String word = lsplit[j].replaceAll(" ", "");
				if (word.startsWith(";")) {
					break;
				} else if (!word.matches("")) {
					ncWords.add(word);
				}
			}
		}
		String[] words = DataHelper.toStringArray(ncWords.toArray());
		
		// Precompile
		for (IPreCompiler pc : precompilers) {
			words = pc.compile(words);
		}
		// This list is used for storing modified source code before bytecode translation
		ArrayList<String> tmpJBCompile = new ArrayList<>();

		// This list is used to hold all program data during bytecode translation
		ArrayList<Byte> tmpCompile = new ArrayList<>();
		
		boolean inString = false;
		
		if (dFile != null) {
			stichDataFile(dFile, tmpCompile);
		}
		
		//////////////////////////////////////////////
		// Compilation Steps:						//
		// 0. Execute Compiler Instructions			//
		// 1. Replace Labels						//
		// 2. Write Bytecode					    //
		// 2.1 Translate Hex-Codes into Binary		//
		// 2.2 Translate Instruction Codes into		//
		//     Binary								//
		//////////////////////////////////////////////

		// 0. Execute OldCompiler Instructions
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word.startsWith("\"")) {
				inString = true;
			}
			if (word.endsWith("\"")) {
				inString = false;
			}
			
			if (word.startsWith("@") && !inString) {
				if (word.matches("@lnk_dat")) {
					String datFile = words[i+1];
					String path = new StringBuilder(file).reverse().toString();
					
					String[] pathSplit = path.split("/");
					path = "";
					for (int k = 1; k < pathSplit.length; k++) {
						if (k != 1) {
							path += "/";
						}
						path += pathSplit[k];
					}
					path = new StringBuilder(path).reverse().toString();
					datFile = path+"/"+datFile;
					
					stichDataFile(datFile, tmpCompile);
				}
				i++;
				continue;
			}
			tmpJBCompile.add(word);
		}
		words = DataHelper.toStringArray(tmpJBCompile.toArray());
		tmpJBCompile.clear();
		
		/* Replace Labels
		 * Based of Felix Eckert's SIA compiler.
		 * SEE: https://github.com/FelixEcker/Simple-Interpreted-Assembler/blob/main/src/de/felixeckert/sia/compiler/Compiler.java#L50
		 * */
		
		HashMap<String, Integer> label_map = new HashMap<>();
		tmpCompile.add((byte) 0x1);
		int labelOffset = 0;
		int strLblSubOff = 0;
		boolean writingNCLCall = false;
		// Translate into Bytecode
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			
			if (word.startsWith("!")) {
				writingNCLCall = true;
				tmpCompile.add((byte) 0xfc);
				word = word.substring(1);
				labelOffset++;
			}
			
			if (writingNCLCall) {
				char[] chars = word.toCharArray();
				for (int j = 0; j < chars.length; j++) {
					if (chars[j] == '\"') inString = !inString;
					if (chars[j] == '\\' && !inString) {
						writingNCLCall = false;
						tmpCompile.add((byte) 0x0);
						labelOffset++;
						break;
					}
					tmpCompile.add((byte) chars[j]);
					labelOffset++;
				}
				if (writingNCLCall) tmpCompile.add((byte) ' ');
				continue;
			}
			
			if (word.startsWith("\"")) {
				inString = true;
				labelOffset--;
				for (int j = i; j < words.length; j++) {
					String subWord = words[j];
					for (char c : subWord.toCharArray()) {
						tmpCompile.add((byte) c);
						//System.out.println("CHR: ("+c+") os = "+labelOffset+" ; new = "+(labelOffset+1));
						labelOffset++;
					}
					
					// Check if String is terminated in this word
					if (subWord.endsWith("\"")) {
						inString = false;
						break;
					} else {
						tmpCompile.add((byte) ' ');
					}
					//System.out.println("strLblSubOff = "+strLblSubOff+" ; word = "+subWord+" ; new = "+(strLblSubOff+1));
					strLblSubOff++;
					i += (j-i)+1;
				}
				continue;
			}
			
			if (word.startsWith("$") || word.startsWith("0x")) {
				int val = Integer.parseInt(word.replace("$", "").replace("0x", ""), 16);
				byte[] bytes = ByteBuffer.allocate(4).putInt(val).array();
				for (byte b : bytes) {
					tmpCompile.add(b);
				}
				//System.out.println("VAL: os = "+labelOffset+" ; new = "+(labelOffset+4));
				labelOffset += 4;
				continue;
			}
			
			if (word.startsWith(":") && !inString) {
				//System.out.println(labelOffset);
				//System.out.println(strLblSubOff);
				//System.out.println(i);
				//System.out.println(labelOffset+(i-strLblSubOff));
				label_map.put(word.replace(":", ""), labelOffset+(i-strLblSubOff));
				continue;
			}
			
			if (word.startsWith("_") && !inString) {
				if (!label_map.containsKey(word.replaceAll("_", ""))) {
					System.err.println("COMPILATION ERROR: LABEL NOT DEFINED! (0x000001)");
					System.err.printf("$%s : %s\n", i, word);
					System.err.println("LABEL NOT IN LABEL_MAP");
				}
				
				byte[] bytes = ByteBuffer.allocate(4).putInt(label_map.get(word.replaceAll("_", ""))).array();
				for (byte b : bytes) {
					tmpCompile.add(b);
				}
				labelOffset += 4;
				continue;
			}
			
			if (word.startsWith("BYTE=")) {
				BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(word.split("=")[1], 10));
				byte[] bytearray = (bigInt.toByteArray());
				tmpCompile.add((byte) bytearray[bytearray.length-1]);
				continue;
			}
			
			if (STD_INSTRUCTION_MAP.containsKey(word.toUpperCase())) {
				tmpCompile.add(STD_INSTRUCTION_MAP.get(word.toUpperCase()));
				
				if (word.matches("log")) {
					if (!words[i+1].startsWith("\"")) {
						tmpCompile.add((byte) 0x0);
						//System.out.println("LOG: os = "+labelOffset+" ; new = "+(labelOffset+1));
						labelOffset++;
					}
				}
			} else {
				System.err.println("COMPILATION ERROR: INVALID INSTRUCTION (0x000000):");
				System.err.printf("$%s : %s\n", i, word);
				System.err.println("NO INSTRUCTION FOUND");
				System.exit(0);
			}
		}
		
		System.out.println("Compilation finished");
		/*
		System.out.println("\n===================");
		System.out.println("debug information: ");
		System.out.println("_lo: "+_labelOffset);
		System.out.println("slo: "+strLblSubOff);
		System.out.println("__i: "+__i);
		*/
		return DataHelper.toByteArray(tmpCompile.toArray());
	}
	
	private void stichDataFile(String file, ArrayList<Byte> tmpCompile) {
		byte[] cont = readBinFile(file.replace("\"", ""));
		for (byte b : cont) {
			tmpCompile.add(b);
		}
	}
	
	private byte[] readBinFile(String filename) {
		try {
			return Files.readAllBytes(Paths.get(filename));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String readFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return "STOP";
	}	
	
	public static void writeBinaryFile(String output, byte[] bin) {
		try {
			OutputStream os = new FileOutputStream(output);
			os.write(bin);	
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////
	
	public static void main(String[] args) {
		// -i <file> | Input file
		// -o <file> | output file
		// -d <file> | data file
		// -pcl <file> | Precompiler list
		
		String inputFile = "";
		String outputFile = "";
		String dFile = null;
		String clst  = "";
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.matches("-i")) {
				inputFile = args[i+1];
				i++;
				continue;
			} else if (arg.matches("-o")) {
				outputFile = args[i+1];
				i++;
				continue;
			} else if (arg.matches("-pcl")) {
				clst = args[i+1];
				i++;
				continue;
			} else if (arg.matches("-d")) {
				dFile = args[i+1];
				i++;
				continue;
			}
		}
		
		OldCompiler c = new OldCompiler(new CLST(clst));
		c.init();
		writeBinaryFile(outputFile, c.compileFile(inputFile, dFile));
	}
}
