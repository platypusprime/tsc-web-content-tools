package ca.tsc.auto_cutter;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.table.DefaultTableModel;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import ca.tsc.auto_cutter.ui.AutoCutterUI;

/**
 * A bot which uses OCR to automatically cut an hour in Adobe Premiere based on
 * pricing graphic changes. Requires use of Joel's Workspace since interface
 * locations are hard-coded.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v0.0:</dt>
 * <dd>14-09-21 - Implemented basic OCR functionality</dd>
 * <dd>14-10-11 - Tuned OCR and made framework for sequence creation</dd>
 * <dd>14-10-13 - Minor changes</dd>
 * <dd>14-10-21 - Changed seeking to frames, exponential graining</dd>
 * <dd>14-10-25 - Debugged new seeking algorithm</dd>
 * <dt>v1.0:</dt>
 * <dd>14-11-08 - Added VOD report reading</dd>
 * <dd>14-11-09 - Added sound on completion</dd>
 * <dd>14-11-22 - Updated constants for Curator video</dd>
 * <dd>14-11-29 - Minor changes</dd>
 * <dd><b>15-02-21 - Added pause before autocutting</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since September 21, 2014
 * @version 1.0.3
 */
public class AutoCutter {

	// clipping times
	private static final int MAX_STEP_SIZE = 25 * 32;
	private static final int REFINE_ITERATIONS = 5;

	// time delays
	private static final long FRAME_RENDER_DELAY = 500;
	private static final long LONG_INPUT_DELAY = 500;
	private static final long SHORT_INPUT_DELAY = 250;

	// OCR constants
	private static final int SCALE = 4;
	private static final int ITEM_NUMBER_LENGTH = 6;

