package ca.tsc.special_request_tool;

import java.io.File;

public enum Commodity {

	UNKNOWN(""),
	FASHION("J:/Merchandise Planner/Show Spreads/Fashions/FASHIONS MASTER SHOW TEMPLATES"),
	HEALTH_AND_BEAUTY("J:/Merchandise Planner/H & B Web Contents & Reports"),
	JEWELLERY("J:/Merchandise Planner/Show Spreads/Jewellery/2015"),
	HOME("J:/Merchandise Planner/Show Spreads/Home and Electronics/2015/"),
	ELECTRONICS("J:/Merchandise Planner/Show Spreads/Home and Electronics/2015/");

	private final String path;

	private Commodity(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return name().substring(0, 1) + name().substring(1).toLowerCase().replaceAll("_", " ");
	}

	public String getFilePath() {
		return getFile().getPath();
	}

	public File getFile() {
		return new File(path);
	}
}