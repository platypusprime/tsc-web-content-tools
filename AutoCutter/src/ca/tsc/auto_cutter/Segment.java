package ca.tsc.auto_cutter;

import java.util.Vector;

public class Segment {

	private final int itemNumber;
	private final int firstFrame;
	private int lastFrame;
	private final String status;
	private final int rename;

	public Segment(int itemNumber, int start, String status, int rename) {
		this.itemNumber = itemNumber;
		this.firstFrame = start;
		this.status = status;
		this.rename = rename;
	}

	public static Segment buildSegment(int itemNumber, int start,
			Vector<Vector<Object>> data) {

		String status = null;
		int rename = 1;

		for (int row = 0; row < data.size(); row++) {
			if (Integer.parseInt((String) data.get(row).get(4)) == itemNumber) {
				status = (String) data.get(row).get(2);

				String renameStr = (String) data.get(row).get(1);
				try {
					rename = renameStr == null ? 1
							: Integer.parseInt(renameStr);
				} catch (NumberFormatException e) {
					rename = 1;
				}
				break;
			}
		}

		return new Segment(itemNumber, start, status, rename);
	}

	public void endSegmentAt(int end) {
		this.lastFrame = end;
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

	public String getStatus() {
		return status;
	}

	public int getRename() {
		return rename;
	}

	@Override
	public String toString() {
		return String.format("%d:\t%05d-%05d", itemNumber, firstFrame,
				lastFrame);
	}

}