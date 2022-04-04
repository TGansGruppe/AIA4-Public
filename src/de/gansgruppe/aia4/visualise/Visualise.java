package de.gansgruppe.aia4.visualise;

import de.gansgruppe.aia4.util.DataHelper;

import static de.gansgruppe.aia4.Main.AIA_RUNTIME;
import static de.gansgruppe.aia4.util.Instructions.INST_NAMES;
import java.awt.*;

public class Visualise implements Runnable{
	private static Visualise singleton;

	private Visualise() {}
	public static void start() {
		singleton = new Visualise();
		singleton._start();
	}

	/////////////////////////////////////////////
	private Thread thread;
	private Renderer renderer;

	public void _start() {
		this.renderer = new Renderer(new Renderer.Window(), this);
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void run() {
		while (true)  {
			this.renderer.render();
		}
	}

	private int distance = 0;
	public void render(Graphics g) {
		distance = 1;
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, renderer.window.getWidth(), renderer.window.getHeight());

		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", Font.PLAIN, 14));

		// Basic Info
		g.drawString("Program Counter: "+AIA_RUNTIME.LANGUAGE_PROCESSOR.programCounter, 0, g.getFontMetrics().getHeight()); distance++;
		g.drawString(String.format("Current Instruction: %s (%s)",(AIA_RUNTIME.LANGUAGE_PROCESSOR.program[
				AIA_RUNTIME.LANGUAGE_PROCESSOR.programCounter] & 0xFF), INST_NAMES.get(AIA_RUNTIME.LANGUAGE_PROCESSOR.program[
				AIA_RUNTIME.LANGUAGE_PROCESSOR.programCounter])), 0, g.getFontMetrics().getHeight()*distance); distance++;

		// Draw Registers
		String registerValues = "";
		for (int i = 0; i < AIA_RUNTIME.LANGUAGE_PROCESSOR.REGS.length; i++) {
			registerValues += String.format("$%s = 0x%s; ", Integer.toHexString(i), Integer.toHexString(AIA_RUNTIME.LANGUAGE_PROCESSOR.REGS[i]));
		}
		g.drawString("Registers: "+registerValues, 0, g.getFontMetrics().getHeight()*distance); distance++;

		// Draw Program
		g.drawString("Program: ", 0, g.getFontMetrics().getHeight()*distance); distance++;

		int xOffset = 0;
		for (int i = 0; i < AIA_RUNTIME.LANGUAGE_PROCESSOR.program.length; i++) {
			if (i % 36 == 0) {
				distance++;
				xOffset = 0;
			}

			byte instruction = AIA_RUNTIME.LANGUAGE_PROCESSOR.program[i];

			if (i == AIA_RUNTIME.LANGUAGE_PROCESSOR.programCounter) {
				g.setColor(Color.GREEN);
				g.drawString("0x"+ DataHelper.byteToHexString(instruction), xOffset, g.getFontMetrics().getHeight()*distance);
				g.setColor(Color.WHITE);
			} else {
				g.drawString("0x"+DataHelper.byteToHexString(instruction), xOffset, g.getFontMetrics().getHeight()*distance);
			}
			xOffset += g.getFontMetrics().getStringBounds("0x"+DataHelper.byteToHexString(instruction)+" ", g).getWidth();
		} distance++;

		// Draw Memory
		g.drawString("Memory: ", 0, g.getFontMetrics().getHeight()*distance); distance++;
		xOffset = 0;
		for (int i = 0; i < AIA_RUNTIME.LANGUAGE_PROCESSOR.MEMORY.length; i++) {
			if (i % 36 == 0) {
				distance++;
				xOffset = 0;
			}

			g.drawString("0x"+Integer.toHexString(AIA_RUNTIME.LANGUAGE_PROCESSOR.MEMORY[i]), xOffset, g.getFontMetrics().getHeight()*distance);

			xOffset += g.getFontMetrics().getStringBounds("0x"+Integer.toHexString(AIA_RUNTIME.LANGUAGE_PROCESSOR.MEMORY[i])+" ", g).getWidth();
		} distance++;
	}
}
