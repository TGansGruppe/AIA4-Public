package de.gansgruppe.aia4.compiler;

import de.gansgruppe.aia4.util.CLST;
import de.gansgruppe.aia4.util.DataHelper;
import de.gansgruppe.aia4.util.NullStream;

import static de.gansgruppe.aia4.util.Instructions.INST_CODES;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// See AIA4 Specification Chapter 4 (09-12-2021 Revision)
/**
 * AIA4 Revised Compiler;
 * A basic, non-optimized compiler for AIA 4,
 * which does not optimize sourcecode.
 *
 * Based on the Source & Compiler Specification from
 * the AIA4 Specification (Chapter 4, 09-12-2021 Revision).
 *
 * @author 0x1905
 * @author Alan Goose
 * @since 0.3.0
 * */
public class Compiler {
    private static final String COMPILER_VERSION                = "1.0.3 (Compliant with AIA R-4 1.1 r27-12-2021)";
    public  static final String STANDARD_LIBRARY_DIRECTORY_NAME = "stdlib_src";

    public  String            sourcePath;
    public  String            source;
    public  byte[]            dFile;
    private CLST              preCompilerList;
    private IPreCompiler[]    preCompilers;

    // Used as a data block when none is specified
    private Byte[] dummyDFile = new Byte[] {
                   0x0, 0x0, 0x0,
                   0x0, 0x0, 0x0,
                   0x0, 0x0
    };

    private ArrayList<File> includedFiles;

    /**
     * Create a list of standard instructions (IIS & MIS)
     * This Map is filled with the contents of {@link de.gansgruppe.aia4.util.Instructions#INST_NAMES}
     * */
    public static HashMap<String, Byte> STD_INSTRUCTION_MAP = INST_CODES;

    // Create a lookup map of escape characters and their
    // codes. Filled in the static initializer
    public static HashMap<Character, Byte> ESCAPE_LOOKUP = new HashMap<>();
    static {
        // Fill ESCAPE_LOOKUP map
        ESCAPE_LOOKUP.put('a', (byte) 0x07);
        ESCAPE_LOOKUP.put('b', (byte) 0x08);
        ESCAPE_LOOKUP.put('t', (byte) 0x09);
        ESCAPE_LOOKUP.put('n', (byte) 0x0a);
        ESCAPE_LOOKUP.put('v', (byte) 0x0b);
        ESCAPE_LOOKUP.put('f', (byte) 0x0c);
        ESCAPE_LOOKUP.put('r', (byte) 0x0d);
        ESCAPE_LOOKUP.put('e', (byte) 0x0e);
    }

    /**
     * @param preCompilerCLST Path to the PreCompiler list
     * @param sourceFile      Path to the Source file.
     * */
    public Compiler(String preCompilerCLST, String sourceFile, String dataFile) throws IOException {
        this.sourcePath      = sourceFile;
        this.source          = new String(Files.readAllBytes(Paths.get(sourceFile)));
        this.dFile           = dFile != null ? Files.readAllBytes(Paths.get(dataFile)) : DataHelper.asPrimitiveBytes(dummyDFile);
        this.preCompilerList = preCompilerCLST.matches("") ? null : new CLST(preCompilerCLST);
        this.includedFiles   = new ArrayList<>();
    }

    /**
     * @param preCompilerList The PreCompiler CLST
     * @param source          The Source Code
     * */
    public Compiler(CLST preCompilerList, String source, String sourcePath, byte[] data) {
        this.source          = source;
        this.sourcePath      = sourcePath;
        this.dFile           = data;
        this.preCompilerList = preCompilerList;
        this.includedFiles   = new ArrayList<>();
    }

