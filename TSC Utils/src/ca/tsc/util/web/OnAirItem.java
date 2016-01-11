package ca.tsc.util.web;

public class OnAirItem implements Comparable<OnAirItem> {

	private String number, hour, startTime, duration;

	public OnAirItem() {
		this.number = null;
		this.hour = null;
		this.startTime = null;
		this.duration = null;
	}

	public String getHour() {
		return hour;
	}

	public String getLn() {
		return number + "\t" + hour + "\t" + startTime + "\t" + duration;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public void setTime(String time) {
		this.startTime = time;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public boolean isComplete() {
		return number != null && hour != null && startTime != null
				&& duration != null && !duration.equals("00.15");
	}

	public boolean replaces(OnAirItem item) {
		return number.equals(item.number) && hour.equals(item.hour)
				&& duration.compareTo(item.duration) > 0;
	}

	@Override
	public boolean equals(Object item) {

		if (item instanceof OnAirItem && isComplete()
				&& ((OnAirItem) item).isComplete())
			return number.equals(((OnAirItem) item).number)
					&& hour.equals(((OnAirItem) item).hour)
					&& startTime.equals(((OnAirItem) item).startTime)
					&& duration.equals(((OnAirItem) item).duration);

		return false;
	}

	@Override
	public int compareTo(OnAirItem item) {

		int compare = number.compareTo(item.number);

		if (compare != 0)
			return compare;

		String hour = this.hour, compareHour = item.hour;
		if (hour.compareTo("07:00") <= 0)
			hour = "9" + hour;
		if (compareHour.compareTo("07:00") <= 0)
			compareHour = "9" + compareHour;
		compare = hour.compareTo(compareHour);
		if (compare != 0)
			return compare;

		compare = duration.compareTo(item.duration);
		return compare;
	}

}
