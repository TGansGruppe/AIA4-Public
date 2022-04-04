package de.gansgruppe.aia4.visualise;

import java.awt.*;
import java.awt.image.BufferStrategy;
import javax.swing.*;

public class Renderer {
	public Window window;
	private Visualise vInstance;

	public Renderer(Window window, Visualise vInstance) {
		this.window = window;
		this.vInstance = vInstance;
	}

	public void render() {
		BufferStrategy bs = window.cvs.getBufferStrategy();

		if (bs == null) {
			window.cvs.createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		g.clearRect(0, 0, window.getWidth(), window.getHeight());

		// Pass graphics over here
		vInstance.render(g);

		g.dispose();
		bs.show();
	}

	public static class Window extends JFrame {
		public Canvas cvs;

		public Window() {
			super("AIA4 Program Visualiser");
			setSize(640*2, 480*2);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(true);
			setLocationRelativeTo(null);

			this.cvs = new Canvas();
			cvs.setSize(getWidth(), getHeight());

			add(cvs);
			setVisible(true);
		}
	}
}
