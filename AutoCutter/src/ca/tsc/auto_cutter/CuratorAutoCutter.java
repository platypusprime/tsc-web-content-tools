package ca.tsc.auto_cutter;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * A bot which uses OCR to automatically cut an hour in Curator based on
 * pricing graphic changes.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v1.0:</dt>
 * <dd>14-11-22 - Created modified AutoCutter for Curator interface</dd>
 * <dd><b>14-11-23 - Minor changes</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since November 22, 2014
 * @version 1.0.1
 */
public class CuratorAutoCutter {

	// clipping times
	private static final int MAX_STEP_SIZE = 32;
	private static final int REFINE_ITERATIONS = 4;

	// time delays
	@SuppressWarnings("unused")
	private static final long FRAME_RENDER_DELAY = 500;
	private static final long LONG_INPUT_DELAY = 500;
	@SuppressWarnings("unused")
	private static final long SHORT_INPUT_DELAY = 250;

	// OCR constants
	private static final int SCALE = 4;
	private static final int ITEM_NUMBER_LENGTH = 6;

	// the rest
	private CuratorTyperRobot r;
	private int currentTime = -120;

	// private ArrayList<Segment> segments;
	// private Map<String, String> data = new HashMap<String, String>();

	public static void main(String[] args) throws AWTException {

		// while (true)
		// System.out.println(MouseInfo.getPointerInfo().getLocation());

		int clipLength = 0;
		// Map<String, String> data = new HashMap<String, String>();

		try {
			// System.out.println("ENTER ITEM INFORMATION:");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			// String ln;
			// while (!(ln = reader.readLine()).equals("")) {
			// String[] lnsplit = ln.split("\t");
			// if (lnsplit[4].length() == 6)
			// data.put(lnsplit[4], lnsplit[2]);
			//
			// }

			System.out.print("ENTER END TIME (MM:SS): ");
			String[] split = reader.readLine().split(":");
			clipLength = Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unused")
		CuratorAutoCutter auto = new CuratorAutoCutter(clipLength/* , data */);

	}

	public CuratorAutoCutter(int clipLength/* , Map<String, String> data */) {

		// this.data = data;

		try {
			r = new CuratorTyperRobot();
			// segments = new ArrayList<Segment>();

			int prevNum = 0, curNum = 0, fineNum = 0;

			// initial set-up
			r.mouseLeftClick(ScreenPoint.FULLSCREEN);
			r.mouseLeftClick(ScreenPoint.CLIP_RESET);
			r.mouseLeftClick(ScreenPoint.RELATIVE_TIME_SEEK);
			r.mouseLeftClick(ScreenPoint.CLIP_CREATE);
			Thread.sleep(LONG_INPUT_DELAY);
			Thread.sleep(LONG_INPUT_DELAY);
			r.mouseLeftClick(ScreenPoint.CLIP_IN);
			Thread.sleep(1500);

			boolean firstSegment = true;

			while (currentTime < clipLength) {
				// manual loop break
				if (MouseInfo.getPointerInfo().getLocation().y < 100)
					return;

				// check next frame
				seek(MAX_STEP_SIZE);
				curNum = readItemNumber();

				// detect number changes
				if (curNum != 0 && (curNum != prevNum || prevNum == 0)) {
					System.out.println("NUMBER CHANGE FROM " + prevNum + " TO " + curNum);

					// if it isn't the first clip, fine cut
					if (!firstSegment) {

						int currentStepSize = (int) (MAX_STEP_SIZE * -.5);
						for (int i = 0; i < REFINE_ITERATIONS; i++) {
							seek(currentStepSize);
							fineNum = readItemNumber();
							if (fineNum != curNum)
								currentStepSize = (int) (Math.abs(currentStepSize) * .5);
							else
								currentStepSize = (int) (Math.abs(currentStepSize) * -.5);
						}

						// create segment
						createSequence(prevNum);
						r.mouseLeftClick(ScreenPoint.CLIP_IN);
						Thread.sleep(LONG_INPUT_DELAY);
						Thread.sleep(LONG_INPUT_DELAY);
					} else {
						firstSegment = false;
					}

					prevNum = curNum;
				}
			}
			// finish up last segment
			createSequence(curNum);
			r.mouseLeftClick(ScreenPoint.CLIP_CLOSE);
			r.mouseLeftClick(ScreenPoint.FULLSCREEN);

		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		signalCompletion();
	}

	private void seek(int relativeSeconds) {

		// click the time jump UI
		r.mouseMove(ScreenPoint.TIMECODE_SECONDS.x, ScreenPoint.TIMECODE_SECONDS.y);
		r.mouseLeftClick();

		// enter in time
		r.twoDigType(Math.abs(relativeSeconds));
		// try {
		// Thread.sleep(SHORT_INPUT_DELAY);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		if (relativeSeconds < 0)
			r.mouseMove(ScreenPoint.SEEK_LEFT.x, ScreenPoint.SEEK_LEFT.y);
		else
			r.mouseMove(ScreenPoint.SEEK_RIGHT.x, ScreenPoint.SEEK_RIGHT.y);
		r.mouseLeftClick();

		currentTime += relativeSeconds;
	}

	private String doScaledOCR(Rectangle rect) throws TesseractException {

		// screen capture
		BufferedImage img = r.createScreenCapture(rect);

		// resize image
		BufferedImage largerImg = new BufferedImage(img.getWidth() * SCALE,
				img.getHeight() * SCALE, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) largerImg.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(img, 0, 0, largerImg.getWidth(), largerImg.getHeight(), null);

		// do OCR
		return Tesseract.getInstance().doOCR(largerImg);
	}

	private int readItemNumber() {

		try {
			Thread.sleep(LONG_INPUT_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Rectangle ITEM_NUMBER_RECT = new Rectangle(ScreenPoint.ITEM_NUMBER_TOP_LEFT.x,
					ScreenPoint.ITEM_NUMBER_TOP_LEFT.y, ScreenPoint.ITEM_NUMBER_BOT_RIGHT.x
							- ScreenPoint.ITEM_NUMBER_TOP_LEFT.x,
					ScreenPoint.ITEM_NUMBER_BOT_RIGHT.y - ScreenPoint.ITEM_NUMBER_TOP_LEFT.y);

			String raw = doScaledOCR(ITEM_NUMBER_RECT); // get raw OCR
			String rawDigits = raw.replaceAll("\\D", "");

			if (rawDigits.length() == ITEM_NUMBER_LENGTH)
				return Integer.parseInt(rawDigits);

		} catch (TesseractException e) {
			e.printStackTrace();
		}

		// return 0 if no digits found
		return 0;
	}

	private void createSequence(int itemNumber) throws InterruptedException {

		Thread.sleep(LONG_INPUT_DELAY);
		r.mouseLeftClick(ScreenPoint.CLIP_OUT);
		Thread.sleep(LONG_INPUT_DELAY);
		r.mouseLeftClick(ScreenPoint.CLIP_NAME_EDIT);
		r.numType(itemNumber);
		r.mouseLeftClick(ScreenPoint.CLIP_SAVE);
		Thread.sleep(LONG_INPUT_DELAY);
		Thread.sleep(LONG_INPUT_DELAY);
		Thread.sleep(LONG_INPUT_DELAY);

	}

	private void signalCompletion() {
		try {
			Clip clip = AudioSystem.getClip();
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(CuratorAutoCutter.class
					.getResourceAsStream("/Electronic_Chime-KevanGC-495939803.wav"));
			clip.open(inputStream);
			FloatControl gainControl = (FloatControl) clip
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-10.0f); // Reduce volume by 10 decibels.
			clip.start();
			Thread.sleep(1000);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}

class CuratorSegment {

	private int itemNumber, firstFrame, lastFrame;

	public CuratorSegment(int itemNumber, int start) {
		this.itemNumber = itemNumber;
		this.firstFrame = start;
	}

	public int getItemNumber() {
		return itemNumber;
	}

	public int getFirstFrame() {
		return firstFrame;
	}

	public int getLastFrame() {
		return lastFrame;
	}

	public void endSegmentAt(int end) {
		this.lastFrame = end;
	}

	public String getln() {
		return String.format("%d:\t%05d-%05d", itemNumber, firstFrame, lastFrame);
	}

}

class CuratorTyperRobot extends Robot {

	private static final int[] DIG_KEY_CODES = { KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2,
			KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
			KeyEvent.VK_8, KeyEvent.VK_9 };

	private Point originalPoint;

	public CuratorTyperRobot() throws AWTException {
		super();
	}

	public void keyType(int keyCode) {
		keyPress(keyCode);
		delay(20);
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

	public void twoDigType(int i) {
		if (i > 9) {
			numType(i);
		} else {
			digType(0);
			digType(i);
		}
	}

	public void mouseLeftClick() {
		mousePress(InputEvent.BUTTON1_MASK);
		mouseRelease(InputEvent.BUTTON1_MASK);
	}

	public void mouseLeftClick(ScreenPoint p) {
		mouseMove(p.x, p.y);
		mouseLeftClick();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void rememberCursor() {
		originalPoint = MouseInfo.getPointerInfo().getLocation();
	}

	public void returnCursor() {
		mouseMove(originalPoint.x, originalPoint.y);
	}
}

enum ScreenPoint {
	FULLSCREEN(1242, 54),
	ITEM_NUMBER_TOP_LEFT(1401, 206),
	ITEM_NUMBER_BOT_RIGHT(1517, 231),
	RELATIVE_TIME_SEEK(1050, 410),
	TIMECODE_SECONDS(1203, 410),
	TIMECODE_FRAMES(1228, 410),
	SEEK_LEFT(1128, 410),
	SEEK_RIGHT(1263, 410),
	CLIP_RESET(959, 479),
	CLIP_CREATE(792, 542),
	CLIP_IN(867, 539),
	CLIP_OUT(1180, 543),
	CLIP_NAME_EDIT(1177, 595),
	CLIP_SAVE(790, 611),
	CLIP_CLOSE(863, 606),
	CLIP_EDIT(295, 96),
	NEW_PROJECT(238, 95);

	int x, y;

	ScreenPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
