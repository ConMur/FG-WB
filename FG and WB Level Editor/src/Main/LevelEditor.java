package Main;

import java.io.IOException;

import javax.swing.JFrame;

public class LevelEditor
{
	private static LevelEditorPanel panel;
	
	public static void main(String[] args) throws IOException
	{
		panel = new LevelEditorPanel();
		
		JFrame frame = new JFrame("Level Editor");
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setUndecorated(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.toFront();
		
		panel.start();
	}

}
