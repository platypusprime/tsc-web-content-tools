package ca.tsc.auto_scripter.deprecated;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import platypus.util.monitoring.ClipboardMonitor;

/**
 * A GUI for parsing scripts from ScriptSure. Automatically detects clipboard
 * changes and parses the contents. Outputs a tab-separated list of item
 * information to the system clipboard.
 * 
 * @deprecated Use <code>AutoScriptTool</code> instead.
 * @author Jingchen Xu
 * @since August 6, 2014
 * @version 1.2.4
 */
public class ScriptSureTool extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private ClipboardMonitor cliplistener;

	private JTextArea outputBox = new JTextArea();
	private JButton startStopButton;

	private Vector<String> prevOutputs = new Vector<String>();

	public static void main(String[] args) {

		// set look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// instantiate GUI
		ScriptSureTool i = new ScriptSureTool();
		i.setVisible(true);

	}

	public ScriptSureTool() {

		super("ScriptSure Tool v1.2.2");
		createGUI();

		// set up clipboard listener
		cliplistener = new ClipboardMonitor();
		cliplistener.addActionListener(this);
		cliplistener.start();
	}

	private void createGUI() {

		// create and show GUI
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(new Dimension(400, 500));
		setLayout(new GridBagLayout());

		/*
		 * button bar
		 */
		int buttonPos = 0;
		ButtonCommand[] allcmds = ButtonCommand.values();
		for (int i = 0; i < allcmds.length; i++) {
			ButtonCommand command = allcmds[i];
			// special case for start/stop button
			if (command == ButtonCommand.START) {
				startStopButton = new JButton(command.text());
				startStopButton.setToolTipText(command.tooltip());
				startStopButton.setActionCommand(command.name());
				startStopButton.addActionListener(this);
				add(startStopButton, new GridBagConstraints(buttonPos, 0, 1, 1, 0.5, 0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
						0, 0));
			}

			if (command != ButtonCommand.STOP) {
				JButton b = new JButton(command.text());
				b.setToolTipText(command.tooltip());
				b.setActionCommand(command.name());
				b.addActionListener(this);
				add(b, new GridBagConstraints(buttonPos, 0, 1, 1, 0.5, 0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
						0, 0));
				buttonPos++;
			}
		}

		/*
		 * output preview box
		 */
		outputBox.setEditable(false);
		JScrollPane outScroll = new JScrollPane(outputBox);
		outScroll.setBorder(BorderFactory.createTitledBorder("Ouput Preview"));
		outScroll.setPreferredSize(new Dimension(0, 400));
		add(outScroll, new GridBagConstraints(0, 1, 4, 1, 0, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// finish up
		validate();
		pack();
	}

	public static String parseScript(String input) throws IOException {

		// parse digits
		String[] digits = input.split("[\\D]");
		Vector<String> itemNums = new Vector<String>();
		Vector<String> durations = new Vector<String>();

		// find unique item numbers
		for (int i = 0; i < digits.length; i++)
			if (digits[i].length() == 6 && !itemNums.contains(digits[i])) {
				itemNums.add(digits[i]);
				durations.add("00.00");
			}

		Collections.sort(itemNums);

		// parse show hour + durations
		String hour = "--";

		BufferedReader reader = new BufferedReader(new StringReader(input));
		String ln;
		while ((ln = reader.readLine()) != null) {
			if (ln.contains("Item #")) {
				reader.readLine(); // skip a line
				String firstItemInfo = reader.readLine(); // juicy stuff
				String[] tempArray1 = firstItemInfo.split(":");
				String[] tempArray2 = tempArray1[tempArray1.length - 2].split(" ");
				hour = tempArray2[tempArray2.length - 1].trim();
				break;
			}
		}

		while ((ln = reader.readLine()) != null) {
			for (int i = 0; i < itemNums.size(); i++) {
				if (ln.contains(itemNums.get(i))) {
					String[] dotsplit = ln.split("\\.");
					String min = dotsplit[dotsplit.length - 2];
					min = min.substring(min.length() - 2);
					String sec = dotsplit[dotsplit.length - 1].substring(0, 2);

					if (Integer.parseInt(min + sec) > Integer.parseInt(durations.get(i).replaceAll(
							"\\.", "")))
						durations.set(i, min + "." + sec);
					break;
				}
			}
		}

		// generate output
		String output = "";
		for (int i = 0; i < itemNums.size(); i++)
			output = output.concat(itemNums.get(i) + "\t" + hour + ":00" + "\t" + durations.get(i)
					+ "\n");

		return output;
	}

	public void actionPerformed(ActionEvent evt) {

		String cmd = evt.getActionCommand();

		// new string in clipboard
		if (evt.getSource() == cliplistener) {
			prevOutputs.add(outputBox.getText());
			String newScript = "";
			try {
				newScript = ScriptSureTool.parseScript(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
			outputBox.setText(outputBox.getText().concat(newScript));

			// scroll to bottom
			outputBox.scrollRectToVisible(new Rectangle(0, (int) outputBox.getPreferredSize()
					.getHeight(), 10, 10));
		}

		// start button
		else if (cmd.equals(ButtonCommand.START.name())) {

			cliplistener.resumeListening();
			startStopButton.setText(ButtonCommand.STOP.text());
			startStopButton.setToolTipText(ButtonCommand.STOP.tooltip());
			startStopButton.setActionCommand(ButtonCommand.STOP.name());
		}

		// stop button
		else if (cmd.equals(ButtonCommand.STOP.name())) {

			cliplistener.pauseListening();
			startStopButton.setText(ButtonCommand.START.text());
			startStopButton.setToolTipText(ButtonCommand.START.tooltip());
			startStopButton.setActionCommand(ButtonCommand.START.name());
		}

		// copy button
		else if (cmd.equals(ButtonCommand.COPY.name())) {

			cliplistener.pauseListening();
			startStopButton.setText(ButtonCommand.START.text());
			startStopButton.setActionCommand(ButtonCommand.START.name());

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			StringSelection stringSelection = new StringSelection(outputBox.getText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
		}

		// clear last button
		else if (cmd.equals(ButtonCommand.UNDO.name()) && prevOutputs.size() > 0) {
			outputBox.setText(prevOutputs.get(prevOutputs.size() - 1));
			prevOutputs.remove(prevOutputs.size() - 1);
		}

		// clear all button
		else if (cmd.equals(ButtonCommand.CLEAR_ALL.name())) {
			prevOutputs.add(outputBox.getText());
			outputBox.setText("");
		}
	}

	enum ButtonCommand {

		START("START", "Start listening to clipboard changes"),
		STOP("STOP", "Stop listening to clipboard changes"),
		COPY("COPY", "Stop listening and export output to clipboard"),
		UNDO("UNDO", "Undo last change"),
		CLEAR_ALL("CLEAR ALL", "Remove all scripts from output");

		private String text;
		private String tooltip;

		private ButtonCommand(String text, String tooltip) {
			this.text = text;
			this.tooltip = tooltip;
		}

		protected String text() {
			return text;
		}

		protected String tooltip() {
			return tooltip;
		}
	}

}
