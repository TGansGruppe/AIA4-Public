package de.gansgruppe.aia4.util;

import java.util.HashMap;

public class Instructions {
	public static HashMap<Byte, String> INST_NAMES = new HashMap<>();
	public static HashMap<String, Byte> INST_CODES = new HashMap<>();
	static {
		INST_NAMES.put((byte) 0, "SET");
		INST_NAMES.put((byte) 1, "CMP");
		INST_NAMES.put((byte) 2, "JE");
		INST_NAMES.put((byte) 3, "JG");
		INST_NAMES.put((byte) 4, "JL");
		INST_NAMES.put((byte) 5, "JMP");
		INST_NAMES.put((byte) 6, "LOG");
		INST_NAMES.put((byte) 7, "ADD");
		INST_NAMES.put((byte) 8, "SUB");
		INST_NAMES.put((byte) 9, "DIV");
		INST_NAMES.put((byte) 10, "MUL");
		INST_NAMES.put((byte) 11, "GET");
		INST_NAMES.put((byte) 12, "CPR");
		INST_NAMES.put((byte) 13, "CPM");
		INST_NAMES.put((byte) 14, "PGET");
		INST_NAMES.put((byte) 15, "PCPM");
		INST_NAMES.put((byte) 34, "CAL");
		INST_NAMES.put((byte) 35, "RET");
		INST_NAMES.put((byte) 36, "SREG");
		INST_NAMES.put((byte) 37, "POP");
		INST_NAMES.put((byte) 38, "PEEK");
		INST_NAMES.put((byte) 39, "PUSH");
		INST_NAMES.put((byte) 254, "SYS_ERR");
		INST_NAMES.put((byte) 255, "STOP");

		INST_NAMES.put((byte) 0x1a, "AND");
		INST_NAMES.put((byte) 0x1b, "OR");
		INST_NAMES.put((byte) 0x1c, "XOR");
		INST_NAMES.put((byte) 0x1d, "ROR");
		INST_NAMES.put((byte) 0x1e, "ROL");
		INST_NAMES.put((byte) 0xfe, "SMU");
		INST_NAMES.put((byte) 0x20, "REV");
		INST_NAMES.put((byte) 0x21, "CMU");

		INST_NAMES.put((byte) 0x16, "TCP_CNCT");
		INST_NAMES.put((byte) 0x17, "TCP_DCNCT");
		INST_NAMES.put((byte) 0x18, "TCP_SEND");
		INST_NAMES.put((byte) 0x19, "TCP_RECV");

		INST_NAMES.put((byte) 0x10, "STD_IN");
		INST_NAMES.put((byte) 0x11, "FL_OPN");
		INST_NAMES.put((byte) 0x12, "FL_WRT");
		INST_NAMES.put((byte) 0x13, "FL_CLS");
		INST_NAMES.put((byte) 0x14, "FL_CRT");
		INST_NAMES.put((byte) 0x15, "FL_APP");

		////////////////////////////////////////

		INST_CODES.put("SET",     (byte) 0);
		INST_CODES.put("CMP",     (byte) 1);
		INST_CODES.put("JE",      (byte) 2);
		INST_CODES.put("JG",      (byte) 3);
		INST_CODES.put("JL",      (byte) 4);
		INST_CODES.put("JMP",     (byte) 5);
		INST_CODES.put("LOG",     (byte) 6);
		INST_CODES.put("ADD",     (byte) 7);
		INST_CODES.put("SUB",     (byte) 8);
		INST_CODES.put("DIV",     (byte) 9);
		INST_CODES.put("MUL",     (byte) 10);
		INST_CODES.put("GET",     (byte) 11);
		INST_CODES.put("CPR",     (byte) 12);
		INST_CODES.put("CPM",     (byte) 13);
		INST_CODES.put("PGET",    (byte) 14);
		INST_CODES.put("PCPM",    (byte) 15);
		INST_CODES.put("CAL",     (byte) 34);
		INST_CODES.put("RET",     (byte) 35);
		INST_CODES.put("SREG",    (byte) 36);
		INST_CODES.put("POP",     (byte) 37);
		INST_CODES.put("PEEK",     (byte) 38);
		INST_CODES.put("PUSH",     (byte) 39);
		INST_CODES.put("SYS_ERR", (byte) 254);
		INST_CODES.put("STOP",    (byte) 255);

		INST_CODES.put("AND", (byte) 0x1a);
		INST_CODES.put("OR",  (byte) 0x1b);
		INST_CODES.put("XOR", (byte) 0x1c);
		INST_CODES.put("ROR", (byte) 0x1d);
		INST_CODES.put("ROL", (byte) 0x1e);
		INST_CODES.put("SMU", (byte) 0x1f);
		INST_CODES.put("REV", (byte) 0x20);
		INST_CODES.put("CMU", (byte) 0x21);


		INST_CODES.put("TCP_CNCT",  (byte) 0x16);
		INST_CODES.put("TCP_DCNCT", (byte) 0x17);
		INST_CODES.put("TCP_SEND",  (byte) 0x18);
		INST_CODES.put("TCP_RECV",  (byte) 0x19);

		INST_CODES.put("STD_IN", (byte) 0x10);
		INST_CODES.put("FL_OPN", (byte) 0x11);
		INST_CODES.put("FL_WRT", (byte) 0x12);
		INST_CODES.put("FL_CLS", (byte) 0x13);
		INST_CODES.put("FL_CRT", (byte) 0x14);
		INST_CODES.put("FL_APP", (byte) 0x15);
	}
}
