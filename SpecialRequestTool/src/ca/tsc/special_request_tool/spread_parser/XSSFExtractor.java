package ca.tsc.special_request_tool.spread_parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XSSFExtractor implements WorkbookExtractor {

	private File file;
	private SharedStringsTable sst;

	// TODO read styles private StylesTable styles;

	public XSSFExtractor(File file) {
		this.file = file;
	}

	@Override
	public ArrayList<String> extractSheetNames() {
		ArrayList<String> sheetNames = new ArrayList<String>();

		SheetIterator sheets = getSheets();
		while (sheets.hasNext()) {
			sheets.next();
			sheetNames.add(sheets.getSheetName());
		}

		return sheetNames;
	}

	@Override
	public ArrayList<SheetData> extractData() {

		ArrayList<SheetData> sheetData = new ArrayList<SheetData>();

		SheetIterator sheets = getSheets();
		while (sheets.hasNext()) {
			InputStream sheet = sheets.next();
			sheetData.add(new SheetData(sheets.getSheetName()));
			try {
				sheetData.get(sheetData.size() - 1).setCells(parseSheet(sheet));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (OpenXML4JException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}

		return sheetData;
	}

	public SheetIterator getSheets() {
		try {
			OPCPackage pkg = OPCPackage.open(file);
			XSSFReader r = new XSSFReader(pkg);
			sst = r.getSharedStringsTable();
			// styles = r.getStylesTable();
			return (SheetIterator) r.getSheetsData();

		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OpenXML4JException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<CellData> parseSheet(InputStream sheet) throws IOException,
			OpenXML4JException, SAXException {

		InputSource sheetSource = new InputSource(sheet);

		// set up parser
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new CellDataHandler(sst);
		parser.setContentHandler(handler);
		parser.parse(sheetSource);

		ArrayList<CellData> data = ((CellDataHandler) handler).getData();
		sheet.close();
		return data;
	}

	private static final Map<Character, Integer> CHAR_NUM_VALS;
	private static final int NUMBER_OF_LETTERS;

	static {
		char[] ls = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		Map<Character, Integer> m = new HashMap<Character, Integer>();
		int j = 0;
		for (char c : ls) {
			m.put(c, j++);
		}
		CHAR_NUM_VALS = m;
		NUMBER_OF_LETTERS = ls.length;
	}

	class CellDataHandler extends DefaultHandler {

		private static final String CELL_NAME = "c";
		private static final String CELL_CONTENT_NAME = "v";

		private SharedStringsTable sst;
		private String lastContents;
		private String cellRef;
		private boolean nextIsString;

		private ArrayList<CellData> data = new ArrayList<CellData>();

		public CellDataHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		public void startElement(String uri, String localName, String name, Attributes attributes)
				throws SAXException {

			if (name.equals(CELL_NAME)) {
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				cellRef = attributes.getValue("r");
				nextIsString = cellType != null && cellType.equals("s");
			}
			// Clear contents cache
			lastContents = "";
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
			// Process the last contents as required.
			// Do now, as characters() may be called more than once
			if (nextIsString) {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
				nextIsString = false;
			}

			if (name.equals(CELL_CONTENT_NAME)) {
				int[] numCellRef = convertCellRef(cellRef);
				data.add(new CellData(numCellRef[0], numCellRef[1], lastContents, (short) 1000));
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			lastContents += new String(ch, start, length);
		}

		public ArrayList<CellData> getData() {
			return data;
		}

		private int[] convertCellRef(String s) {

			int[] cr = { 0, 0 };

			// column number
			int colVal = 0;
			int mul = 1;
			for (char c : new StringBuffer(s.replaceAll("\\d", "")).reverse().toString()
					.toCharArray()) {
				colVal += CHAR_NUM_VALS.get(c) * mul;
				mul *= NUMBER_OF_LETTERS;
			}
			cr[0] = colVal + 1;

			// row number
			cr[1] = Integer.parseInt(s.replaceAll("\\D", ""));

			return cr;
		}

	}
}
