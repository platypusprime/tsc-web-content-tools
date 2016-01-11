package ca.tsc.special_request_tool.main_ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import ca.tsc.special_request_tool.Item;

public class ItemTable implements ActionListener, ComponentListener, MouseListener {

	private JTable table;
	private ItemTableModel model;
	private JPopupMenu menu;
	private JScrollPane pane;

	private ArrayList<Item> items = new ArrayList<Item>();

	public ItemTable() {
		model = new ItemTableModel();
		table = new JTable(model);

		table.setRowSorter(new TableRowSorter<ItemTableModel>(model));
		menu = SRTMenuItem.makeItemsContextMenu(this);

		// set minimum column widths to accommodate titles
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setMinWidth(20);
		table.getColumnModel().getColumn(1).setMinWidth(75);
		table.getColumnModel().getColumn(2).setMinWidth(60);
		table.getColumnModel().getColumn(3).setMinWidth(110);
		table.getColumnModel().getColumn(4).setMinWidth(60);
		table.getColumnModel().getColumn(5).setMinWidth(60);

		// handle right-clicks
		table.addMouseListener(this);

		pane = new JScrollPane(table);
		pane.setBorder(BorderFactory.createTitledBorder("Items"));

		// resize columns when container is resized
		pane.addComponentListener(this);
	}

	public JScrollPane getPane() {
		return pane;
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
		update();
	}

	public void addItems(ArrayList<Item> newItems) {
		items.addAll(newItems);
		update();
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
				model.setData(items);
				resizeTableColumns();
			}
		});
	}

	private void setFlags(boolean b) {
		for (int row : table.getSelectedRows())
			items.get(table.convertRowIndexToModel(row)).overrideFlag(b);
		update();
	}

	private void generateSearchString() {

		String output = "";
		for (int row : table.getSelectedRows()) {
			output = output.concat(items.get(table.convertRowIndexToModel(row)).getItemNumber()
					+ ",");
		}

		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(output.substring(0, output.length() - 1)), null);

		JOptionPane.showMessageDialog(null, "Items in range: " + table.getSelectedRowCount(),
				"Copied!", JOptionPane.INFORMATION_MESSAGE);
	}

	private void crossCheck(String input) {

		String[] nums = input.split("[\\D]");

		for (String num : nums) {
			if (num.length() == 6) {
				for (int row : table.getSelectedRows()) {
					Item item = items.get(table.convertRowIndexToModel(row));
					if (Integer.parseInt(num) == item.getItemNumber())
						item.overrideFlag(false);
				}
			}
		}
		update();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		switch (SRTMenuItem.valueOf(command)) {
		case FLAG:
			setFlags(true);
			break;

		case UNFLAG:
			setFlags(false);
			break;

		case WF_SEARCH:
			generateSearchString();
			break;

		case CROSS_CHECK:
			String input = JOptionPane.showInputDialog("Cross check WebFactory data",
					"Paste rows of items with copy");
			crossCheck(input);
			// TODO cross check pop-up
			// new CrossChecker(this);
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
		resizeTableColumns();
	}

	class ItemTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -3766034017421489667L;

		private ArrayList<Item> items = new ArrayList<Item>();
		private String[] columnNames = { "", "Commodity", "Item #", "Show Name", "Due Date",
				"Air Date", "Notes (in construction)" };

		public ItemTableModel() {
			this.items = new ArrayList<Item>();
		}

		public ItemTableModel(ArrayList<Item> items) {
			this.items = items;
		}

		public void setData(ArrayList<Item> items) {
			this.items = items;
			fireTableDataChanged();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {

			case 0:
				return items.get(rowIndex).getFlag() ? "!!!" : "✓";
			case 1:
				return items.get(rowIndex).getCommodity();
			case 2:
				return items.get(rowIndex).getItemNumber();
			case 3:
				return items.get(rowIndex).getShowName();
			case 4:
				return items.get(rowIndex).getFormattedDueDate();
			case 5:
				return items.get(rowIndex).getFormattedAirDate();
			case 6:
				return items.get(rowIndex).getNotes();
			default:
				return null;
			}
		}

		@Override
		public int getRowCount() {
			return items.size();
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
		// unused
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// unused
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// unused
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// unused
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// unused
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// unused
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// unused
	}

}