    /**
     * Loads all PreCompiler .class files specified in the
     * PreCompiler CLST.
     * */
    public void loadPreCompilers() {
        try {
            ClassLoader cl = null;
            preCompilers = new IPreCompiler[preCompilerList.getColumns().size()];
            for (int i = 0; i < preCompilers.length; i++) {
                URL url = new File(preCompilerList.getEntry(i, 0)).toURI().toURL();
                URL[] urls = new URL[]{url};
                cl = new URLClassLoader(urls);
                preCompilers[i] = (IPreCompiler) cl.loadClass(preCompilerList.getEntry(i, 1)).newInstance();
            }
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compiles the given source file to byte-code.
     *
     * @param byteMode
     * @param addressLength
     * */
    public byte[] compile(boolean byteMode, int addressLength) throws IOException {
        if (preCompilers == null || preCompilers.length == 0 || preCompilerList != null) {
            loadPreCompilers();
        }
        //////////////////////////////////////////////
        ArrayList<String> _words = new ArrayList<>();
        ArrayList<Byte> bytecode = new ArrayList<>();

        HashMap<String, Integer> labels         = new HashMap<>();
        HashMap<Integer, String> missing_labels = new HashMap<>();
        HashMap<String, byte[]>  identifiers    = new HashMap<>();

        int     counter  = 0;
        boolean inString = false;

        String[] lines                = source.replaceAll("\t", " ").split("\r\n");
        ArrayList<String> _included   = new ArrayList<>();

        if (byteMode) {
            System.out.printf("comp: Compiling in Byte-Mode (Address-Length %s byte%s)\n", addressLength, addressLength > 1 ? "s" : "");
            if (addressLength < 1 || addressLength > 4) {
                System.err.printf("COMPILATION ERROR: Invalid Address-Length for ByteMode [%s] (0x000006)\n", addressLength);
                System.exit(1);
            }
        }

        // Include other files
        _words.addAll(doSourceIncludes(lines, new File(sourcePath).getParent()));
        lines = DataHelper.toStringArray(_words.toArray());
        _words.clear();

        // Remove Comments
        for (int i = 0; i < lines.length; i++) {
            String[] lsplit = lines[i].split(" ");

            for (int j = 0; j < lsplit.length; j++) {
                String word = lsplit[j].replaceAll(" ", "");

                if (word.startsWith(";")) {
                    counter++;
                    break;
                } else if (!word.matches("")) {
                    _words.add(word);
                }
            }
        }
        System.out.printf("comp: Removed %s comments\n", counter); counter = 0;
        String[] words = DataHelper.toStringArray(_words.toArray());
        _words.clear();

        ArrayList<Byte> data_block = new ArrayList<>();

        // Compiler Commands
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.startsWith("\"")) inString = !inString;
            if (word.startsWith("@") && !inString) {
                switch (word) {
                case "@lnk_dat":
                    String dPath  = DataHelper.assembleStringFromArray(i+1, words, "\"", true);
                    String sPath  = new File(sourcePath).getParent();
                    File file = new File(sPath, dPath);
                    stitchDataFile(file, data_block);
                    i += dPath.split("/").length;
                    System.out.printf("comp: Stitched data-file \"%s\"\n", file.getAbsolutePath());
                    break;
                case "@id_define":
                    DataType dataType = DataType.getType(words[i+1]);
                    String   name     = words[i+2];
                    i += registerIdentifier(i+3, words, name, dataType, identifiers)+2;
                    break;
                }
            } else {
                _words.add(word);
            }
        }
        words = DataHelper.toStringArray(_words.toArray());

        // Pre Compile
        if (preCompilers != null && preCompilers.length != 0) {
            for (IPreCompiler pc : preCompilers) {
                System.out.printf("comp: Executing PreCompiler %s/%s [\"%s\"]\n", counter+1, preCompilers.length, pc.getName());
                words = pc.compile(words);
                counter++;
            }
            counter = 0;
        } else {
            System.out.println("comp: No PreCompilers specified");
        }

        // Translate to Byte Code
        inString           = false;
        counter            = 0;
        int counter2       = 0;
        int[] counter3     = new int[] {0};
        boolean writingNCL = false;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            // Native Code Linker Call Detection
            if (word.startsWith("!")) {
                writingNCL = true;
                bytecode.add((byte) 0xfc);
                word = word.substring(1);
            }

            // Native Code Linker Call Translation
            if (writingNCL) {
                char[] chars = word.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    if (chars[j] == '\"') inString = !inString;
                    if (chars[j] == '\\' && !inString) {
                        writingNCL = false;
                        bytecode.add((byte) 0x0);
                        break;
                    }
                    bytecode.add((byte) chars[j]);
                }
                if (writingNCL) bytecode.add((byte) ' ');
                continue;
            }

