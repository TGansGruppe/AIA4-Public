package de.gansgruppe.aia4.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.zip.*;

/**
 * A class containing multiple helpful functions
 * for converting in between data types.
 * 
 * @author 0x1905
 * @author Alan Goose
 * */
public class DataHelper {
	/**
	 * Converts an object array to a byte array
	 * @param arr The object array
	 * @return The converted byte array
	 * */
	public static byte[] toByteArray(Object[] arr) {
		byte[] out = new byte[arr.length];
		for (int i = 0; i < arr.length; i++) {
			out[i] = (byte) arr[i];
		}
		
		return out;
	}

	/**
	 * Converts an array of Wrapped-Bytes to a
	 * primitive type byte array.
	 * @see java.lang.Byte
	 *
	 * @param arr The object array
	 * @return The converted byte array
	 * */
	public static byte[] asPrimitiveBytes(Byte[] arr) {
		byte[] out = new byte[arr.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = arr[i];
		}

		return out;
	}
	
	/**
	 * Converts an object array to a int array
	 * @param arr The object array
	 * @return The converted int array
	 * */
	public static int[] toIntArray(Object[] arr) {
		int[] out = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			out[i] = (int) arr[i];
		}
		
		return out;
	}
	
	/**
	 * Converts an object array to a String array
	 * @param arr The object array
	 * @return The converted string array
	 * */
	public static String[] toStringArray(Object[] arr) {
		String[] out = new String[arr.length];
		for (int i = 0; i < arr.length; i++) {
			out[i] = (String) arr[i];
		}
		
		return out;
	}
	
	/**
	 * @param bytes Array of bytes to be made into a 32-bit int
	 * @return The created int
	 * */
	public static int createInt(byte[] bytes) {
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (bytes[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	/**
	 * @param bytes Array of bytes to be made into a 16-bit short
	 * @return The created short
	 * */
	public static short createShort(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getShort();
	}
	
	/**
	 * @param byyte The byte to be converted to an integer
	 * @return Converted byte
	 * */
	public static int byteToInt(byte byyte)  {
		return byyte & 0xFF;
	}
	
	/**
	 * @param bytes Bytes to be converted
	 * @return The converted array
	 * */
	public static int[] convertToIntArray(byte[] bytes) {
		int[] out = new int[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			out[i] = byteToInt(bytes[i]);
		}
		
		return out;
	}
	
	/**
	 * @param _int The integer to be converted
	 * @return An array of 4 bytes which made up the input
	 * */
	public static byte[] convertToByteArray(int _int) {
		return ByteBuffer.allocate(4).putInt(_int).array();
	}

	/**
	 * Assembles a single String from a array of Strings.
	 *
	 * @param index 	  The index to start assembly from
	 * @param words 	  The array to assemble from
	 * @param terminator  The String terminator
	 * @param removeFirst Sets if the first char of the String should be clipped
	 * @return The assembled String
	 */
	public static String assembleStringFromArray(int index, String[] words, String terminator, boolean removeFirst) {
		String out = "";

		for (int i = index; i < words.length; i++) {
			out += words[i];
			if (words[i].endsWith(terminator)) break;
			out += " ";
		}
		return out.substring(removeFirst ? 1 : 0, out.length()-1);
	}

	/**
	 * Returns a key corresponding to a value inside of a Map
	 * 
	 * @param map The Map to be searched
	 * @param value The value to search with
	 * @return Key corresponding to the value
	 * */
	public static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry: map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
	
	/**
	 * @param x The integer to be converted
	 * @return An array of individual bits of the input
	 * */
	private static boolean[] intToBool(int x) {
		boolean[] flags = new boolean[32];
		byte[] bytes = convertToByteArray(x);
		
		int bitPos = 0;
		for (int i = 0; i < 4; i++) {
			boolean[] n = booleanArrayFromByte(bytes[i]);
			for (int j = 0; j < 8; j++) {
				flags[bitPos] = n[j];
				bitPos++;
			}
		}

		return flags;
	}
	
	/**
	 * @param x The byte to be converted
	 * @return An array of individual bits of the input
	 * */
	public static boolean[] booleanArrayFromByte(byte x) {
	    boolean bs[] = new boolean[8];
	    bs[0] = ((x & 0x01) != 0);
	    bs[1] = ((x & 0x02) != 0);
	    bs[2] = ((x & 0x04) != 0);
	    bs[3] = ((x & 0x08) != 0);
	    bs[4] = ((x & 16)   != 0);
	    bs[5] = ((x & 32)   != 0);
	    bs[6] = ((x & 64)   != 0);
	    bs[7] = ((x & 128)  != 0);
	    return bs;
	}

	public static String byteToHexString(byte v) {
		return String.format("%02X", v);
	}

	/**
	 * Compresses a byte array using the {@link java.util.zip.DeflaterOutputStream}.
	 * @param in The array to compress
	 * @return The compressed byte array
	 * */
	public static byte[] compressBytes(byte[] in) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream defl = new DeflaterOutputStream(out);
			defl.write(in);
			defl.flush();
			defl.close();

			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	/**
	 * Decompresses a byte array using the {@link java.util.zip.InflaterOutputStream}.
	 * @param in The array to decompress
	 * @return The decompressed byte array
	 * */
	public static byte[] decompressBytes(byte[] in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InflaterOutputStream infl = new InflaterOutputStream(out);
		infl.write(in);
		infl.flush();
		infl.close();

		return out.toByteArray();
	}

	/**
	 * Gets the directory of the current program
	 * @param CLAZZ The Class to use to locate the program
	 * @return The Path to the program directory
	 * */
	public static String getProgramDirectory(Class<?> CLAZZ) {
		try {
			return new File(CLAZZ
					.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI()
					.getPath()).getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return "\\/";
	}
}
