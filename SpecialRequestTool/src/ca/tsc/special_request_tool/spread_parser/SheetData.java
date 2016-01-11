package ca.tsc.special_request_tool.spread_parser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SheetData {

	private static final Pattern ITEM_NUMBER_PATTERN = Pattern.compile("^(\\d{6})$");

	private String sheetName;
	private ArrayList<CellData> cells = new ArrayList<CellData>();

	public SheetData(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void addCell(CellData cell) {
		cells.add(cell);
	}

	public ArrayList<CellData> getCells() {
		return cells;
	}

	public Object getCellDataAt(int col, int row) {
		for (CellData cell : cells)
			if (cell.col == col && cell.row == row)
				return cell.data;
		return null;
	}

	public void setCells(ArrayList<CellData> cells) {
		this.cells = cells;
	}

	public ArrayList<Integer> getItemNumbers(int itemNumberColumn) {

		ArrayList<Integer> items = new ArrayList<Integer>();
		for (CellData cell : cells)
			if (cell.col == itemNumberColumn) {

				final Matcher m = ITEM_NUMBER_PATTERN.matcher(cell.data.toString());
				if (m.find()) {
					int newItem = Integer.parseInt(m.group());
					if (!items.contains(newItem))
						items.add(newItem);
				}
			}

		return items;
	}

	public ArrayList<Integer> getItemNumbers(int itemNumberColumn, int itemNameColumn,
			String... keywords) {

		ArrayList<CellData> cellsCopy = new ArrayList<CellData>(cells);

		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int x = 0; x < cellsCopy.size(); x++) {
			CellData cell = cellsCopy.get(x);
			if (cell.col == itemNumberColumn) {
				final Matcher m = ITEM_NUMBER_PATTERN.matcher(cell.data.toString());
				if (m.find()) {
					int newItem = Integer.parseInt(m.group());
					boolean cellContains = false;

					for (int y = 0; y < cellsCopy.size(); y++) {
						CellData cell2 = cellsCopy.get(y);
						if (cell2.col == itemNameColumn && cell2.row == cell.row) {
							String data = cell2.data.toString();
							cellsCopy.remove(cell2);

							for (String keyword : keywords)
								if (data.toLowerCase().contains(keyword.toLowerCase()))
									cellContains = true;
						}
					}
					if (!cellContains && !items.contains(newItem)) {
						items.add(newItem);
						cellsCopy.remove(cell);
					}
				}
			}
		}
		return items;
	}

	// private boolean cellContains(int col, int row, ArrayList<CellData>
	// cellsCopy,
	// String... keywords) {
	//
	// return false;
	// }

	public ArrayList<Integer> getBoldedItemNumbers(int itemNumberColumn) {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (CellData cell : cells)
			if (cell.col == itemNumberColumn && cell.isBold()) {

				final Matcher m = ITEM_NUMBER_PATTERN.matcher(cell.data.toString());
				if (m.find()) {
					int newItem = Integer.parseInt(m.group());
					if (!items.contains(newItem))
						items.add(newItem);
				}
			}
		return items;

	}
}