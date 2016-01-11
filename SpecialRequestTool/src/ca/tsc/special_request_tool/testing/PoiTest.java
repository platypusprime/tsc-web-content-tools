package ca.tsc.special_request_tool.testing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class PoiTest {

	final static Pattern p = Pattern.compile("^(\\d{6})$");

	public PoiTest() {
		// http://poi.apache.org/spreadsheet/quick-guide.html
		// http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/eventusermodel/XLSX2CSV.java
	}

	public static void main(String[] args) throws IOException, OpenXML4JException, SAXException {

		ArrayList<Integer> homeList = readHomeElecSheet(new File(
				"sr_test_spreads/HOME NOV 24 - NOV 30.xlsx"));
		System.out.printf("\n%d items found:\n", homeList.size());
		for (int i : homeList) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> elecList = readHomeElecSheet(new File(
				"sr_test_spreads/ELECTRONICS NOV 24 - NOV 30.xlsx"));
		System.out.printf("\n%d items found:\n", elecList.size());
		for (int i : elecList) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> healBeauList = readHealBeauSheet(new File(
				"sr_test_spreads/BACKJOY DPM RPT 2014.xls"));
		System.out.printf("\n%d items found:\n", healBeauList.size());
		for (int i : healBeauList) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> healBeauList2 = readHealBeauSheet(new File(
				"sr_test_spreads/Click n Curl DPM 2014.xls"));
		System.out.printf("\n%d items found:\n", healBeauList2.size());
		for (int i : healBeauList2) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> fashionList = readFashionSheet(new File(
				"sr_test_spreads/MARALLIS - TAURUS NEW2.xlsm"));
		System.out.printf("\n%d items found:\n", fashionList.size());
		for (int i : fashionList) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> fashionList2 = readFashionSheet(new File(
				"sr_test_spreads/ORANGE - SHOWSPREAD - TAURUS NEW 3.xlsm"));
		System.out.printf("\n%d items found:\n", fashionList2.size());
		for (int i : fashionList2) {
			System.out.println(i);
		}

		System.out.println();

		ArrayList<Integer> jewelleryList = readJewellerySheet(new File(
				"sr_test_spreads/Diamonds in Silver 2014.xls"));
		System.out.printf("\n%d items found:\n", jewelleryList.size());
		for (int i : jewelleryList) {
			System.out.println(i);
		}
	}

	public static ArrayList<Integer> readJewellerySheet(File file) throws InvalidFormatException,
			IOException {
		ArrayList<Integer> itemNums = new ArrayList<Integer>();

		System.out.printf("Creating workbook from %s...", file.getName());
		Workbook wb = WorkbookFactory.create(file);
		System.out.println("DONE!");

		// info is at the first sheet for jewellery
		int sheetIndex = 0;
		Sheet sheet = wb.getSheetAt(sheetIndex);

		int lastRow = sheet.getLastRowNum();

		System.out.printf("%d rows found at sheet %d\n", lastRow, sheetIndex);
		System.out.printf("Reading sheet %d...", sheetIndex);
		for (int i = 0; i < lastRow; i++) {
			// data is in B column
			Cell cell = sheet.getRow(i).getCell(1);
			String result = "";

			// only bolded cells are new
			if (cell != null
					&& wb.getFontAt(cell.getCellStyle().getFontIndex()).getBoldweight() == Font.BOLDWEIGHT_BOLD)
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					result = cell.getRichStringCellValue().getString();
					break;
				case Cell.CELL_TYPE_NUMERIC:
				case Cell.CELL_TYPE_FORMULA:
					result = Integer.toString((int) cell.getNumericCellValue());
					break;
				default:
					break;
				}
			final Matcher m = p.matcher(result);
			if (m.find()) {
				int newNum = Integer.parseInt(m.group(0));
				if (!itemNums.contains(newNum))
					itemNums.add(newNum);
			}

		}
		System.out.println("DONE!");
		return itemNums;
	}

	public static ArrayList<Integer> readHealBeauSheet(File file) throws InvalidFormatException,
			IOException {
		ArrayList<Integer> itemNums = new ArrayList<Integer>();

		System.out.printf("Creating workbook from %s...", file.getName());
		Workbook wb = WorkbookFactory.create(file);
		System.out.println("DONE!");

		// info is at the last sheet for H&B
		int sheetIndex = wb.getNumberOfSheets() - 1;
		Sheet sheet = wb.getSheetAt(sheetIndex);

		int lastRow = sheet.getLastRowNum();
		System.out.printf("%d rows found at sheet %d\n", lastRow, sheetIndex);
		System.out.printf("Reading sheet %d...", sheetIndex);
		for (int i = 0; i < lastRow; i++) {
			// data is in B column
			Cell cell = sheet.getRow(i).getCell(1);
			String result = "";

			// TODO check if bolding is required for H&B
			if (cell != null
					&& wb.getFontAt(cell.getCellStyle().getFontIndex()).getBoldweight() == Font.BOLDWEIGHT_BOLD)
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					result = cell.getRichStringCellValue().getString();
					break;
				case Cell.CELL_TYPE_NUMERIC:
				case Cell.CELL_TYPE_FORMULA:
					result = Integer.toString((int) cell.getNumericCellValue());
					break;
				default:
					break;
				}
			final Matcher m = p.matcher(result);
			if (m.find()) {
				int newNum = Integer.parseInt(m.group(0));
				if (!itemNums.contains(newNum))
					itemNums.add(newNum);
			}

		}
		System.out.println("DONE!");
		return itemNums;
	}

	public static ArrayList<Integer> readFashionSheet(File file) throws IOException,
			OpenXML4JException, SAXException {
		ArrayList<Integer> itemNums = new ArrayList<Integer>();

		System.out.printf("Pulling XML from %s...", file.getName());
		OPCPackage pkg = OPCPackage.open(file);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new FashionSheetHandler(sst, itemNums);
		parser.setContentHandler(handler);
		System.out.println("DONE!");

		// rId2 found by processing the Workbook
		// Seems to either be rId# or rSheet#
		System.out.printf("Reading sheet %d...", 1);
		InputStream sheet2 = r.getSheet("rId2");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();
		System.out.println("DONE!");
		return itemNums;
	}

	public static ArrayList<Integer> readHomeElecSheet(File file) throws InvalidFormatException,
			IOException {
		ArrayList<Integer> itemNums = new ArrayList<Integer>();
	
		System.out.printf("Creating workbook from %s...", file.getName());
		Workbook wb = WorkbookFactory.create(file);
		System.out.println("DONE!");
	
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = wb.getSheetAt(sheetIndex);
			if (isWantedHomeElec(sheet)) {
	
				int lastRow = sheet.getLastRowNum();
				System.out.printf("%d rows found at sheet %d\n", lastRow, sheetIndex);
				System.out.printf("Reading sheet %d...", sheetIndex);
				for (int i = 0; i < lastRow; i++) {
					// data is in B column
					Cell cell = sheet.getRow(i).getCell(1);
					String result = "";
	
					if (cell != null)
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							result = cell.getRichStringCellValue().getString();
							if (result.equals("Comments:"))
								break;
							break;
						case Cell.CELL_TYPE_NUMERIC:
						case Cell.CELL_TYPE_FORMULA:
							result = Integer.toString((int) cell.getNumericCellValue());
							break;
						default:
							break;
						}
					final Matcher m = p.matcher(result);
					if (m.find()) {
						int newNum = Integer.parseInt(m.group(0));
						if (!itemNums.contains(newNum))
							itemNums.add(newNum);
					}
				}
				System.out.println("DONE!");
			}
		}
		return itemNums;
	}

	private static boolean isWantedHomeElec(Sheet sheet) {
		if (sheet.getSheetName().matches("^Sheet[0-9]+$"))
			return false;
		// TODO check date
		return true;
	}
}
