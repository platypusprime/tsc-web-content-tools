package ca.tsc.util.web;

import java.net.URI;
import java.util.Calendar;

public class Show {
	private String name;
	private Calendar dueDate, airDate;
	private URI uri;

	public Show(String name, String scriptNum, URI uri) {
		setName(name);
		setDates(scriptNum);
		setURI(uri);
	}

	public Show(String name, Calendar airDate, URI uri) {
		setName(name);
		setDates(airDate);
		setURI(uri);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getAirDate() {
		return airDate;

	}

	public Calendar getDueDate() {
		return dueDate;
	}

	public void setDates(Calendar airDate) {

		this.airDate = airDate;
		setDueDateFromAirDate();
	}

	public void setDueDate(Calendar dueDate) {
		this.dueDate = dueDate;
	}

	public void setDates(String scriptNum) {

		airDate = Calendar.getInstance();
		airDate.set(Calendar.DATE, Integer.parseInt(scriptNum.substring(0, 2)));
		airDate.set(Calendar.MONTH,
				Integer.parseInt(scriptNum.substring(2, 4)) - 1);

		setDueDateFromAirDate();
	}

	private void setDueDateFromAirDate() {

		// start at air date
		dueDate = Calendar.getInstance();
		dueDate.setTime(airDate.getTime());

		// try going back two days
		dueDate.add(Calendar.DAY_OF_YEAR, -2);

		// keep rolling until a weekday is found
		while (dueDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| dueDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			dueDate.add(Calendar.DAY_OF_YEAR, -1);
		}

	}

	public URI getURI() {
		return uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof Show)
			return this.uri.equals(((Show) o).getURI());
		return false;

	}

}
