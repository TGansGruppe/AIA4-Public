package de.gansgruppe.aia4.compiler;

/**
 * A Precompiler implementation is used for
 * replacing instructions with their hex codes.
 * */
public interface IPreCompiler {
	/**
	 * @param words The uncompiled and split source code.
	 * @return The precompiled source code.
	 * */
	String[] compile(String[] words);
	String   getName();
}
