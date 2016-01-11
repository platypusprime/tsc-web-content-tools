package ca.tsc.special_request_tool.spread_parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DateUtil;

import ca.tsc.special_request_tool.Commodity;
import ca.tsc.special_request_tool.Item;
import ca.tsc.special_request_tool.SRShow;

public class SpreadParser {

	private static final Pattern DATE_PATTERN = Pattern.compile("\\D+\\d{1,2}\\D+");
	private static final Pattern YEAR_PATTERN = Pattern.compile("\\D+\\d{4}\\D+");

	// // FOR TESTING
	// public static void main(String[] args) {
	//
	// System.out.println("TESTING XLS");
	//
	// // XLS TESTING
	// // HSSFRipper xlsParser = new HSSFRipper(new File(
	// // "C:/Users/Joel/Desktop/Spreads/Jewellery/AMY KAHN R 2014.xls"));
	// HSSFExtractor xlsExtractor = new HSSFExtractor(new File(
	// "J:/Merchandise Planner/Show Spreads/Jewellery/2014/VICENZA GOLD  2014.xls"));
	// // HSSFExtractor xlsExtractor = new HSSFExtractor(new File(
	// // "sr_test_spreads/BACKJOY DPM RPT 2014.xls"));
	// ArrayList<String> xlsSheetNames = xlsExtractor.extractSheetNames();
	// for (String s : xlsSheetNames) {
	// System.out.println(s);
	// }
	//
	// // ArrayList<SheetData> xlsSheets = xlsExtractor.extractData();
	// // for (SheetData sheet : xlsSheets) {
	// //
	// // ArrayList<Integer> cells = sheet.getBoldedItemNumbers(2);
	// // System.out.println(sheet.getSheetName());
	// // for (Integer cell : cells)
	// // System.out.println(cell);
	// // }
	//
	// System.out.println("\nTESTING XLSX");
	//
	// // XLSX TESTING
	// XSSFExtractor xlsxExtractor = new XSSFExtractor(new File(
	// "sr_test_spreads/ORANGE - SHOWSPREAD - TAURUS NEW 3.xlsm"));
	//
	// ArrayList<String> xlsxSheetNames = xlsxExtractor.extractSheetNames();
	// for (String s : xlsxSheetNames) {
	// System.out.println(s);
	// }
	//
	// // ArrayList<SheetData> xlsxSheets = xlsxExtractor.extractData();
	// // for (SheetData sheet : xlsxSheets) {
	// //
	// // ArrayList<Integer> cells = sheet.getBoldedItemNumbers(1);
	// // System.out.println(sheet.getSheetName());
	// //
	// // for (Integer cell : cells)
	// // System.out.println(cell);
	// // }
	// }

	public static ArrayList<String> getSheetList(SRShow show) {

		if (show != null && show.getFile() != null) {
			WorkbookExtractor extractor;

			if (show.getFile().getName().endsWith("xls"))
				extractor = new HSSFExtractor(show.getFile());
			else
				extractor = new XSSFExtractor(show.getFile());

			return extractor.extractSheetNames();
		}

		return new ArrayList<String>();
	}

	private static ArrayList<SheetData> extractCells(File file) {

		System.out.printf("Reading cell data from workbook: %s...",
				file.getName());
		WorkbookExtractor extractor;

		if (file.getName().endsWith("xls"))
			extractor = new HSSFExtractor(file);
		else
			extractor = new XSSFExtractor(file);

		ArrayList<SheetData> output = extractor.extractData();
		System.out.println("DONE!");
		return output;
	}

	public static ArrayList<Item> parseSpread(SRShow show) {

		ArrayList<Item> items = new ArrayList<Item>();
		ArrayList<Integer> itemNums = new ArrayList<Integer>();

		if (show != null && show.getFile() != null) {

			ArrayList<SheetData> sheets = extractCells(show.getFile());

			sheetLoop: for (SheetData sheet : sheets)
				switch (show.getCommodity()) {

				case FASHION:
					// check sheet name and cell A3 for show date
					if (!sheet.getSheetName().contains("show summary")
							&& !sheet.getSheetName().contains("Vendor")
							&& (dateOK(sheet.getSheetName(), show,
									Calendar.DATE, Calendar.MONTH,
									Calendar.YEAR) || (sheet.getCellDataAt(1, 3) != null && dateOK(
									sheet.getCellDataAt(1, 3).toString(), show,
									Calendar.DATE,
									Calendar.MONTH, Calendar.YEAR)))) {

						System.out.printf(
								"Pulling item numbers from sheet:%s...",
								sheet.getSheetName());
						itemNums = sheet.getItemNumbers(1); // items in A
						System.out.println("DONE!");
						break sheetLoop;
					}
					break;

				case HEALTH_AND_BEAUTY:
					// check cell A2 for show date
					Object dateCell = sheet.getCellDataAt(1, 2);
					if ((dateCell != null && dateOK(dateCell.toString(), show,
							Calendar.DATE,
							Calendar.MONTH, Calendar.YEAR))
							|| ((dateCell != null && parseDateString(dateCell.toString()) == null) || dateCell == null
									&& dateOK(sheet.getSheetName(), show,
											Calendar.MONTH))) {

						// set defaults
						int itemNumberColumn = 3;
						int itemNameColumn = 5;

						// find correct columns
						int columnsFound = 0;
						for (CellData cell : sheet.getCells()) {
							if (cell.data.toString().contains("ITEM #")) {
								itemNumberColumn = cell.col;
								columnsFound++;
							} else if (cell.data.toString().contains(
									"DESCRIPTION")) {
								itemNameColumn = cell.col;
								columnsFound++;
							}

							// found everything we need
							if (columnsFound >= 2)
								break;
						}

						System.out.printf(
								"Pulling item numbers from sheet:%s...",
								sheet.getSheetName());
						itemNums = sheet.getItemNumbers(itemNumberColumn,
								itemNameColumn, "K1",
								"ADP");
						System.out.println("DONE!");
						break sheetLoop;
					}

					break;

				case JEWELLERY:

					// check sheet name for show date
					if (dateOK(sheet.getSheetName(), show, Calendar.MONTH)) {

						System.out.printf(
								"Pulling item numbers from sheet:%s...",
								sheet.getSheetName());
						itemNums = sheet.getBoldedItemNumbers(2); // items in B
						System.out.println("DONE!");
						break sheetLoop;
					}
					break;

				default:
					break;
				}
		}
		System.out.print("Creating items...");
		for (int itemNum : itemNums) {
			Item newItem = new Item(itemNum, show);
			if (!items.contains(newItem))
				items.add(newItem);
		}
		System.out.println("DONE!");
		return items;
	}