            // String Translation
            if (word.startsWith("\"")) {
                String s = DataHelper.assembleStringFromArray(i, words, "\"", false);
                       s = replaceEscapes(s);
                int ii   = s.split(" ").length-1;
                if (words[i+1].matches("\"")) ii++; // Check if next word is a double quote, if so increment i by one more
                                                          // to avoid the compiler attempting to terminate a string on that word
                i += ii;

                char[] chars = s.toCharArray();
                for (char c : chars) bytecode.add((byte) c);
                bytecode.add((byte) 0x0);

                continue;
            }

            // Char Translation
            if (word.startsWith("'")) {
                char c = word.split("")[1].toCharArray()[0];
                bytecode.add((byte) c);
                continue;
            }
            if (word.startsWith("p'")) {
                char c = word.split("")[2].toCharArray()[0];
                bytecode.add((byte) 0);
                bytecode.add((byte) 0);
                bytecode.add((byte) 0);
                bytecode.add((byte) c);
                continue;
            }

            // Hex-Value Translation
            if (word.startsWith("$")) {
                int val = Integer.parseInt(word.replace("$", ""), 16);
                byte[] bytes = ByteBuffer.allocate(4).putInt(val).array();
                for (int j = 4-addressLength; j < bytes.length; j++) {
                    bytecode.add(bytes[j]);
                }
                continue;
            }

            if (word.startsWith("0x")) {
                int val = Integer.parseInt(word.replace("0x", ""), 16);
                byte[] bytes = ByteBuffer.allocate(4).putInt(val).array();
                if (byteMode) {
                    bytecode.add(bytes[3]);
                } else {
                    for (byte b : bytes) {
                        bytecode.add(b);
                    }
                }
                continue;
            }

            // Label Registration
            if (word.startsWith(":") && !inString) {
                labels.put(word.replace(":", ""), bytecode.size()-1);
                counter2++;
                continue;
            }

            // Label Replacement
            if (word.startsWith("_") && !inString) {
                if (!labels.containsKey(word.replaceAll("_", ""))) {
                    for (int j = 0; j < 4; j++) bytecode.add((byte) 0x00);
                    missing_labels.put(bytecode.size()-4, word.replaceAll("_", ""));
                    continue;
                }

                byte[] bytes = ByteBuffer.allocate(4).putInt(labels.get(word.replaceAll("_", ""))).array();
                for (byte b : bytes) {
                    bytecode.add(b);
                }
                counter3[0]++;
                continue;
            }

