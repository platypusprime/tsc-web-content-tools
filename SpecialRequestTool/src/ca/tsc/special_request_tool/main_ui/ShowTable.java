package ca.tsc.special_request_tool.main_ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import ca.tsc.special_request_tool.Commodity;
import ca.tsc.special_request_tool.Item;
import ca.tsc.special_request_tool.SRShow;

public class ShowTable implements ActionListener, MouseListener, ComponentListener,
		ListSelectionListener {

	private JTable table;
	private ShowTableModel model;
	private ItemTable itemTable;
	private JPopupMenu menu;
	private JScrollPane pane;

	private ArrayList<SRShow> shows = new ArrayList<SRShow>();
	private ArrayList<Integer> referenceList = new ArrayList<Integer>();

	public ShowTable(ItemTable itemTable) {
		model = new ShowTableModel();
		table = new JTable(model);
		this.itemTable = itemTable;

		table.setRowSorter(new TableRowSorter<ShowTableModel>(model));
		menu = SRTMenuItem.makeShowsContextMenu(this);

		// set minimum column widths to accommodate titles
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setMinWidth(110);
		table.getColumnModel().getColumn(1).setMinWidth(75);
		table.getColumnModel().getColumn(2).setMinWidth(60);
		table.getColumnModel().getColumn(3).setMinWidth(60);

		// mirror selection with item table
		table.getSelectionModel().addListSelectionListener(this);

		// handle right-clicks
		table.addMouseListener(this);

		pane = new JScrollPane(table);
		pane.setBorder(BorderFactory.createTitledBorder("Shows"));

		// resize columns when container is resized
		pane.addComponentListener(this);
	}

	public JScrollPane getPane() {
		return pane;
	}

	public ArrayList<SRShow> getShows() {
		return shows;
	}

	public void setShows(ArrayList<SRShow> shows) {
		this.shows = shows;
		update();
	}

	public void addShows(ArrayList<SRShow> newShows) {
		shows.addAll(newShows);
		update();
	}

	public void setReferenceList(ArrayList<Integer> wfData) {
		referenceList = wfData;

		for (SRShow show : shows)
			show.updateFlags(referenceList);
		itemTable.update();

		for (int i : referenceList)
			System.out.println(i);

	}

	protected void resizeTableColumns() {
		// TODO handle the case where there isn't enough space
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int totalWidth = 0;
				for (int column = 0; column < table.getColumnCount() - 1; column++) {
					TableColumn tableColumn = table.getColumnModel().getColumn(column);
					int preferredWidth = tableColumn.getMinWidth();
					int maxWidth = tableColumn.getMaxWidth();

					for (int row = 0; row < table.getRowCount(); row++) {
						TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
						Component c = table.prepareRenderer(cellRenderer, row, column);
						int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
						preferredWidth = Math.max(preferredWidth, width);

						// We've exceeded the maximum width, no need to check
						// other rows
						if (preferredWidth >= maxWidth) {
							preferredWidth = maxWidth;
							break;
						}
					}

					tableColumn.setPreferredWidth(preferredWidth);
					totalWidth += preferredWidth;
				}

				// last column occupies remainder of space
				TableColumn tableColumn = table.getColumnModel().getColumn(
						table.getColumnCount() - 1);
				tableColumn.setPreferredWidth(table.getParent().getWidth() - totalWidth);
			}
		});
	}

	protected void update() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				model.setData(shows);
				resizeTableColumns();
			}
		});
	}

	private void editName() {
		int row = table.getSelectedRow();
		if (row != -1) {
			String newName = JOptionPane.showInputDialog("Enter new show name",
					shows.get(table.convertRowIndexToModel(row)).getName());
			if (newName != null) {
				shows.get(table.convertRowIndexToModel(row)).setName(newName);
				update();
			}
		}
	}

	private void editCommodities() {
		Commodity newCommodity = (Commodity) JOptionPane.showInputDialog(table,
				"Select new commodity", "Edit commodity", JOptionPane.QUESTION_MESSAGE, null,
				Commodity.values(), Commodity.UNKNOWN);
		if (newCommodity != null)
			for (int row : table.getSelectedRows())
				shows.get(table.convertRowIndexToModel(row)).setCommodity(newCommodity);
		update();
	}

	private void editDate() {

		// TODO
		// int row;
		// if ((row = getSelectedRow()) != -1) {
		// new DatePicker(shows.get(convertRowIndexToModel(row)));
		// }
	}

	private void editFile() {
		int row = table.getSelectedRow();
		if (row != -1) {

			JFileChooser fc = new JFileChooser(new File("J:/Merchandise Planner/Show Spreads"));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Spreadsheets", "xls",
					"xlsm", "xlsx");
			fc.setFileFilter(filter);

			if (fc.showOpenDialog(table) == JFileChooser.APPROVE_OPTION) {
				shows.get(table.convertRowIndexToModel(row)).setFile(fc.getSelectedFile());
				update();
			}

			// TODO new SpreadPicker(shows.get(row));
		}

	}

	private void clearSpread() {
		for (int row : table.getSelectedRows())
			shows.get(table.convertRowIndexToModel(row)).setFile(null);
		update();
	}

	private void removeShows() {

		ArrayList<Integer> remove = new ArrayList<Integer>();
		for (int row : table.getSelectedRows()) {
			remove.add(table.convertRowIndexToModel(row));
		}
		Collections.sort(remove);
		ArrayList<SRShow> newShows = new ArrayList<SRShow>(shows);
		for (int i = remove.size() - 1; i >= 0; i--) {
			newShows.remove((int) remove.get(i));
		}
		shows = newShows;
		update();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getValueIsAdjusting())
			new Thread(new Runnable() {

				@Override
				public void run() {

					// update items
					final ArrayList<Item> displayItems = new ArrayList<Item>();
					for (int row : table.getSelectedRows()) {
						shows.get(table.convertRowIndexToModel(row)).updateFlags(referenceList);
						displayItems.addAll(shows.get(table.convertRowIndexToModel(row)).getItems());
					}
					itemTable.setItems(displayItems);
				}
			}).start();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		String command = evt.getActionCommand();
		switch (SRTMenuItem.valueOf(command)) {

		case EDIT_SHOW_NAME:
			editName();
			break;

		case EDIT_SHOW_DATES:
			editDate();
			break;

		case EDIT_SHOW_COMMODITY:
			editCommodities();
			break;

		case CHOOSE_SPREAD:
			editFile();
			break;

		case CLEAR_SPREAD:
			clearSpread();
			break;

		case REMOVE_SHOW:
			removeShows();
			break;

		default:
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {

			int rowUnderMouse = table.rowAtPoint(e.getPoint());

			// check if a valid row is at the point
			if (rowUnderMouse >= 0 && rowUnderMouse < table.getRowCount()) {
				// change selection to the clicked row if not already
				// selected
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

			menu.show(table, e.getX(), e.getY());
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		update();
	}

	class ShowTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 7349232741526186570L;

		private ArrayList<SRShow> shows = new ArrayList<SRShow>();;
		private String[] columnNames = { "Show Name", "Commodity", "Air Date", "Due Date",
				"Workbook" };

		public ShowTableModel() {
			this.shows = new ArrayList<SRShow>();
		}

		public ShowTableModel(ArrayList<SRShow> shows) {
			this.shows = shows;
		}

		public void setData(ArrayList<SRShow> shows) {
			this.shows = shows;
			fireTableDataChanged();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {

			case 0:
				return shows.get(rowIndex).getName();
			case 1:
				return shows.get(rowIndex).getCommodity().toString();
			case 2:
				return shows.get(rowIndex).getFormattedAirDate();
			case 3:
				return shows.get(rowIndex).getFormattedDueDate();
			case 4:
				File f = shows.get(rowIndex).getFile();
				return f != null ? f.getName() : null;
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return shows.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// UNUSED
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// UNUSED
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
	public void componentShown(ComponentEvent e) {
		// UNUSED
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// UNUSED
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// UNUSED
	}

}
