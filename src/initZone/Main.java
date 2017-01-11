package initZone;

import java.awt.Color;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import com.jogamp.opengl.awt.GLJPanel;

public class Main {
	
	


	public static void main(String[] args) {
		JFrame window = new JFrame();
		GLJPanel test = new GLJPanel();
		
		
		BaseRenderer bgWipe;
		try {
			bgWipe = new BaseRenderer();
			test.addGLEventListener(bgWipe);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		window.setSize(1200, 900);
		window.getContentPane().add(test);
		window.setVisible(true);
		
		while(true){
			test.display();
		}
		
	}

}