            // Singular Byte Translation
            if (word.startsWith("BYTE=")) {
                BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(word.split("=")[1], 10));
                byte[] bytearray = (bigInt.toByteArray());
                bytecode.add((byte) bytearray[bytearray.length-1]);
                continue;
            }

            // Identifier Translation
            if (word.startsWith("#")) {
                if (identifiers.containsKey(word.substring(1))) {
                    byte[] data = identifiers.get(word.substring(1));
                    for (byte b : data) {
                        bytecode.add(b);
                    }
                    continue;
                } else {
                    System.err.println("COMPILATION ERROR: INVALID IDENTIFIER (0x000002):");
                    System.err.printf("$%s : %s\n", i, word);
                    System.err.println("NO IDENTIFIER FOUND");
                    System.exit(0);
                }
            }

            // Instruction Translation
            if (STD_INSTRUCTION_MAP.containsKey(word.toUpperCase())) {
                counter++;
                bytecode.add(STD_INSTRUCTION_MAP.get(word.toUpperCase()));

                switch (word) {
                    case "log":
                        if (!words[i+1].startsWith("\"")) {
                            bytecode.add((byte) 0x0);
                        }
                        break;
                    case "sreg":
                        if (words[i+2].startsWith("0x")) {
                            bytecode.add((byte) 0x1);
                        } else {
                            bytecode.add((byte) 0x0);
                        }

                        BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(words[i+1].replaceAll("\\$", ""), 16));
                        byte[] bytearray = (bigInt.toByteArray());
                        if (bytearray[bytearray.length-1] > 0xf) {
                            bytecode.add((byte) 0xf);
                            i++;
                            break;
                        }

                        bytecode.add(bytearray[bytearray.length-1]);
                        i++;
                        break;
                    case "pop":
                    case "peek":
                    case "push":
                        if (words[i+1].startsWith("#$")) {
                            bytecode.add((byte) 0x1);
                            words[i+1].replaceFirst("#", "");
                            break;
                        } else if (words[i+1].startsWith("r$")) {
                            bytecode.add((byte) 0x2);
                            words[i+1].replaceFirst("r", "");
                            break;
                        }
                        bytecode.add((byte) 0x0);
                        break;

                }
            } else {
                System.err.println("COMPILATION ERROR: INVALID INSTRUCTION (0x000000):");
                System.err.printf("$%s : %s\n", i, word);
                System.err.println("NO INSTRUCTION FOUND");
                System.exit(0);
            }
        }

        ArrayList<Byte>[] tmp = new ArrayList[] {bytecode};
        // Replace Missing Label addresses if there are some
        if (!missing_labels.isEmpty()) {
            missing_labels.forEach((codepoint, label) -> {
                if (!labels.containsKey(label)) {
                    System.err.println("COMPILATION ERROR: LABEL NOT DEFINED! (0x000001)");
                    System.err.printf("$%s : %s\n", codepoint, label);
                    System.err.println("LABEL NOT IN LABEL_MAP");
                    return;
                }
                byte[] address = ByteBuffer.allocate(4).putInt(labels.get(label)).array();
                for (int i = 0; i < 4; i++)
                    tmp[0].set(codepoint+i, address[i]);

                counter3[0]++;
            });
        }

        // Check if Data-Block has contents, if not give it some
        if (data_block.size() == 0) {
            data_block.addAll(Arrays.asList(dummyDFile));
        }
        
        // Terminate Data-Block
        data_block.add((byte) 0x01);

        // Write Format Byte
        data_block.add(byteMode ? 0x01 : (byte) 0x00);
        if (byteMode) data_block.add((byte) addressLength);

        // Attach the compiled program to the bytecode ArrayList
        data_block.addAll(bytecode);
        bytecode = data_block;

        System.out.printf("comp: Found %s label%s\n", counter2, counter2 > 1 ? "s" : "");
        System.out.printf("comp: Replaced %s label mention%s\n", counter3[0], counter3[0] > 1 ? "s" : "");
        System.out.printf("comp: Compiled %s instructions\n", counter);
        System.out.printf("comp: Compiled Program size: %s bytes\n", bytecode.size());

        return DataHelper.toByteArray(bytecode.toArray());
    }

    /**
     * Recursively includes AIA Source Files.
     *
     * @param lines The Program Source to include for
     * @return The Program Source with the included source
     * */
    public ArrayList<String> doSourceIncludes(String[] lines, String origin) throws IOException {
        ArrayList<String> included = new ArrayList<>();
        ArrayList<String> words    = new ArrayList<>();

        boolean including = true;
        for (int i = 0; i < lines.length; i++) {
            String[] lsplit = lines[i].split(" ");

            if (!including) {
                words.add(lines[i]);
                continue;
            }

            for (int j = 0; j < lsplit.length; j++) {
                String word = lsplit[j];

                if (word.matches(".start")) {
                    including = false;
                } else if (word.matches("\\*include")) {
                    if (lsplit[j+1].matches("source")) { // Is the include not compiled?
                        // Load File
                        String dPath   = DataHelper.assembleStringFromArray(j+2, lsplit, "\"", true);
                        File file;

                        // Check if loading standard library
                        if (lsplit[j+2].startsWith("!")) {
                            dPath = dPath.substring(1);
                            file = new File(new File(DataHelper.getProgramDirectory(this.getClass()), STANDARD_LIBRARY_DIRECTORY_NAME),
                                    dPath);

                            // Check if valid library was specified
                            if (!file.exists()) {
                                System.err.println("INCLUDE ERRROR: INVALID STD_LIB: "+dPath+" (0x000005)");
                                System.exit(1);
                            }
                        } else {
                            file = new File(origin, dPath);

                            // Check if valid library was specified
                            if (!file.exists()) {
                                System.err.println("INCLUDE ERRROR: INVALID INCLUDE PATH: "+dPath+" (0x000005)");
                                System.exit(1);
                            }
                        }

                        // Add to included list
                        if (includedFiles.contains(file)) continue;
                        includedFiles.add(file);

                        // Do Includes for included file
                        String[] _lines = new String(Files.readAllBytes(file.toPath())).replaceAll("\t", " ").split("\r\n");
                        included.addAll(doSourceIncludes(_lines, file.getParent()));

                        System.out.printf("comp: Included & Appended source file [\"%s\"]\n", file.getAbsolutePath());
                    }
                }
            }
        }
        words.addAll(included);
        return words;
    }

    /**
     * Registers an Identifier for compilation.
     *
     * @param index       The current index within the words array
     * @param words       The source code array
     * @param name        The name to register the identifier with
     * @param dataType    The data type of the identifier
     * @param identifiers The identifier registry
     * */
    private int registerIdentifier(int index, String[] words, String name, DataType dataType, HashMap<String, byte[]> identifiers) {
        if (dataType == DataType.INVALID) {
            System.err.println("COMPILATION ERROR: INVALID DATA-TYPE (0x000003):");
            System.err.printf("identifier: %s ; index : %s", name, index);
            System.exit(0);
        }

        int skipWords = 0;
        byte[] data = new byte[] {0x0};
        if (dataType == DataType.STRING) {
            String str = DataHelper.assembleStringFromArray(index, words, "\"", true);
            char[] _data = str.toCharArray();
            data = new byte[_data.length];
            for (int i = 0; i < _data.length; i++) {
                data[i] = (byte) _data[i];
            }
            skipWords = str.split(" ").length;
        } else if (dataType == DataType.INTEGER) {
            String nWord = words[index];
            if (nWord.startsWith("0x") || nWord.startsWith("$")) {
                int val = Integer.parseInt(nWord.replace("$", "").replace("0x", ""), 16);
                data = ByteBuffer.allocate(4).putInt(val).array();
            } else {
                int val = Integer.parseInt(nWord);
                data = ByteBuffer.allocate(4).putInt(val).array();
            }
            skipWords = 1;
        } else if (dataType == DataType.BYTE) {
            String nWord = words[index].replace("$", "").replace("0x", "");
            BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(nWord, 10));
            data = (bigInt.toByteArray());
            data = new byte[] {data[data.length-1]};
            skipWords = 1;
        } else if (dataType == DataType.LARGE) {
            String nWord = words[index];
            if (nWord.startsWith("0x") || nWord.startsWith("$")) {
                long val = Long.parseLong(nWord.replace("$", "").replace("0x", ""), 16);
                data = ByteBuffer.allocate(8).putLong(val).array();
            } else {
                long val = Long.parseLong(nWord);
                data = ByteBuffer.allocate(8).putLong(val).array();
            }
            skipWords = 1;
        }

        identifiers.put(name, data);
        System.out.printf("comp: Registered Identifier \"%s\" of type %s [%s byte%s]\n",
                name, dataType.toString(), data.length, data.length > 1 ? "s" : "");

        return skipWords;
    }

    private void stitchDataFile(File file, ArrayList<Byte> bytecode) throws IOException {
        byte[] cont = Files.readAllBytes(file.toPath());
        for (byte b : cont) {
            bytecode.add(b);
        }
    }

    /**
     * Replaces all standard escape codes in a String with their binary equivalent.
     *
     * @param text The Input String
     * @return String with binary escapes
     * */
    private String replaceEscapes(String text)  {
        String out = "";

        boolean inEscape = false;
        for (char c : text.toCharArray()) {
            if (c == '\\' && inEscape)  {
                out += "\\";
                inEscape = false;
                continue;
            } else if (c == '\\') {
                inEscape = true;
                continue;
            }
            if (inEscape) {
                if (!ESCAPE_LOOKUP.containsKey(c)) {
                    out += c;
                    continue;
                }

                out += (char) (byte) ESCAPE_LOOKUP.get(c);
                inEscape = false;
            } else {
                out += c;
            }
        }

        return out;
    }

    /**
     * Used to define Data Types for Custom Identifiers defined
     * using the {@code @id_define <type> <name> <data>} compiler command.
     *
     * All Data-Types are defined within Chapter 4.1.2.1 (Custom Identifier Data-Types)
     * within the AIA4 Specification (16-10-2021 Revision).
     *
     * @author 0x1905
     * @since 0.3.0
     * */
    public static class DataType {
        private String name;

        public static DataType STRING  = new DataType("string");
        public static DataType INTEGER = new DataType("integer");
        public static DataType BYTE    = new DataType("byte");
        public static DataType LARGE   = new DataType("large");
        public static DataType INVALID = new DataType("");

        /////////////////////////////////////////////

        public DataType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name.toUpperCase();
        }

        /////////////////////////////////////////////

        public static DataType getType(String name) {
            switch (name) {
                case "str": return STRING;
                case "int": return INTEGER;
                case "byt": return BYTE;
                case "lrg": return LARGE;
            }
            return INVALID;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("AIA 4 Standard Compiler "+COMPILER_VERSION);

        if (args.length == 0) {
            System.out.println();
            System.out.println("Valid Compiler Arguments:");
            System.out.println("-i <file>   Input Source-File");
            System.out.println("-o <file>   Output Program-File");
            System.out.println("-d <file>   Set Data File");
            System.out.println("-pcl <file> Set Precompiler List (.clst)");
            System.out.println("-verbose    Logs every stage/action of the compiler");

            return;
        }

        String  inputFile     = "";
        String  outputFile    = "";
        String  dFile         = null;
        String  clst          = "";
        boolean nocomp        = false;
        boolean byteMode      = false;
        int     addressLength = 4;

        PrintStream outCopy = System.out;
        System.setOut(new PrintStream(new NullStream()));

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
            } else if (arg.matches("-verbose")) {
                System.setOut(outCopy);
                continue;
            } else if (arg.matches("-bytemode")) {
                outCopy.println("BYTE MODE IS A WORK IN PROGRESS FEATURE, EXPECT BUGS WHEN EXECUTING A COMPILED PROGRAM");
                byteMode      = true;
                addressLength = Integer.parseInt(args[i+1]);
                i++;
                continue;
            } else if (arg.matches("-nocomp")) {
                nocomp = true;
                continue;
            }
        }

        System.out.printf("comp: Compiling file %s!\n", inputFile);
        Compiler c = new Compiler(clst, inputFile, dFile);

        long startTime = System.currentTimeMillis();
        byte[] prg = c.compile(byteMode, addressLength);

        System.out.printf("comp: Writing bytecode to file [\"%s\"]\n",outputFile);
        writeBinaryFile(outputFile, prg, nocomp);
        System.setOut(outCopy);
        System.out.printf("Compilation Finished in %ss!\n", (float) (System.currentTimeMillis()-startTime)/1000);
    }

    private static void writeBinaryFile(String output, byte[] bin, boolean nocomp) {
        try {
            if (!nocomp) {
                bin = DataHelper.compressBytes(bin);
                System.out.printf("comp: Compressed output to %s bytes\n", bin.length);
            }
            OutputStream os = new FileOutputStream(output);
            os.write(bin);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
