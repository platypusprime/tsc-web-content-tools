package ca.tsc.auto_cutter;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AutoCutterScreenCoords {

	private static final Rectangle DEFAULT_ITEM_NUMBER_RECT = new Rectangle(447, 204, 647 - 447,
			230 - 204);
	private static final Point DEFAULT_FULL_TIME_SEEK_LOCATION = new Point(39, 920);
	private static final Point DEFAULT_SMALL_TIME_SEEK_LOCATION = new Point(405, 638);
	private static final Point DEFAULT_SOURCE_LOCATION = new Point(1135, 354);
	private static final Point DEFAULT_SEQUENCE_LOCATION = new Point(125, 906);

	private Rectangle itemNumberRect;
	private Point fullTimeSeekPoint, smallTimeSeekPoint, sourcePoint, sequencePoint;

	public AutoCutterScreenCoords(Rectangle itemNumberRect, Point fullTimeSeekPoint,
			Point smallTimeSeekPoint, Point sourcePoint, Point sequencePoint) {
		this.itemNumberRect = new Rectangle(itemNumberRect);
		this.fullTimeSeekPoint = new Point(fullTimeSeekPoint);
		this.smallTimeSeekPoint = new Point(smallTimeSeekPoint);
		this.sourcePoint = new Point(sourcePoint);
		this.sequencePoint = new Point(sequencePoint);

		load();
	}

	public AutoCutterScreenCoords() {
		this(DEFAULT_ITEM_NUMBER_RECT, DEFAULT_FULL_TIME_SEEK_LOCATION,
				DEFAULT_SMALL_TIME_SEEK_LOCATION, DEFAULT_SOURCE_LOCATION,
				DEFAULT_SEQUENCE_LOCATION);
	}

	public Rectangle getItemNumberRect() {
		return itemNumberRect;
	}

	public void setItemNumberRect(Rectangle itemNumberRect) {
		this.itemNumberRect = itemNumberRect;
		save();
	}

	public Point getFullTimeSeekPoint() {
		return fullTimeSeekPoint;
	}

	public void setFullTimeSeekPoint(Point fullTimeSeekPoint) {
		this.fullTimeSeekPoint = fullTimeSeekPoint;
		save();
	}

	public void setFullTimeSeekPoint(Rectangle r) {
		setFullTimeSeekPoint(new Point(r.x + r.width / 2, r.y + r.height / 2));
	}

	public Point getSmallTimeSeekPoint() {
		return smallTimeSeekPoint;
	}

	public void setSmallTimeSeekPoint(Point smallTimeSeekPoint) {
		this.smallTimeSeekPoint = smallTimeSeekPoint;
		save();
	}

	public void setSmallTimeSeekPoint(Rectangle r) {
		setSmallTimeSeekPoint(new Point(r.x + r.width / 2, r.y + r.height / 2));
	}

	public Point getSourcePoint() {
		return sourcePoint;
	}

	public void setSourcePoint(Point sourcePoint) {
		this.sourcePoint = sourcePoint;
		save();
	}

	public void setSourcePoint(Rectangle r) {
		setSourcePoint(new Point(r.x + r.width / 2, r.y + r.height / 2));
	}

	public Point getSequencePoint() {
		return sequencePoint;
	}

	public void setSequencePoint(Point sequencePoint) {
		this.sequencePoint = sequencePoint;
		save();
	}

	public void setSequencePoint(Rectangle r) {
		setSequencePoint(new Point(r.x + r.width / 2, r.y + r.height / 2));
	}

	public void reset() {

		this.itemNumberRect = new Rectangle(DEFAULT_ITEM_NUMBER_RECT);
		this.fullTimeSeekPoint = new Point(DEFAULT_FULL_TIME_SEEK_LOCATION);
		this.smallTimeSeekPoint = new Point(DEFAULT_SMALL_TIME_SEEK_LOCATION);
		this.sourcePoint = new Point(DEFAULT_SOURCE_LOCATION);
		this.sequencePoint = new Point(DEFAULT_SEQUENCE_LOCATION);

		save();
	}

	private void save() {
		File f = new File("autocutter.config");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));

			if (!itemNumberRect.equals(DEFAULT_ITEM_NUMBER_RECT)) {
				writer.write(String.format("item_number_rect:%d,%d,%d,%d", itemNumberRect.x,
						itemNumberRect.y, itemNumberRect.width, itemNumberRect.height));
				writer.newLine();
			}

			if (!fullTimeSeekPoint.equals(DEFAULT_FULL_TIME_SEEK_LOCATION)) {
				writer.write(String.format("full_seek_pt:%d,%d", fullTimeSeekPoint.x,
						fullTimeSeekPoint.y));
				writer.newLine();
			}

			if (!smallTimeSeekPoint.equals(DEFAULT_SMALL_TIME_SEEK_LOCATION)) {
				writer.write(String.format("small_seek_pt:%d,%d", smallTimeSeekPoint.x,
						smallTimeSeekPoint.y));
				writer.newLine();
			}

			if (!sourcePoint.equals(DEFAULT_SOURCE_LOCATION)) {
				writer.write(String.format("source_pt:%d,%d", sourcePoint.x, sourcePoint.y));
				writer.newLine();
			}

			if (!sequencePoint.equals(DEFAULT_SEQUENCE_LOCATION)) {
				writer.write(String.format("sequence_pt:%d,%d", sequencePoint.x, sequencePoint.y));
				writer.newLine();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void load() {
		File f = new File("autocutter.config");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));

			String ln;
			while ((ln = reader.readLine()) != null) {
				String[] split1 = ln.split(":");
				String[] split2 = split1[1].split(",");
				if (split1[0].equals("item_number_rect")) {
					setItemNumberRect(new Rectangle(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1]), Integer.parseInt(split2[2]),
							Integer.parseInt(split2[3])));
				} else if (split1[0].equals("full_seek_pt")) {
					setFullTimeSeekPoint(new Point(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1])));
				} else if (split1[0].equals("small_seek_pt")) {
					setSmallTimeSeekPoint(new Point(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1])));
				} else if (split1[0].equals("source_pt")) {
					setSourcePoint(new Point(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1])));
				} else if (split1[0].equals("sequence_pt")) {
					setSequencePoint(new Point(Integer.parseInt(split2[0]),
							Integer.parseInt(split2[1])));
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}