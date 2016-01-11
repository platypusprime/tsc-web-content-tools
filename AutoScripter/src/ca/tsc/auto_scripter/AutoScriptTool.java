package ca.tsc.auto_scripter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;

import platypus.util.general.PStringUtils;
import ca.tsc.util.web.OnAirItem;
import ca.tsc.util.web.ScriptSureRequests;
import ca.tsc.util.web.Show;

/**
 * AutoScriptTool automatically pulls script information via HTTP and parses it
 * for use in daily video reports. Authentication is required on a per-run
 * basis. Output is sent via the system clipboard.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v0.0:</dt>
 * <dd>14-09-27 - Began testing HTTP requests</dd>
 * <dd>14-09-28 - Completed HTTP framework</dd>
 * <dd>14-10-03 - Implemented page data parsing</dd>
 * <dd>14-10-04 - Implemented data structure</dd>
 * <dt>v1.0:</dt>
 * <dd>14-10-04 - First stable version</dd>
 * <dd>14-10-04 - Added progress monitor</dd>
 * <dd>14-10-04 - Fixed comparator bug</dd>
 * <dd>14-10-05 - Added filter to ignore 15-sec segments</dd>
 * <dt>v1.1:</dt>
 * <dd>14-10-18 - Replaced progress monitor with full UI</dd>
 * <dd>14-11-26 - Added login interface</dd>
 * <dd>14-11-29 - Fixed login threading</dd>
 * <dd>14-12-06 - Cleaned up libraries</dd>
 * <dd>14-12-28 - Fixed unhandled socket timeout bug</dd>
 * <dd><b>15-02-22 - Added Start Time column</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since September 27, 2014
 * @version v1.1.4
 */
