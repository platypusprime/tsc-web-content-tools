package ca.tsc.special_request_tool.pop_up;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ca.tsc.special_request_tool.SRShow;
import ca.tsc.special_request_tool.SpecialRequestTool;
import ca.tsc.special_request_tool.main_ui.DataReceiver;

public class ShowLoader extends JFrame implements ActionListener {

	private static final long serialVersionUID = 8540379631503575267L;

	// constants
	private static final String[] MONTHS = { "January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November", "December" };
	private static final Integer[] YEARS = { 2013, 2014, 2015, 2016, 2017 };
	private static final int BUFFER = 10;
	private static final int DEFAULT_DATE_OFFSET = 4;
	private static final int DEFAULT_SHOW_PERIOD = 4;

	// components
	private JTextField userField;
	private JPasswordField passField;
	private JComboBox startMonth, startDate, startYear, endMonth, endDate, endYear;

	// interface
	private DataReceiver receiver;

	public ShowLoader(DataReceiver receiver) {
		super("Load shows from ScriptSure");
		this.receiver = receiver;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		createAndShowGUI();
	}

	private void createAndShowGUI() {

		setLayout(new GridBagLayout());

		// username
		add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(BUFFER, BUFFER, 0, 1), 0, 0));
		userField = new JTextField(20);
		add(userField, new GridBagConstraints(1, 0, 3, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(BUFFER, 0, 0, BUFFER), 0, 0));

		// password
		add(new JLabel("Password"), new GridBagConstraints(0, 1, 4, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, BUFFER, 0, 1), 0,
				0));
		passField = new JPasswordField(20);
		add(passField, new GridBagConstraints(1, 1, 3, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1, 0, 0, BUFFER), 0, 0));

		// start date
		add(new JLabel("Start date:"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(BUFFER, BUFFER, 0, 1), 0, 0));

		Calendar curCal = Calendar.getInstance();
		curCal.add(Calendar.DATE, DEFAULT_DATE_OFFSET);
		startMonth = new JComboBox(MONTHS);
		startMonth.setSelectedIndex(curCal.get(Calendar.MONTH));
		startMonth.addActionListener(this);
		add(startMonth, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(BUFFER, 0, 0, 0), 0, 0));

		startDate = new JComboBox(new DefaultComboBoxModel(
				makeDateArray(curCal.getActualMaximum(Calendar.DAY_OF_MONTH))));
		startDate.setSelectedItem(curCal.get(Calendar.DAY_OF_MONTH));
		add(startDate, new GridBagConstraints(2, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(BUFFER, 0, 0, 0), 0, 0));

		startYear = new JComboBox(YEARS);
		startYear.setSelectedItem(curCal.get(Calendar.YEAR));
		startYear.addActionListener(this);
		add(startYear, new GridBagConstraints(3, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(BUFFER, 0, 0, BUFFER), 0, 0));

		// end date
		add(new JLabel("End date:"), new GridBagConstraints(0, 3, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, BUFFER, 0, 1), 0,
				0));

		curCal.add(Calendar.DATE, DEFAULT_SHOW_PERIOD);
		endMonth = new JComboBox(MONTHS);
		endMonth.setSelectedIndex(curCal.get(Calendar.MONTH));
		endMonth.addActionListener(this);
		add(endMonth, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1, 0, 0, 0), 0, 0));

		endDate = new JComboBox(new DefaultComboBoxModel(
				makeDateArray(curCal.getActualMaximum(Calendar.DAY_OF_MONTH))));
		endDate.setSelectedItem(curCal.get(Calendar.DAY_OF_MONTH));
		add(endDate, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1, 0, 0, 0), 0, 0));

		endYear = new JComboBox(YEARS);
		endYear.setSelectedItem(curCal.get(Calendar.YEAR));
		endYear.addActionListener(this);
		add(endYear, new GridBagConstraints(3, 3, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1, 0, 0, BUFFER), 0, 0));

		// buttons
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new GridLayout(0, 2));
		add(bPanel, new GridBagConstraints(0, 4, 4, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(30, 50, 5, BUFFER), 0, 0));

		JButton b1 = new JButton("OK");
		b1.setActionCommand("OK");
		b1.addActionListener(this);
		bPanel.add(b1);

		JButton b2 = new JButton("Cancel");
		b2.setActionCommand("Cancel");
		b2.addActionListener(this);
		bPanel.add(b2);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private static Integer[] makeDateArray(int max) {
		Integer[] output = new Integer[max];
		for (int i = 0; i < max;)
			output[i] = ++i;
		return output;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();

		if (src == startMonth || src == startYear) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, startMonth.getSelectedIndex());
			cal.set(Calendar.YEAR, (Integer) startYear.getSelectedItem());
			Integer prevSelection = (Integer) startDate.getSelectedItem();
			startDate.setModel(new DefaultComboBoxModel(makeDateArray(cal
					.getActualMaximum(Calendar.DATE))));
			startDate.setSelectedItem(prevSelection);
		}

		else if (src == endMonth || src == endYear) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, endMonth.getSelectedIndex());
			cal.set(Calendar.YEAR, (Integer) endYear.getSelectedItem());
			Integer prevSelection = (Integer) endDate.getSelectedItem();
			endDate.setModel(new DefaultComboBoxModel(makeDateArray(cal
					.getActualMaximum(Calendar.DATE))));
			endDate.setSelectedItem(prevSelection);
		}

		else if (evt.getActionCommand().equals("OK")) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					ArrayList<SRShow> shows = null;

					// get start date
					Calendar startCal = Calendar.getInstance();
					startCal.set(Calendar.MONTH, startMonth.getSelectedIndex());
					startCal.set(Calendar.DATE, (Integer) startDate.getSelectedItem());
					startCal.set(Calendar.YEAR, (Integer) startYear.getSelectedItem());

					// get end date
					Calendar endCal = Calendar.getInstance();
					endCal.set(Calendar.MONTH, endMonth.getSelectedIndex());
					endCal.set(Calendar.DATE, (Integer) endDate.getSelectedItem());
					endCal.set(Calendar.YEAR, (Integer) endYear.getSelectedItem());

					// get shows from ScriptSure
					shows = SpecialRequestTool.getShows(userField.getText(),
							new String(passField.getPassword()), startCal, endCal);

					System.out.println("DONE");
					receiver.receiveShowData(shows);
				}
			}).start();

			setVisible(false);
			dispose();

		} else if (evt.getActionCommand().equals("Cancel")) {
			setVisible(false);
			dispose();
		}
	}
}
