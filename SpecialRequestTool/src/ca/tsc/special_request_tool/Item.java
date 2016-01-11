package ca.tsc.special_request_tool;

public class Item {

	private final int itemNumber;

	private boolean flag = false, forcedFlag = false;
	private boolean flagOverride = false;
	private SRShow show;
	private String notes = "";

	public Item(int itemNumber, SRShow show) {

		this.itemNumber = itemNumber;
		setShow(show);
	}

	public boolean getFlag() {
		return flagOverride ? forcedFlag : flag;
	}

	public boolean flagOverriden() {
		return flagOverride;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
		flagOverride = false;
	}

	public void overrideFlag(boolean flag) {
		this.forcedFlag = flag;
		flagOverride = true;
	}

	public Commodity getCommodity() {
		return show.getCommodity();
	}

	public int getItemNumber() {
		return itemNumber;
	}

	public String getShowName() {
		return show.getName();
	}

	public String getFormattedDueDate() {
		return show.getFormattedDueDate();
	}

	public String getFormattedAirDate() {
		return show.getFormattedAirDate();
	}

	public void setShow(SRShow show) {
		this.show = show;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Item)
			return itemNumber == ((Item) o).getItemNumber();
		return false;
	}
}
