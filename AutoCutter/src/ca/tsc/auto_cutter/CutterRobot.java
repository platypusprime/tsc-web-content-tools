package ca.tsc.auto_cutter;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CutterRobot extends Robot {

	private static final int[] DIG_KEY_CODES = { KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2,
			KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
			KeyEvent.VK_8, KeyEvent.VK_9 };

	private Point originalPoint;

	public CutterRobot() throws AWTException {
		super();
	}

	public void keyType(int keyCode) {
		keyPress(keyCode);
		// delay(20);
		keyRelease(keyCode);
	}

	public void digType(int i) {
		if (i >= 0 && i < 10) {
			keyType(DIG_KEY_CODES[i]);
		}
	}

	public void numType(int i) {
		if (i < 0)
			return;
		if (i > 9)
			numType(i / 10);
		digType(i % 10);
	}

	public void mouseLeftClick() {
		mousePress(InputEvent.BUTTON1_MASK);
		mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public void rememberCursor() {
		originalPoint = MouseInfo.getPointerInfo().getLocation();
	}

	public void returnCursor() {
		mouseMove(originalPoint.x, originalPoint.y);
	}

	protected void seekFrame(int frame, Point p) {
		// click the time jump UI
		mouseMove(p.x, p.y);
		mousePress(InputEvent.BUTTON1_MASK);
		mouseRelease(InputEvent.BUTTON1_MASK);

		// enter in time
		numType(frame);
		keyType(KeyEvent.VK_ENTER);
	}
}
