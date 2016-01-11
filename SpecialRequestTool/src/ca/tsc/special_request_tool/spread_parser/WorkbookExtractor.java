package ca.tsc.special_request_tool.spread_parser;

import java.util.ArrayList;

public interface WorkbookExtractor {

	public ArrayList<String> extractSheetNames();

	public ArrayList<SheetData> extractData();
}
