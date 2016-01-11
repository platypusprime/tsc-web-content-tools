package ca.tsc.special_request_tool.main_ui;

import java.util.ArrayList;

import ca.tsc.special_request_tool.SRShow;

public interface DataReceiver {
	void receiveShowData(ArrayList<SRShow> showData);

	void receiveWFData(ArrayList<Integer> wfData);
}

enum DataType {
	SHOW_DATA,
	WF_DATA;
}
