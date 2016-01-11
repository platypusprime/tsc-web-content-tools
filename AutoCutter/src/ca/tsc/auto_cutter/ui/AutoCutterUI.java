package ca.tsc.auto_cutter.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

import platypus.components.modal.PSnipper;
import ca.tsc.auto_cutter.AutoCutter;
import ca.tsc.auto_cutter.AutoCutterScreenCoords;
import ca.tsc.auto_cutter.Segment;

public class AutoCutterUI implements ActionListener {

	private static final String FRAME_TITLE = "AutoCutter";
	private static final String[][] FILE_MENU_STRINGS = { { "Exit", "EXIT" } };
	private static final String[][] EDIT_MENU_STRINGS = {
			{ "Set item number area", "ITEM_NUM_RECT" },
			{ "Set fullscreen timecode field location", "FULL_TIMECODE_PT" },
			{ "Set restored timecode field location", "SMALL_TIMECODE_PT" },
			{ "Set source panel location", "SRC_PT" },
			{ "Set sequence panel location", "SEQ_PT" },
			{ "Reset locations to default", "RESET" } };

	private static final String DATA_BUTTON_TEXT = "Input spreadsheet data";
	private static final String SCAN_BUTTON_TEXT = "Scan and cut";
	private static final String CUT_BUTTON_TEXT = "Cut only";
	private static final String[] INPUT_COLUMN_NAMES = { "Video", "Rename",
			"Status", "Time", "Item", "Name", "Brand", "Show" };
	private static final String[] RESULTS_COLUMN_NAMES = { "Item",
			"Start Time", "End Time", "Note" };

	private JFrame frame;
	private JDialog dataDialog;
	private JButton dataButton, scanButton, cutButton;
	private JTable inputTable;

	public JTable resultsTable;
	public JProgressBar progressBar;

	private final AutoCutterScreenCoords coords;

