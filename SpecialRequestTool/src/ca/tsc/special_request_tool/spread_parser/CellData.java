package ca.tsc.special_request_tool.spread_parser;

public class CellData {

	private static final short PLAIN_WEIGHT = 400;

	final int col, row;
	final Object data;
	final short boldWeight;

	public CellData(int col, int row, int data, short boldWeight) {
		this.col = col;
		this.row = row;
		this.data = data;
		this.boldWeight = boldWeight;
	}

	public CellData(int col, int row, String data, short boldWeight) {
		this.col = col;
		this.row = row;
		this.data = data;
		this.boldWeight = boldWeight;
	}

	public boolean isBold() {
		return boldWeight > PLAIN_WEIGHT;
	}
}