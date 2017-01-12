package initZone;

import java.awt.Color;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.awt.GLJPanel;

import eventListeners.MainFrameKeyboardEar;
import eventListeners.MouseMotionCapture;

public class Main {
	
	public static boolean continueJuego = true;
	public static boolean[] movementDirections = new boolean[4];
	private static float delta = 0.05f;

	public static void main(String[] args) {
		JFrame window = new JFrame();
		GLJPanel glContext = new GLJPanel();
		
		MainFrameKeyboardEar keyboardListener = new MainFrameKeyboardEar();
		glContext.addKeyListener(keyboardListener);
		
		MouseMotionCapture mouseCapturer = new MouseMotionCapture();
		glContext.addMouseMotionListener(mouseCapturer);
		
		window.setSize(1200, 900);
		window.getContentPane().add(glContext);
		window.setVisible(true);
		
		BaseRenderer bgWipe = null;
		try {
			bgWipe = new BaseRenderer();
			glContext.addGLEventListener(bgWipe);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(continueJuego){
			pingKeyboard(keyboardListener,bgWipe);
			glContext.display();
		}
		//Application not closing properly TODO
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(false);
		return;
	}
	
	public static void pingKeyboard(MainFrameKeyboardEar keys,BaseRenderer bR){
		movementDirections = keys.getDir();
		if(movementDirections[0]){
			bR.cameraLocation[2] += delta*1;
		}
		else if(movementDirections[1]){
			bR.cameraLocation[2] -= delta*1;
		}
		if(movementDirections[2]){
			bR.cameraLocation[0] += delta*1;
		}
		else if(movementDirections[3]){
			bR.cameraLocation[0] -= delta*1;
		}
		
		bR.cameraLookation[0] = bR.cameraLocation[0];
				bR.cameraLookation[1] = bR.cameraLocation[1];
						bR.cameraLookation[2] = bR.cameraLocation[2] - 1.0f;
		
	}

}
