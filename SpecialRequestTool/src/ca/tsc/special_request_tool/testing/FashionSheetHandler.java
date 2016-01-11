package ca.tsc.special_request_tool.testing;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FashionSheetHandler extends DefaultHandler {

	private SharedStringsTable sst;
	private String lastContents;
	private boolean nextIsString;
	private boolean rightCol;
	private ArrayList<Integer> data;

	public FashionSheetHandler(SharedStringsTable sst, ArrayList<Integer> holder) {
		this.sst = sst;
		this.data = holder;
	}

	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException {
		// c => cell
		if (name.equals("c")) {
			// Figure out if the value is an index in the SST
			String cellType = attributes.getValue("t");
			String cellRef = attributes.getValue("r");
			rightCol = cellRef.matches("^A\\d+");
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

		// v => contents of a cell
		// Output after we've seen the string contents
		if (name.equals("v")) {
			final Matcher m = PoiTest.p.matcher(lastContents);
			if (m.find() && rightCol) {
				int newNum = Integer.parseInt(m.group(0));
				if (!data.contains(newNum))
					data.add(newNum);
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		lastContents += new String(ch, start, length);
	}

}