	public static ArrayList<Segment> scan(int clipLength,
			Vector<Vector<Object>> excelData, AutoCutterUI ui,
			AutoCutterScreenCoords coords) {

		try {
			CutterRobot r = new CutterRobot();
			ArrayList<Segment> segments = new ArrayList<Segment>();
			int prevNum = 0, curNum = 0, fineNum = 0;

			// go to first frame
			r.seekFrame(0, coords.getFullTimeSeekPoint());
			Thread.sleep(1500);

			// update UI
			ui.progressBar.setValue(0);
			ui.progressBar.setMaximum(clipLength);
			((DefaultTableModel) ui.resultsTable.getModel()).setRowCount(0);

			// scrub through frames
			for (int frame = 0; frame <= clipLength; frame += MAX_STEP_SIZE) {

				// check for escape
				if (MouseInfo.getPointerInfo().getLocation().y < 100)
					return null;

				ui.progressBar.setValue(frame);

				curNum = readItemNumber(frame, coords.getItemNumberRect(),
						coords.getFullTimeSeekPoint());

				// detect number changes
				if (curNum != 0 && (curNum != prevNum || prevNum == 0)) {

					// if it isn't the first clip, fine cut
					if (!segments.isEmpty()) {

						int currentStepSize = (int) (MAX_STEP_SIZE * -.5);
						int currentTime = frame;
						for (int i = 0; i < REFINE_ITERATIONS; i++) {
							currentTime += currentStepSize;
							fineNum = readItemNumber(currentTime,
									coords.getItemNumberRect(),
									coords.getFullTimeSeekPoint());
							if (fineNum != curNum)
								currentStepSize = (int) (Math.abs(currentStepSize) * .5);
							else
								currentStepSize = (int) (Math.abs(currentStepSize) * -.5);
						}
						frame = currentTime;
					}

					// end previous segment if it exists
					if (!segments.isEmpty()) {
						segments.get(segments.size() - 1).endSegmentAt(frame);
						DefaultTableModel model = (DefaultTableModel) ui.resultsTable.getModel();
						model.setValueAt(frame, model.getRowCount() - 1, 2);
					}

					// add new segment
					segments.add(Segment.buildSegment(curNum, frame, excelData));
					DefaultTableModel model = (DefaultTableModel) ui.resultsTable.getModel();
					Object[] row = { curNum, frame, null, "Clip located" };
					model.addRow(row);

					prevNum = curNum;
				}
			}
			segments.get(segments.size() - 1).endSegmentAt(clipLength);

			DefaultTableModel model = (DefaultTableModel) ui.resultsTable.getModel();
			model.setValueAt(clipLength, model.getRowCount() - 1, 2);
			ui.progressBar.setValue(clipLength);

			return segments;

		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TesseractException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void cut(ArrayList<Segment> segments, AutoCutterUI ui,
			AutoCutterScreenCoords coords) {

		try {
			DefaultTableModel model = (DefaultTableModel) ui.resultsTable.getModel();
			ui.progressBar.setValue(0);
			ui.progressBar.setMaximum(segments.size());
			for (int i = 0; i < segments.size(); i++) {
				createSequence(segments.get(i), coords.getSmallTimeSeekPoint(),
						coords.getSourcePoint(), coords.getSequencePoint());
				model.setValueAt("Clip cut", i, 3);
				ui.progressBar.setValue(i + 1);
				Thread.sleep(LONG_INPUT_DELAY);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		signalCompletion();
	}

	private static String doScaledOCR(Rectangle rect)
			throws TesseractException, AWTException {

		// screen capture
		BufferedImage img = new CutterRobot().createScreenCapture(rect);

		// resize image
		BufferedImage largerImg = new BufferedImage(img.getWidth() * SCALE,
				img.getHeight() * SCALE, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) largerImg.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(img, 0, 0, largerImg.getWidth(), largerImg.getHeight(),
				null);

		// do OCR
		return Tesseract.getInstance().doOCR(largerImg);
	}

	private static int readItemNumber(int frame, Rectangle itemNumRect,
			Point timeSeekPt)
			throws AWTException, InterruptedException, TesseractException {

		// navigate to frame
		new CutterRobot().seekFrame(frame, timeSeekPt);

		Thread.sleep(FRAME_RENDER_DELAY);

		String raw = doScaledOCR(itemNumRect); // get raw OCR
		String rawDigits = raw.replaceAll("\\D", "");

		if (rawDigits.length() == ITEM_NUMBER_LENGTH)
			return Integer.parseInt(rawDigits);

		// return 0 if no digits found
		return 0;
	}

	private static void createSequence(Segment segment, Point timeSeekPt,
			Point srcPt, Point seqPt)
			throws InterruptedException, AWTException {

		// System.out.println(segment.getln()); // TODO remove println

		CutterRobot r = new CutterRobot();

		// create new sequence
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyType(KeyEvent.VK_N);
		r.keyRelease(KeyEvent.VK_CONTROL);

		Thread.sleep(LONG_INPUT_DELAY);

		// type sequence name
		String status = segment.getStatus();

		if (status != null && (status.equals("*") || status.equals("S/O"))) {
			// prefix a '*'
			r.keyPress(KeyEvent.VK_SHIFT);
			r.keyType(KeyEvent.VK_8);
			r.keyRelease(KeyEvent.VK_SHIFT);
		} else if (status != null && !status.equals("")) {
			// prefix a '!'
			r.keyPress(KeyEvent.VK_SHIFT);
			r.keyType(KeyEvent.VK_1);
			r.keyRelease(KeyEvent.VK_SHIFT);
		}
		r.numType(segment.getItemNumber());
		r.keyPress(KeyEvent.VK_SHIFT);
		r.keyType(KeyEvent.VK_MINUS); // UNDERSCORE
		r.keyType(KeyEvent.VK_V); // V
		r.keyType(KeyEvent.VK_O); // O
		r.keyType(KeyEvent.VK_D); // D
		r.keyRelease(KeyEvent.VK_SHIFT);
		r.digType(segment.getRename());
		r.keyType(KeyEvent.VK_ENTER);

		// set markers
		Thread.sleep(LONG_INPUT_DELAY);
		r.seekFrame(segment.getFirstFrame(), timeSeekPt);
		Thread.sleep(SHORT_INPUT_DELAY);
		r.keyType(KeyEvent.VK_I);
		Thread.sleep(LONG_INPUT_DELAY);
		r.seekFrame(segment.getLastFrame(), timeSeekPt);
		Thread.sleep(SHORT_INPUT_DELAY);
		r.keyType(KeyEvent.VK_O);

		// drag clip over to sequence
		Thread.sleep(LONG_INPUT_DELAY);
		r.mouseMove(srcPt.x, srcPt.y);
		r.mousePress(InputEvent.BUTTON1_MASK);
		Thread.sleep(LONG_INPUT_DELAY);
		r.mouseMove(srcPt.x, seqPt.y);
		r.mouseMove(seqPt.x, seqPt.y);
		r.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	private static void signalCompletion() {

		try {
			Clip clip = AudioSystem.getClip();
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(AutoCutter.class
					.getResourceAsStream("/Electronic_Chime-KevanGC-495939803.wav"));
			clip.open(inputStream);
			FloatControl gainControl = (FloatControl) clip
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-10.0f); // Reduce volume by 10 decibels.
			clip.start();

			Thread.sleep(1000); // allow clip to play

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}