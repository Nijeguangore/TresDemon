package eventListeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseMotionCapture implements MouseMotionListener {
	private int X_0=-1,Y_0=-1;
	private int X_1,Y_1;

	public MouseMotionCapture() {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(X_0 != -1 || Y_0 != -1){
			X_0 = X_1;
			X_1 = e.getX();
			Y_0 = Y_1;
			Y_1 = e.getY();
		}
		else{
			X_0 = e.getX();
			X_1 = X_0;
			Y_0 = e.getY();
			Y_1 = Y_0;
		}
		
	}
	
	public int getX(){
		int change = X_1 - X_0;
		X_0 = X_1;
		return change;
	}
	
	public int getY(){
		int change = Y_1 - Y_0;
		Y_0 = Y_1;
		return change;
	}

}
