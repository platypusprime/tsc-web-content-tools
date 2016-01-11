/* ====================================================================
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==================================================================== */
package ca.tsc.special_request_tool.testing;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XSSF and SAX (Event API)
 */
public class FromHowTo {
	public static void main(String[] args) throws Exception {
		FromHowTo howto = new FromHowTo();
		InfoHolder holder = new InfoHolder();
		howto.processOneSheet("sr_test_spreads/MARALLIS - TAURUS NEW2.xlsm", holder);
		for (int i : holder.data) {
			System.out.println(i);
		}
		// howto.processAllSheets("sr_test_spreads/MARALLIS - TAURUS NEW2.xlsm");
	}

	public void processOneSheet(String filename, InfoHolder holder) throws Exception {
		OPCPackage pkg = OPCPackage.open(new File(filename));
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst, holder);

		// rId2 found by processing the Workbook
		// Seems to either be rId# or rSheet#
		InputStream sheet2 = r.getSheet("rId2");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();
	}

	// public void processAllSheets(String filename) throws Exception {
	// OPCPackage pkg = OPCPackage.open(filename);
	// XSSFReader r = new XSSFReader(pkg);
	// SharedStringsTable sst = r.getSharedStringsTable();
	//
	// XMLReader parser = fetchSheetParser(sst);
	//
	// Iterator<InputStream> sheets = r.getSheetsData();
	// while (sheets.hasNext()) {
	// System.out.println("Processing new sheet:\n");
	// InputStream sheet = sheets.next();
	// InputSource sheetSource = new InputSource(sheet);
	// parser.parse(sheetSource);
	// sheet.close();
	// System.out.println("");
	// }
	// }

	public XMLReader fetchSheetParser(SharedStringsTable sst, InfoHolder holder)
			throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new SheetHandler(sst, holder);
		parser.setContentHandler(handler);
		return parser;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private static class SheetHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		private boolean rightCol;
		private InfoHolder holder;

		private SheetHandler(SharedStringsTable sst, InfoHolder holder) {
			this.sst = sst;
			this.holder = holder;
		}

		public void startElement(String uri, String localName, String name, Attributes attributes)
				throws SAXException {
			// c => cell
			if (name.equals("c")) {
				// Print the cell reference
				// System.out.print(attributes.getValue("r") + " - ");
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				String cellRef = attributes.getValue("r");
				rightCol = cellRef.matches("^A\\d+");
				if (cellType != null && cellType.equals("s")) {
					// System.out.println(cellRef.matches("^A\\d+"));
					nextIsString = true;
				} else {
					// System.out.println(cellRef.matches("^A\\d+"));
					nextIsString = false;
				}
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

			// v => contents of a cell
			// Output after we've seen the string contents
			if (name.equals("v")) {
				final Matcher m = PoiTest.p.matcher(lastContents);
				if (m.find() && rightCol) {
					int newNum = Integer.parseInt(m.group(0));
					// System.out.println(newNum);
					holder.add(newNum);
					// System.out.println(lastContents);
				}
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			lastContents += new String(ch, start, length);
		}
	}

	private static class InfoHolder {
		ArrayList<Integer> data = new ArrayList<Integer>();

		public InfoHolder() {

		}

		public void add(int i) {
			data.add(i);
		}
	}
}