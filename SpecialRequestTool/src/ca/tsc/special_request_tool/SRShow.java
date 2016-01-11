package ca.tsc.special_request_tool;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ca.tsc.special_request_tool.spread_parser.SpreadParser;
import ca.tsc.util.web.Show;

public class SRShow extends Show {

	private static final String SPREADSHEET_FORMAT = "\t\t%s\t%d\t%s\t%s\t%s\n";
	private static final String EMAIL_FORMAT = "%s (%d)\nDue Date: %s\nAir Date: %s\n\n";
	private static final SimpleDateFormat EMAIL_DATE_FORMAT = new SimpleDateFormat(
			"MMM dd");

	private String[] mandatory, preferred;
	private File file = null;
	private Commodity commodity = Commodity.UNKNOWN; // determined from path

	// all item numbers parsed from the associated spread
	private ArrayList<Item> items = new ArrayList<Item>();
	private boolean itemsRead = false;

	/**
	 * Constructor for shows pulled from ScriptSure
	 * 
	 * @param name
	 * @param scriptNum
	 */
	public SRShow(String name, String scriptNum) {
		super(name, scriptNum, null);

		this.mandatory = name.split(" ");
		String[] preferred = { "TAURUS" };
		this.preferred = preferred;
	}

	/**
	 * Constructor for shows pulled from a H&E spread
	 * 
	 * @param file
	 * @param name
	 * @param airDate
	 */
	public SRShow(String name, Calendar airDate, File file) {
		super(name, airDate, null);

		this.file = file;
	}

	public SRShow(Show show) {
		super(show.getName(), show.getAirDate(), show.getURI());
	}

	public void chooseSpread(ArrayList<File> files) {

		if (file != null)
			return;

		float highScore = 0;

		for (File file : files) {
			float score = scoreSpread(file);
			if (score > highScore) {
				this.setFile(file);
				highScore = score;

			} else if (score != 0 && score == highScore
					&& file.lastModified() > this.getFile().lastModified()) {
				this.setFile(file);
			}
		}
	}

	private float scoreSpread(File file) {

		// TODO improve spread scoring

		String filename = file.getName()
				.toLowerCase()
				.replaceAll("clearance", "");

		float output = 0;

		for (String s : mandatory)
			if (filename.contains(s.toLowerCase()))
				output += 100.0 / mandatory.length;

		// check if at least half the keywords are included
		if (output < 50)
			return 0;

		for (String s : preferred) {
			if (filename.contains(s.toLowerCase()))
				output += 1.0 / preferred.length;
		}

		return output;
	}

	public String getFormattedAirDate() {
		return EMAIL_DATE_FORMAT.format(getAirDate().getTime());

	}

	public String getFormattedDueDate() {
		return EMAIL_DATE_FORMAT.format(getDueDate().getTime());
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {

		if (file != null && file.equals(this.file))
			return;

		this.file = file;

		// determine commodity
		if (file != null)
			for (Commodity commodity : Commodity.values())
				if (getFile().getAbsolutePath().contains(
						commodity.getFilePath()))
					this.commodity = commodity;

		items.clear();
		itemsRead = false;
	}

	public Commodity getCommodity() {
		return commodity;
	}

	public void setCommodity(Commodity commodity) {
		this.commodity = commodity;
	}

	public ArrayList<Item> getItems() {
		if (!itemsRead) {
			items = SpreadParser.parseSpread(this);
			itemsRead = true;
		}
		return items;
	}

	public void updateFlags(ArrayList<Integer> referenceList) {
		for (Item item : items)
			if (!item.flagOverriden())
				item.setFlag(!referenceList.contains(item.getItemNumber()));
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
		itemsRead = true;
	}

	public String getSpreadSheetString() {

		String output = "";

		for (Item item : items) {
			if (item.getFlag())
				output = output.concat(String.format(SPREADSHEET_FORMAT,
						item.getCommodity().toString(), item.getItemNumber(),
						getName(), getFormattedDueDate(), getFormattedAirDate()));
		}

		return output;
	}

	public String getEmailString() {

		int count = 0;
		for (Item item : items)
			if (item.getFlag())
				count++;

		if (count == 0)
			return null;

		return String.format(EMAIL_FORMAT, getName(), count,
				getFormattedDueDate(),
				getFormattedAirDate());
	}

}