	ArrayList<Segment> segments;

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		// set look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new AutoCutterUI();
	}

	public AutoCutterUI() {
		coords = new AutoCutterScreenCoords();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createAndShowGUI();
				createAndAddMenuBar();
			}

		});
	}

	private void createAndShowGUI() {

		// set up window
		frame = new JFrame(FRAME_TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridBagLayout());

		// create and add progress bar
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		frame.add(progressBar, new GridBagConstraints(0, 0, 3, 1, 0, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// create and add buttons
		dataButton = new JButton(DATA_BUTTON_TEXT);
		dataButton.addActionListener(this);
		frame.add(dataButton, new GridBagConstraints(0, 1, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		scanButton = new JButton(SCAN_BUTTON_TEXT);
		scanButton.addActionListener(this);
		frame.add(scanButton, new GridBagConstraints(1, 1, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		cutButton = new JButton(CUT_BUTTON_TEXT);
		cutButton.addActionListener(this);
		frame.add(cutButton, new GridBagConstraints(2, 1, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// create and add data tables
		inputTable = new JTable(new DefaultTableModel(INPUT_COLUMN_NAMES, 0) {
			private static final long serialVersionUID = -5744291796191569355L;

			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		});
		JScrollPane inputSP = new JScrollPane(inputTable);
		frame.add(inputSP, new GridBagConstraints(0, 2, 3, 1, 0, 0.5,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		resultsTable = new JTable(
				new DefaultTableModel(RESULTS_COLUMN_NAMES, 0) {
					private static final long serialVersionUID = -4160280122533617069L;

					@Override
					public boolean isCellEditable(int row, int col) {
						return false;
					}
				});
		JScrollPane resultsSP = new JScrollPane(resultsTable);
		frame.add(resultsSP, new GridBagConstraints(0, 3, 3, 1, 0, 0.5,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// set up data input dialog
		dataDialog = new JDialog(frame, true);
		dataDialog.setTitle("Paste cells from spreadsheet here");
		dataDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dataDialog.setLayout(new GridBagLayout());
		final JTextArea ta = new JTextArea(10, 50);
		dataDialog.add(new JScrollPane(ta), new GridBagConstraints(0, 0, 3, 1,
				0, 0.5,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		dataDialog.add(new JPanel(), new GridBagConstraints(0, 1, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		JButton b1 = new JButton("Confirm");
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					parseSpreadData(ta.getText());
				} catch (IOException e) {
					e.printStackTrace();
				}
				dataDialog.setVisible(false);
				ta.setText("");
			}
		});
		dataDialog.add(b1, new GridBagConstraints(1, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		JButton b2 = new JButton("Cancel");
		b2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataDialog.setVisible(false);
				ta.setText("");
			}
		});
		dataDialog.add(b2, new GridBagConstraints(2, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		dataDialog.pack();
		dataDialog.setLocationRelativeTo(frame);

		// wrap it up
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	private void createAndAddMenuBar() {

		JMenuBar menuBar = new JMenuBar();

		// create and add file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		// add file menu items
		for (int i = 0; i < FILE_MENU_STRINGS.length; i++) {
			JMenuItem menuItem = new JMenuItem(FILE_MENU_STRINGS[i][0]);
			menuItem.addActionListener(this);
			menuItem.setActionCommand(FILE_MENU_STRINGS[i][1]);
			fileMenu.add(menuItem);
		}

		// create and add edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);

		// add edit menu items
		for (int i = 0; i < EDIT_MENU_STRINGS.length; i++) {
			JMenuItem menuItem = new JMenuItem(EDIT_MENU_STRINGS[i][0]);
			menuItem.addActionListener(this);
			menuItem.setActionCommand(EDIT_MENU_STRINGS[i][1]);
			editMenu.add(menuItem);
			if (i == EDIT_MENU_STRINGS.length - 2)
				editMenu.addSeparator();
		}

		// set menu bar
		frame.setJMenuBar(menuBar);
	}

	private void parseSpreadData(String text) throws IOException {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(text));

			DefaultTableModel model = ((DefaultTableModel) inputTable.getModel());

			// reset UI
			((DefaultTableModel) inputTable.getModel()).setRowCount(0);
			((DefaultTableModel) resultsTable.getModel()).setRowCount(0);
			progressBar.setValue(0);

			// read until blank
			String ln;
			while ((ln = reader.readLine()) != null && !ln.isEmpty()) {
				String[] lnsplit = ln.split("\t");
				String[] newData = new String[8];
				if (lnsplit[4].length() == 6) {
					System.arraycopy(lnsplit, 0, newData, 0,
							Math.min(8, lnsplit.length));
				} else if (lnsplit[5].length() == 6) {
					System.arraycopy(lnsplit, 0, newData, 0, 3);
					System.arraycopy(lnsplit, 4, newData, 3,
							Math.min(5, lnsplit.length - 4));
				} else {
					continue;
				}
				model.addRow(newData);
			}

		} finally {
			// close reader
			if (reader != null)
				reader.close();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JButton) {
			JButton src = (JButton) e.getSource();

			if (src == dataButton) {
				dataDialog.pack();
				dataDialog.setLocationRelativeTo(frame);
				dataDialog.setVisible(true);

			} else if (src == scanButton) {
				String output = JOptionPane.showInputDialog(frame,
						"Please enter the last frame",
						"Scan and cut", JOptionPane.QUESTION_MESSAGE);
				final int lastFrame;
				if (output != null)
					try {
						lastFrame = Integer.parseInt(output);
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(frame,
								"Please enter an integer", "Error!",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				else
					return;

				try {
					// delay start a bit
					Thread.sleep(3000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}

				new Thread(new Runnable() {

					@Override
					public void run() {
						segments = AutoCutter.scan(
								lastFrame,
								((DefaultTableModel) inputTable.getModel()).getDataVector(),
								AutoCutterUI.this, coords);
						AutoCutter.cut(segments, AutoCutterUI.this, coords);
					}

				}).start();

			} else if (src == cutButton) {
				AutoCutter.cut(segments, this, coords);

			} else {
				System.err.println("Unexpected action event");

			}
		} else {
			String cmd = e.getActionCommand();

			if (cmd.equals(FILE_MENU_STRINGS[0][1])) {
				System.exit(0);
			} else if (cmd.equals(EDIT_MENU_STRINGS[0][1])) {
				new Thread() {
					@Override
					public void run() {
						coords.setItemNumberRect(PSnipper.snip());
					}
				}.start();
			} else if (cmd.equals(EDIT_MENU_STRINGS[1][1])) {
				new Thread() {
					@Override
					public void run() {
						coords.setFullTimeSeekPoint(PSnipper.snip());
					}
				}.start();
			} else if (cmd.equals(EDIT_MENU_STRINGS[2][1])) {
				new Thread() {
					@Override
					public void run() {
						coords.setSmallTimeSeekPoint(PSnipper.snip());
					}
				}.start();
			} else if (cmd.equals(EDIT_MENU_STRINGS[3][1])) {
				new Thread() {
					@Override
					public void run() {
						coords.setSourcePoint(PSnipper.snip());
					}
				}.start();
			} else if (cmd.equals(EDIT_MENU_STRINGS[4][1])) {
				new Thread() {
					@Override
					public void run() {
						coords.setSequencePoint(PSnipper.snip());
					}
				}.start();
			} else if (cmd.equals(EDIT_MENU_STRINGS[5][1])) {
				coords.reset();
			}
		}
	}
}
