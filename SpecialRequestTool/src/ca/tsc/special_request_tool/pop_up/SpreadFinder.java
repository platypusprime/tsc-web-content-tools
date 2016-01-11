package ca.tsc.special_request_tool.pop_up;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import ca.tsc.special_request_tool.main_ui.DataReceiver;

// TODO spread finder interface
public class SpreadFinder extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private DataReceiver receiver;

	public SpreadFinder(DataReceiver receiver) {
		super("Load spreads");
		this.receiver = receiver;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
