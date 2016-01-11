package ca.tsc.special_request_tool.pop_up;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.tsc.special_request_tool.main_ui.DataReceiver;

public class WebfactoryLoader extends JFrame implements ActionListener, DocumentListener {

	private static final long serialVersionUID = -2566549735940768883L;

	public static final Dimension PANEL_SIZE = new Dimension(300, 400);

	private JTextArea input;
	private JList preview;

	private DataReceiver receiver;
	private ArrayList<Integer> itemNums;

	public WebfactoryLoader(DataReceiver receiver) {
		super("Load item numbers from WebFactory");

		this.receiver = receiver;
		itemNums = new ArrayList<Integer>();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		createAndShowGUI();
	}

	private void createAndShowGUI() {

		setLayout(new GridBagLayout());

		JPanel tp = new JPanel();
		tp.setLayout(new GridLayout(0, 2));
		((GridLayout) tp.getLayout()).setHgap(10);

		// input area
		input = new JTextArea();
		input.setFont(new Font("Monospaced", Font.PLAIN, 9));
		input.getDocument().addDocumentListener(this);
		JScrollPane inputSP = new JScrollPane(input);
		inputSP.setBorder(BorderFactory.createTitledBorder("Input"));
		inputSP.setPreferredSize(PANEL_SIZE);
		tp.add(inputSP);

		// preview list
		preview = new JList();
		preview.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		preview.setVisibleRowCount(-1);
		JScrollPane previewSP = new JScrollPane(preview);
		previewSP.setBorder(BorderFactory.createTitledBorder("Preview"));
		previewSP.setPreferredSize(PANEL_SIZE);
		tp.add(previewSP);

		add(tp, new GridBagConstraints(0, 0, 2, 1, .5, .5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 0, 10), 0, 0));

		// buttons
		JPanel bp = new JPanel();
		bp.setLayout(new GridLayout(0, 2));

		JButton b1 = new JButton("OK");
		b1.setActionCommand("OK");
		b1.addActionListener(this);
		bp.add(b1);
		JButton b2 = new JButton("Cancel");
		b2.setActionCommand("Cancel");
		b2.addActionListener(this);
		bp.add(b2);

		add(new JPanel(), new GridBagConstraints(0, 1, 1, 1, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
		add(bp, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void updateList() {

		// extract item numbers
		itemNums.clear();
		String[] webfactoryDigits = input.getText().split("[\\D]");
		for (String s : webfactoryDigits)
			if (s.length() == 6) {
				Integer i = Integer.parseInt(s);
				if (!itemNums.contains(i))
					itemNums.add(i);
			}
		Collections.sort(itemNums);

		// update preview
		DefaultListModel model = new DefaultListModel();
		for (Integer i : itemNums)
			model.addElement(i);
		preview.setModel(model);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();

		if (command.equals("OK"))
			receiver.receiveWFData(itemNums);
		setVisible(false);
		dispose();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateList();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateList();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateList();
	}

}
