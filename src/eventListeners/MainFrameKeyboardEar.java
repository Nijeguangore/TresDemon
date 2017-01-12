package eventListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import initZone.Main;

public class MainFrameKeyboardEar implements KeyListener {
	
	private char directionKey = 'z';
	private boolean changed = false;
	private boolean[] directions;
	
	public MainFrameKeyboardEar() {
		//N,S,E,W
		directions = new boolean[]{false,false,false,false};
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == e.VK_ESCAPE){
			Main.continueJuego = false;
		}
		else{
			if(e.getKeyCode() == e.VK_W){
				directions[0] = true;
			}
			else if(e.getKeyCode() == e.VK_S){
				directions[1] = true;
			}
			if(e.getKeyCode() == e.VK_A){
				directions[2] = true;
			}
			else if(e.getKeyCode() == e.VK_D){
				directions[3] = true;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == e.VK_W){
			directions[0] = false;
		}
		else if(e.getKeyCode() == e.VK_S){
			directions[1] = false;
		}
		if(e.getKeyCode() == e.VK_A){
			directions[2] = false;
		}
		else if(e.getKeyCode() == e.VK_D){
			directions[3] = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean[] getDir(){
		return directions;
	}

}
