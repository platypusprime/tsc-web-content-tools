package ca.tsc.special_request_tool;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ca.tsc.special_request_tool.main_ui.SRTMainInterface;
import ca.tsc.util.web.ScriptSureRequests;
import ca.tsc.util.web.Show;

/**
 * GUI for generating SR reports. Automatically retrieves and parses show
 * schedules, show spreads, and WebFactory reports. Outputs on interface, for
 * spreadsheet, and for e-mail.
 * 
 * @author Jingchen Xu
 * @since Jan 04, 2015
 * @version 0.4.2
 */
public class SpecialRequestTool {

	public static void main(String args[]) {

		// set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// instantiate GUI
		new SRTMainInterface();
	}

	// set to ignore files older than 3 months
	private static final Calendar FILE_AGE_THRESHOLD = Calendar.getInstance();
	static {
		FILE_AGE_THRESHOLD.add(Calendar.MONTH, -3);
	}

	public static ArrayList<File> getSpreads() {

		ArrayList<File> spreads = new ArrayList<File>();
		for (Commodity commodity : Commodity.values()) {
			spreads.addAll(getSpreads(commodity));
		}
		return spreads;
	}

	private static ArrayList<File> getSpreads(Commodity commodity) {
		switch (commodity) {
		case FASHION:
		case HEALTH_AND_BEAUTY:
		case JEWELLERY:
			ArrayList<File> spreads = getSpreads(commodity.getFile());
			return spreads;
		default:
			return new ArrayList<File>();
		}
	}

	private static ArrayList<File> getSpreads(File directory) {

		ArrayList<File> output = new ArrayList<File>();
		File[] files = directory.listFiles();

		for (File file : files) {
			if (file.isDirectory())
				output.addAll(getSpreads(file));

			// check file name/type
			else if (file.getName().contains(".xls")
					&& !file.getName().toLowerCase().contains("do not use")
					&& !file.getName().startsWith("~$")) {

				// check file age
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(file.lastModified());
				if (cal.after(FILE_AGE_THRESHOLD))
					output.add(file);
			}

		}

		return output;
	}

	public static ArrayList<SRShow> getShows(String user, String pass,
			Calendar startCal,
			Calendar endCal) {

		Calendar curCal = (Calendar) startCal.clone();
		ArrayList<SRShow> shows = new ArrayList<SRShow>();

		try {

			ScriptSureRequests.login(user, pass);

			while (!curCal.after(endCal)) {
				addShowsFromDay(curCal, shows);
				curCal.add(Calendar.DAY_OF_YEAR, 1); // increment day
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return shows;
	}

	private static void addShowsFromDay(Calendar cal,
			ArrayList<SRShow> shows) throws URISyntaxException, IOException {

		ArrayList<Show> newShows = ScriptSureRequests.listShows(cal);
		for (Show newShow : newShows) {
			SRShow newSRShow = new SRShow(newShow);
			if (!shows.contains(newSRShow)) {
				shows.add(newSRShow);
			}
		}
	}
}
