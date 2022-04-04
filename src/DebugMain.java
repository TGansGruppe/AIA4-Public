import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

import de.gansgruppe.aia4.Main;
import de.gansgruppe.aia4.util.DataHelper;

public class DebugMain {
	public static void main(String[] args) {
		boolean foundFile = false;
		boolean noOtherFunc = true;
		ArrayList<String> nArgs = new ArrayList<>();
		
		for (String s : args) {
			if (s.matches("-f"))
				foundFile = true;
			
			if (s.matches("-h") || s.matches("-v"))
				noOtherFunc = false;
		}
		
		for (String s : args) {
			nArgs.add(s);
		}
		
		if (!foundFile && noOtherFunc) {
			System.out.println("DebugMain: You have specified no file to execute!");
			
			boolean validFile = false;
			Scanner s = new Scanner(System.in);
			String f = "";
			while (!validFile) {
				System.out.println("FILE> ");
				f = s.nextLine();
				validFile = new File(f).exists();
				
				if (!validFile) {
					System.out.println("DebugMain: That file does not exist!");
				}
			}
			s.close();
			nArgs.add("-f");
			nArgs.add(f);
		}
		nArgs.add("-pje");

		Main.main(DataHelper.toStringArray(nArgs.toArray()));
	}
}
