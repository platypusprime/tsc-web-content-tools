package ca.tsc.special_request_tool.deprecated;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import ca.tsc.special_request_tool.Item;
import ca.tsc.util.web.QALinkOpener;

/**
 * A comprehensive suite of tools for generating Special Requests updates.
 * Support has been discontinued.
 * 
 * @author Jingchen Xu
 * @since November 4, 2014
 * @version 1.1.7
 */
public class SRReportGenerator extends JFrame implements ActionListener, MouseListener {

	// default serial version ID
	private static final long serialVersionUID = 1L;

	// constants
	protected static final String[] PRODUCT_TYPES = { "Home", "Electronics", "Fashion",
			"Jewellery", "Beauty", "Health" };

	// components
	private JTextArea webfactoryInputArea, vendorInputArea, emailArea;
	private JComboBox productTypeBox = new JComboBox(PRODUCT_TYPES);
	private JTextField showNameField, dateField;
	private JTable table;
	private JPopupMenu tableMenu = new JPopupMenu();

	// table painter helpers
	private Object blankObject = new Object();

	// data fields
	private Map<Integer, Object> webfactoryItems = new HashMap<Integer, Object>();
	private Vector<Integer> vendorItems = new Vector<Integer>();
	private Map<Integer, Item> problemItems = new HashMap<Integer, Item>();

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