public class AutoScriptTool extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	// date constants
	private static final String[] MONTHS = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };
	private static final Integer[] YEARS = { 2013, 2014, 2015, 2016, 2017 };
	private static final int BUFFER = 10;

	// UI components
	private JProgressBar progressBar;
	private JLabel progressLabel;
	private JTextArea outputArea;
	private JButton copyButton, cancelButton;

	// functional objects
	// private CloseableHttpClient httpClient;
	private ArrayList<OnAirItem> items;

	public static void main(String args[]) {

		// set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unused")
		AutoScriptTool tool = new AutoScriptTool();
	}

	public AutoScriptTool() {

		// httpClient = ScriptSureRequests.getHttpClient();

		createAndShowGUI();
		final JFrame self = this;
		new Thread() {
			@Override
			public void run() {
				Object[] input = showInitialDialog(self);
				script((String) input[0], (String) input[1],
						(Calendar) input[2]);
			}
		}.start();

	}

	private void script(String username, String password, Calendar cal) {

		try {
			setTitle("AutoScript Tool - Running");

			println("Authenticating as " + username + "...");
			ScriptSureRequests.login(username, password);
			println("Authentication request sent\n");
			// TODO show whether authentication successful

			// get show urls
			println("Loading show list...");
			ArrayList<Show> shows = ScriptSureRequests.listShows(cal);
			progressBar.setMaximum(shows.size()); // set up progress monitor
			println("Found " + shows.size() + " shows\n");

			loadShows(shows); // read show details

			// wrap everything up
			println("-------------------------\nComplete");
			println("Found " + items.size() + " items in total");
			copyButton.setEnabled(true);
			cancelButton.setEnabled(false);
			setTitle("AutoScript Tool - Complete");
			ScriptSureRequests.cleanUp();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadShows(ArrayList<Show> shows) {

		items = new ArrayList<OnAirItem>();

		int num = 0;
		for (Show show : shows) {
			try {
				println(String.format("Reading show %d of %d (%s)",
						num + 1, shows.size(),
						PStringUtils.substring(show.getURI().toString(),
								"ShowID=", "&amp")));

				ArrayList<OnAirItem> newItems = ScriptSureRequests.scrapeShow(show.getURI());
				if (newItems != null) {
					println("Found " + newItems.size() + " items");
					items.addAll(newItems);
				} else {
					println("Error reading show");
				}

				println("");

				num++;
				progressBar.setValue(num);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return;
	}

	private Object[] showInitialDialog(final JFrame owner) {

		final Object[] output = new Object[3];
		final Object lock = new Object();

		// setup dialog
		final JDialog dialog = new JDialog(owner, "Login", true);
		dialog.setLayout(new GridBagLayout());
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// username field
		dialog.add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1,
				0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 2, 0, 1), 0, 0));
		final JTextField userField = new JTextField(20);
		dialog.add(userField, new GridBagConstraints(1, 0, 3, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 0, 0, 1), 0, 0));

		// password field
		dialog.add(new JLabel("Password"), new GridBagConstraints(0, 1, 1, 1,
				0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(1, 2, 0, 1), 0, 0));
		final JPasswordField passField = new JPasswordField(20);
		passField.setActionCommand("ok");
		dialog.add(passField, new GridBagConstraints(1, 1, 3, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 0, 0, 1), 0, 0));

		// date interface
		dialog.add(new JLabel("Date"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 2, 0, 1), 0, 0));
		Calendar curCal = Calendar.getInstance();
		final JComboBox monthBox = new JComboBox(MONTHS);
		monthBox.setSelectedIndex(curCal.get(Calendar.MONTH));
		monthBox.setActionCommand("date");
		dialog.add(monthBox, new GridBagConstraints(1, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 0, 0, 0), 0, 0));
		final JComboBox dateBox = new JComboBox(new DefaultComboBoxModel(
				makeDateArray(curCal.getActualMaximum(Calendar.DAY_OF_MONTH))));
		dateBox.setSelectedItem(curCal.get(Calendar.DAY_OF_MONTH));
		dialog.add(dateBox, new GridBagConstraints(2, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 0, 0, 0), 0, 0));
		final JComboBox yearBox = new JComboBox(YEARS);
		yearBox.setSelectedItem(curCal.get(Calendar.YEAR));
		monthBox.setActionCommand("date");
		dialog.add(yearBox, new GridBagConstraints(3, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 0, 0, BUFFER), 0, 0));

		// buttons bar
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		dialog.add(buttonPanel, new GridBagConstraints(0, 3, 4, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						30, 50, 5, BUFFER), 0, 0));
		JButton scriptButton = new JButton("Script");
		scriptButton.setActionCommand("ok");
		buttonPanel.add(scriptButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		buttonPanel.add(cancelButton);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		ActionListener dialogListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String cmd = e.getActionCommand();

				if (cmd.equals("ok")) {

					output[0] = userField.getText();
					output[1] = new String(passField.getPassword());
					Calendar outputCal = Calendar.getInstance();
					outputCal.set(Calendar.YEAR,
							(Integer) yearBox.getSelectedItem());
					outputCal.set(Calendar.MONTH, monthBox.getSelectedIndex());
					outputCal.set(Calendar.DATE,
							(Integer) dateBox.getSelectedItem());
					// System.out.println(outputCal.get(Calendar.MONTH));
					// System.out.println(monthBox.getSelectedIndex() + 1);
					// System.out.println((Integer) dateBox.getSelectedItem());
					output[2] = outputCal;

					// unblock thread
					synchronized (lock) {
						lock.notify();
					}
					dialog.setVisible(false);

				} else if (cmd.equals("cancel")) {
					System.exit(0);

				} else if (cmd.equals("date")) {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.MONTH, monthBox.getSelectedIndex());
					cal.set(Calendar.YEAR, (Integer) yearBox.getSelectedItem());
					Integer prevSelection = (Integer) dateBox.getSelectedItem();
					dateBox.setModel(new DefaultComboBoxModel(makeDateArray(cal
							.getActualMaximum(Calendar.DATE))));
					dateBox.setSelectedItem(prevSelection);
				}
			}
		};

		passField.addActionListener(dialogListener);
		monthBox.addActionListener(dialogListener);
		yearBox.addActionListener(dialogListener);
		scriptButton.addActionListener(dialogListener);
		cancelButton.addActionListener(dialogListener);

		// show dialog on the EDT
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				dialog.pack();
				dialog.setLocationRelativeTo(owner);
				dialog.setVisible(true);
			}
		});

		// create and start a thread that puts the lock on wait
		Thread blockThread = new Thread() {
			@Override
			public void run() {
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		blockThread.start();

		// wait for outside code to notify the lock
		try {
			blockThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return output;
	}

	private static Integer[] makeDateArray(int max) {
		Integer[] output = new Integer[max];
		for (int i = 0; i < max;)
			output[i] = ++i;
		return output;
	}

	private void createAndShowGUI() {

		setTitle("AutoScript Tool");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		add(progressBar, new GridBagConstraints(0, 0, 3, 1, .5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(2, 2, 2, 2), 0, 0));

		progressLabel = new JLabel();
		add(progressLabel, new GridBagConstraints(0, 1, 3, 1, .5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(0, 2, 2, 2), 0, 0));

		outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
		JScrollPane scrollPane = new JScrollPane(outputArea);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(300, 500));
		add(scrollPane, new GridBagConstraints(0, 2, 3, 1, .5, 0.5,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(0, 2, 2, 2), 0, 0));

		cancelButton = new JButton("CANCEL");
		cancelButton.addActionListener(this);
		add(cancelButton, new GridBagConstraints(2, 3, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(0, 2, 2, 2), 0, 0));

		copyButton = new JButton("COPY");
		copyButton.setEnabled(false);
		copyButton.setPreferredSize(cancelButton.getPreferredSize());
		copyButton.addActionListener(this);
		add(copyButton, new GridBagConstraints(1, 3, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(0, 2, 2, 0), 0, 0));

		add(new JPanel(), new GridBagConstraints(0, 3, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new InsetsUIResource(0, 2, 2, 0), 0, 0));

		validate();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void println(String ln) {

		progressLabel.setText(ln);
		outputArea.setText(outputArea.getText() + ln + "\n");
//		outputArea.scrollRectToVisible(new Rectangle(0, (int) outputArea
//				.getPreferredSize().getHeight(), 10, 10));
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == copyButton) {
			Collections.sort(items);

			// convert data to string
			String output = "";
			for (OnAirItem item : items)
				output = output.concat(item.getLn() + "\n");

			// copy output to clipboard
			StringSelection stringSelection = new StringSelection(output);
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(stringSelection, null);
			JOptionPane.showMessageDialog(this, "Output copied to clipboard!",
					"AutoScriptTool", JOptionPane.INFORMATION_MESSAGE);
		} else {
			System.exit(0);
		}
	}
}