	public static ArrayList<SRShow> parseHEBook(File file) {

		ArrayList<SRShow> shows = new ArrayList<SRShow>();
		if (file == null)
			return shows;
		ArrayList<SheetData> sheets = extractCells(file);
		Commodity commodity = file.getName().contains("HOME") ? Commodity.HOME
				: Commodity.ELECTRONICS;

		for (SheetData sheet : sheets) {
			String sheetName = sheet.getSheetName();

			// check for any red flags in the sheet name
			if (sheetName.toLowerCase().contains("delete")
					|| sheetName.matches("^Sheet\\d+$"))
				break;

			// read show name and date from spreadsheet
			Object nameData = sheet.getCellDataAt(3, 3);
			String showName = nameData != null ? nameData.toString()
					: sheetName;
			Object dateData = sheet.getCellDataAt(3, 1);
			Calendar showDate = dateData != null ? parseDateString(dateData.toString())
					: parseDateString(sheetName);
			if (showDate == null)
				continue;

			SRShow show = new SRShow(showName, showDate, file);
			show.setCommodity(commodity);
			ArrayList<Item> items = new ArrayList<Item>();

			System.out.printf("Pulling item numbers from sheet:%s...",
					sheet.getSheetName());
			ArrayList<Integer> itemNums = sheet.getItemNumbers(2);
			System.out.println("DONE!");

			System.out.print("Creating items...");
			for (int itemNum : itemNums) {
				Item newItem = new Item(itemNum, show);
				if (!items.contains(newItem))
					items.add(newItem);
			}
			System.out.println("DONE!");
			show.setItems(items);
			shows.add(show);
		}

		return shows;
	}

	private static boolean dateOK(String rawDate, SRShow show, int... fields) {
		System.out.printf("Checking sheet: %s\n", rawDate);

		Calendar showDate = Calendar.getInstance();
		showDate.setTime(show.getAirDate().getTime());

		Calendar parsedDate = parseDateString(rawDate);
		if (parsedDate == null)
			return false;

		parsedDate.add(Calendar.DATE, -2);
		for (int i = 0; i < 5; i++) {
			boolean matches = true;
			for (int field : fields) {
				if (parsedDate.get(field) != parsedDate.get(field))
					matches = false;
			}
			if (matches)
				return true;
			parsedDate.add(Calendar.DATE, 1);
		}
		return false;
	}

	private static Calendar parseDateString(String rawDate) {

		// look for month
		for (Month month : Month.values())
			if (month.matches(rawDate)) {
				Calendar cal = Calendar.getInstance();

				cal.set(Calendar.MONTH, month.num - 1);

				// look for date
				final Matcher dateMatcher = DATE_PATTERN.matcher(rawDate);
				if (dateMatcher.find())
					cal.set(Calendar.DATE,
							Integer.parseInt(dateMatcher.group().replaceAll(
									"\\D", "")));

				// look for year
				final Matcher yearMatcher = YEAR_PATTERN.matcher(rawDate);
				if (yearMatcher.find())
					cal.set(Calendar.YEAR,
							Integer.parseInt(yearMatcher.group().replaceAll(
									"\\D", "")));

				return cal;
			}

		// at this point check if we've got an EXCEL date
		try {
			return DateUtil.getJavaCalendar(Double.parseDouble(rawDate), false);
		} catch (NumberFormatException e) {
			System.err.printf("Couldn't parse date from string: %s\n", rawDate);
			return null;
		}

	}

	private enum Month {

		JANUARY(1),
		FEBRUARY(2),
		MARCH(3),
		APRIL(4),
		MAY(5),
		JUNE(6),
		JULY(7),
		AUGUST(8),
		SEPTEMBER(9),
		OCTOBER(10),
		NOVEMBER(11),
		DECEMBER(12);

		final int num;
		final String pat1, pat2;

		private Month(int num) {
			this.num = num;
			pat1 = "[^A-Z]*" + this.toString().substring(0, 3) + ".*";
			pat2 = ".+[^A-Z]+" + this.toString().substring(0, 3) + ".*";
		}

		private boolean matches(String s) {
			return s.toUpperCase().matches(pat1)
					|| s.toUpperCase().matches(pat2);
		}
	}

}