		SRReportGenerator rg = new SRReportGenerator();
		rg.setLocationRelativeTo(null);
		rg.setVisible(true);
	}

	public SRReportGenerator() {
		super("SR Tool v.1.0.7");
		createAndShowGUI();
	}

	private void createAndShowGUI() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());

		/*
		 * webfactory input area
		 */
		webfactoryInputArea = new JTextArea();
		JScrollPane wfsp = new JScrollPane(webfactoryInputArea);
		wfsp.setBorder(BorderFactory.createTitledBorder("WebFactory Report"));
		wfsp.setPreferredSize(new Dimension(250, 75));
		wfsp.setMinimumSize(new Dimension(250, 75));
		add(wfsp, new GridBagConstraints(0, 0, 1, 1, 0.5, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));

		/*
		 * vendor spread input area
		 */
		JPanel vdp = new JPanel(new GridBagLayout());

		vdp.add(new JLabel("Item Type: "), new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		productTypeBox.setSelectedIndex(0);
		vdp.add(productTypeBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 4, 0, 0), 0, 0));

		vdp.add(new JLabel("Show Name: "), new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));
		showNameField = new JTextField();
		vdp.add(showNameField, new GridBagConstraints(1, 1, 1, 1, 0.5, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 4, 0, 0), 0, 0));

		vdp.add(new JLabel("Date: "), new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));
		dateField = new JTextField();
		vdp.add(dateField, new GridBagConstraints(1, 2, 1, 1, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(2, 4, 0, 0), 0, 0));

		vendorInputArea = new JTextArea();
		JScrollPane vdsp = new JScrollPane(vendorInputArea);
		vdsp.setPreferredSize(new Dimension(250, 150));
		vdsp.setMinimumSize(new Dimension(250, 150));
		vdp.add(vdsp, new GridBagConstraints(0, 3, 2, 1, 0, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));

		vdp.setBorder(BorderFactory.createTitledBorder("Show Spread"));
		add(vdp, new GridBagConstraints(0, 1, 1, 1, 0.5, 0.5, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0));

		/*
		 * table
		 */
		TableColumn[] columns = TableColumn.values();
		String[] columnNames = new String[columns.length];
		for (int i = 0; i < columnNames.length; i++)
			columnNames[i] = columns[i].title();
		DefaultTableModel model = new DefaultTableModel(columnNames, 0);
		table = new JTable(model) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class getColumnClass(int column) {
				return TableColumn.values()[column].type();
			}

			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column < TableColumn.COMMIT.column()) {
					renderer.setHorizontalAlignment(SwingConstants.CENTER);
					return renderer;
				}
				return super.getCellRenderer(row, column);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == TableColumn.COMMIT.column();
			}
		};
		table.addMouseListener(this);
		table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));
		JScrollPane tableSP = new JScrollPane(table);
		tableSP.setMinimumSize(new Dimension(300, 0));
		add(tableSP, new GridBagConstraints(1, 0, 1, 3, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		/*
		 * text preview area
		 */
		emailArea = new JTextArea();
		emailArea.setEditable(false);
		JScrollPane outputsp = new JScrollPane(emailArea);
		outputsp.setBorder(BorderFactory.createTitledBorder("Email preview"));
		outputsp.setPreferredSize(new Dimension(250, 0));
		add(outputsp, new GridBagConstraints(2, 0, 2, 2, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));

		/*
		 * button bar
		 */
		JButton b1 = new JButton(ButtonCommand.UPDATE.text());
		b1.setActionCommand(ButtonCommand.UPDATE.name());
		b1.addActionListener(this);
		add(b1, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 2, 2, 2), 0, 0));

		JButton b4 = new JButton(ButtonCommand.COPY_REPORT.text());
		b4.setActionCommand(ButtonCommand.COPY_REPORT.name());
		b4.addActionListener(this);
		add(b4, new GridBagConstraints(2, 2, 1, 1, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 2, 2, 2), 0, 0));

		JButton b5 = new JButton(ButtonCommand.COPY_EMAIL.text());
		b5.setActionCommand(ButtonCommand.COPY_EMAIL.name());
		b5.addActionListener(this);
		add(b5, new GridBagConstraints(3, 2, 1, 1, 0.5, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 2, 2, 2), 0, 0));

		/*
		 * table popup menu
		 */
		JMenuItem item1 = new JMenuItem(ButtonCommand.OPEN_ON_LIVE.text());
		item1.setActionCommand(ButtonCommand.OPEN_ON_LIVE.name());
		item1.addActionListener(this);
		tableMenu.add(item1);

		JMenuItem item2 = new JMenuItem(ButtonCommand.OPEN_ON_TEST.text());
		item2.setActionCommand(ButtonCommand.OPEN_ON_TEST.name());
		item2.addActionListener(this);
		tableMenu.add(item2);

		JMenuItem item3 = new JMenuItem(ButtonCommand.COPY_RANGE.text());
		item3.setActionCommand(ButtonCommand.COPY_RANGE.name());
		item3.addActionListener(this);
		tableMenu.add(item3);

		JMenuItem item4 = new JMenuItem(ButtonCommand.COMMIT.text());
		item4.setActionCommand(ButtonCommand.COMMIT.name());
		item4.addActionListener(this);
		tableMenu.add(item4);

		JMenuItem item5 = new JMenuItem(ButtonCommand.UNCOMMIT.text());
		item5.setActionCommand(ButtonCommand.UNCOMMIT.name());
		item5.addActionListener(this);
		tableMenu.add(item5);

		JMenuItem item6 = new JMenuItem(ButtonCommand.SPECIAL_UNCOMMIT.text());
		item6.setActionCommand(ButtonCommand.SPECIAL_UNCOMMIT.name());
		item6.addActionListener(this);
		tableMenu.add(item6);

		// finish off
		validate();
		pack();
	}

	private void update() {
		readInputAreas();
		updateTable();
		emailArea.setText(makeEmail());
	}

	private void readInputAreas() {

		// read webfactory area
		String[] webfactoryDigits = webfactoryInputArea.getText().split("[\\D]");
		webfactoryItems.clear();
		for (String s : webfactoryDigits)
			if (s.length() == 6)
				webfactoryItems.put(Integer.parseInt(s), blankObject);

		// read vendor area
		String[] vendorDigits = vendorInputArea.getText().split("[\\D]");
		vendorItems.clear();
		for (String s : vendorDigits)
			if (s.length() == 6 && !vendorItems.contains(Integer.parseInt(s)))
				vendorItems.add(Integer.parseInt(s));
	}

	private void updateTable() {

		// remove uncommitted rows
		for (int row = 0; row < table.getModel().getRowCount(); row++)
			if (!(Boolean) table.getModel().getValueAt(row, TableColumn.COMMIT.column()))
				problemItems.remove((Integer) table.getModel().getValueAt(row,
						TableColumn.ITEM_NUMBER.column()));

		// clear table
		((DefaultTableModel) table.getModel()).setRowCount(0);

		// check vendor items against webfactory report
		for (int vendorItem : vendorItems) {
			if (webfactoryItems.get(vendorItem) == null) {
				// Item item = new Item(vendorItem,
				// productTypeBox.getSelectedIndex(),
				// showNameField.getText(), dateField.getText());
				// problemItems.put(vendorItem, item);
			}

		}

		// add new values to the table quietly and then repaint
		// for (Item problemItem : problemItems.values())
		// ((DefaultTableModel) table.getModel()).addRow(problemItem.getRow());

	}

	private void copyReport() {

		String output = "";
		TableModel model = table.getModel();

		// iterate through rows
		for (int row = 0; row < table.getRowCount(); row++) {
			int realRow = table.convertRowIndexToModel(row);
			output = output + model.getValueAt(realRow, TableColumn.ITEM_TYPE.column()) + "\t"
					+ model.getValueAt(realRow, TableColumn.ITEM_NUMBER.column()) + "\t"
					+ ((String) model.getValueAt(realRow, TableColumn.SHOW.column())).trim() + "\t"
					+ model.getValueAt(realRow, TableColumn.DUE_DATE.column()) + "\t"
					+ model.getValueAt(realRow, TableColumn.ON_AIR_DATE.column()) + "\n";
		}

		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(output), null);

	}

	private String makeEmail() {

		String output = "";

		// make a list of shows associated with counts
		Map<String, ShowOld> shows = new HashMap<String, ShowOld>();
		// for (Item item : problemItems.values()) {
		// first instance of show
		// if (shows.get(item.show) == null) {
		// shows.put(item.show, new ShowOld(item));
		// }
		// // increment show
		// else {
		// shows.get(item.show).increment();
		// }
		// }

		// generate a nice pretty email to display
		for (ShowOld show : shows.values()) {
			output = output.concat(show.getEmailString());
		}

		return output;
	}

	protected static String getDueDate(String dateString) {

		Calendar calendar = getCalendarFromString(dateString);

		if (calendar == null)
			return "";

		// try going back two days
		calendar.roll(Calendar.DAY_OF_YEAR, false);
		calendar.roll(Calendar.DAY_OF_YEAR, false);

		// keep rolling until a weekday is found
		while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			calendar.roll(Calendar.DAY_OF_YEAR, false);
		}

		// make the output proper
		return getDateString(calendar);
	}

	protected static Calendar getCalendarFromString(String dateString) {

		Calendar calendar = Calendar.getInstance();

		if (dateString.toLowerCase().contains("jan")) {
			calendar.set(Calendar.MONTH, Calendar.JANUARY);

		} else if (dateString.toLowerCase().contains("feb")) {
			calendar.set(Calendar.MONTH, Calendar.FEBRUARY);

		} else if (dateString.toLowerCase().contains("mar")) {
			calendar.set(Calendar.MONTH, Calendar.MARCH);

		} else if (dateString.toLowerCase().contains("apr")) {
			calendar.set(Calendar.MONTH, Calendar.APRIL);

		} else if (dateString.toLowerCase().contains("may")) {
			calendar.set(Calendar.MONTH, Calendar.MAY);

		} else if (dateString.toLowerCase().contains("jun")) {
			calendar.set(Calendar.MONTH, Calendar.JUNE);

		} else if (dateString.toLowerCase().contains("jul")) {
			calendar.set(Calendar.MONTH, Calendar.JULY);

		} else if (dateString.toLowerCase().contains("aug")) {
			calendar.set(Calendar.MONTH, Calendar.AUGUST);

		} else if (dateString.toLowerCase().contains("sep")) {
			calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);

		} else if (dateString.toLowerCase().contains("oct")) {
			calendar.set(Calendar.MONTH, Calendar.OCTOBER);

		} else if (dateString.toLowerCase().contains("nov")) {
			calendar.set(Calendar.MONTH, Calendar.NOVEMBER);

		} else if (dateString.toLowerCase().contains("dec")) {
			calendar.set(Calendar.MONTH, Calendar.DECEMBER);

		} else {
			return null;
		}

		calendar.set(Calendar.DATE, Integer.parseInt(dateString.replaceAll("\\D", "")));

		return calendar;
	}

	protected static String getDateString(Calendar calendar) {

		String month;
		switch (calendar.get(Calendar.MONTH)) {

		case Calendar.JANUARY:
			month = "Jan ";
			break;

		case Calendar.FEBRUARY:
			month = "Feb ";
			break;

		case Calendar.MARCH:
			month = "Mar ";
			break;

		case Calendar.APRIL:
			month = "Apr ";
			break;

		case Calendar.MAY:
			month = "May ";
			break;

		case Calendar.JUNE:
			month = "Jun ";
			break;

		case Calendar.JULY:
			month = "Jul ";
			break;

		case Calendar.AUGUST:
			month = "Aug ";
			break;

		case Calendar.SEPTEMBER:
			month = "Sep ";
			break;

		case Calendar.OCTOBER:
			month = "Oet ";
			break;

		case Calendar.NOVEMBER:
			month = "Nov ";
			break;

		case Calendar.DECEMBER:
			month = "Dec ";
			break;

		default:
			month = "? ";
			break;
		}

		// send it to the presses
		return month + calendar.get(Calendar.DATE);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// UNUSED
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseReleased(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (e.isPopupTrigger()) {

			int rowUnderMouse = table.rowAtPoint(e.getPoint());

			// check if a valid row is at the point
			if (rowUnderMouse >= 0 && rowUnderMouse < table.getRowCount()) {
				// change selection to the clicked row if not already selected
				if (!table.isRowSelected(rowUnderMouse)) {
					table.setRowSelectionInterval(rowUnderMouse, rowUnderMouse);
				}
			}

			// if invalid row under mouse, deselect everything
			else
				table.clearSelection();

			// if somehow the selection is still wrong, abort
			if (table.getSelectedRow() < 0)
				return;

			// bask in the glory of the popup menu
			tableMenu.show(table, e.getX(), e.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// UNUSED
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// UNUSED
	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		switch (ButtonCommand.valueOf(evt.getActionCommand())) {
		case UPDATE:
			update();
			break;

		case OPEN_ON_LIVE:
			for (int number : getSelectedItems()) {
				try {
					Desktop.getDesktop().browse(
							new URI(QALinkOpener.LIVE_SITE + Integer.toString(number)));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			break;

		case OPEN_ON_TEST:
			for (int number : getSelectedItems()) {
				try {
					Desktop.getDesktop().browse(
							new URI(QALinkOpener.TEST_SITE + Integer.toString(number)));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			break;

		case COMMIT:
			for (int row : table.getSelectedRows())
				table.getModel().setValueAt(true, table.convertRowIndexToModel(row),
						TableColumn.COMMIT.column());
			break;

		case UNCOMMIT:
			for (int row : table.getSelectedRows())
				table.getModel().setValueAt(false, table.convertRowIndexToModel(row),
						TableColumn.COMMIT.column());
			break;

		case SPECIAL_UNCOMMIT:

			String s = (String) JOptionPane.showInputDialog(this,
					"Paste item numbers to uncommit here:", "Targeted Uncommit",
					JOptionPane.QUESTION_MESSAGE, null, null, "");
			System.out.println(s);

			String[] uncommitDigits = s.split("[\\D]");

			for (String digit : uncommitDigits) {
				if (digit.length() == 6) {
					for (int row : table.getSelectedRows()) {
						int itemNum = ((Integer) table.getModel()
								.getValueAt(table.convertRowIndexToModel(row),
										TableColumn.ITEM_NUMBER.column()));
						if (Integer.parseInt(digit) == itemNum)
							table.getModel().setValueAt(false, table.convertRowIndexToModel(row),
									TableColumn.COMMIT.column());
					}
				}
			}

			// for (int row : table.getSelectedRows())
			// table.getModel().setValueAt(false,
			// table.convertRowIndexToModel(row),
			// TableColumn.COMMIT.column());
			break;

		case COPY_RANGE:
			String output = "";
			int[] selectedItems = getSelectedItems();
			for (int number : selectedItems) {
				output = output.concat(number + ",");
			}

			Toolkit.getDefaultToolkit()
					.getSystemClipboard()
					.setContents(new StringSelection(output.substring(0, output.length() - 1)),
							null);

			JOptionPane.showMessageDialog(null, "Items in range: " + selectedItems.length,
					"Copied!", JOptionPane.INFORMATION_MESSAGE);
			break;

		case COPY_REPORT:
			copyReport();
			break;

		case COPY_EMAIL:
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(new StringSelection(emailArea.getText()), null);
			break;

		default:
			break;
		}
	}

	private int[] getSelectedItems() {
		int[] selectedRows = table.getSelectedRows();
		int[] selectedItems = new int[selectedRows.length];

		for (int i = 0; i < selectedRows.length; i++) {
			selectedItems[i] = (Integer) table.getModel()
					.getValueAt(table.convertRowIndexToModel(selectedRows[i]),
							TableColumn.ITEM_NUMBER.column());
		}

		return selectedItems;
	}
}

enum TableColumn {

	ITEM_NUMBER("Item Number", 0, String.class),
	ITEM_TYPE("Item Type", 1, Integer.class),
	SHOW("Show", 2, String.class),
	DUE_DATE("Due Date", 3, String.class),
	ON_AIR_DATE("On-Air Date", 4, String.class),
	COMMIT("Include", 5, Boolean.class);

	private String title;
	private int column;
	@SuppressWarnings("rawtypes")
	private Class type;

	private TableColumn(String title, int column, @SuppressWarnings("rawtypes") Class type) {
		this.title = title;
		this.column = column;
		this.type = type;
	}

	protected String title() {
		return title;
	}

	protected int column() {
		return column;
	}

	@SuppressWarnings("rawtypes")
	protected Class type() {
		return type;
	}
}

enum ButtonCommand {

	UPDATE("Process"),
	OPEN_ON_LIVE("Open on live site"),
	OPEN_ON_TEST("Open on preview site"),
	COMMIT("Commit rows"),
	UNCOMMIT("Uncommit rows"),
	SPECIAL_UNCOMMIT("Targeted uncommit"),
	COPY_RANGE("Copy WF search string"),
	COPY_REPORT("Copy report"),
	COPY_EMAIL("Copy email");

	private String text;

	private ButtonCommand(String text) {
		this.text = text;
	}

	protected String text() {
		return text;
	}

}

class ShowOld {

	protected String name;
	protected int count = 1;
	protected String date;

	public ShowOld(Item item) {
		// name = item.show;
		// date = item.date;
	}

	public void increment() {
		count++;
	}

	public String getEmailString() {
		String output = name.trim() + " (" + count + ")\n";
		output = output.concat("Due: " + SRReportGenerator.getDueDate(date) + "\n");
		output = output.concat("On air: " + date + "\n\n");
		return output;
	}

}
