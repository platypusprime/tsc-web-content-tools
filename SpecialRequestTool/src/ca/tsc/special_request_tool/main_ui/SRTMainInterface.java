package ca.tsc.special_request_tool.main_ui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.tsc.special_request_tool.Commodity;
import ca.tsc.special_request_tool.SRShow;
import ca.tsc.special_request_tool.SpecialRequestTool;
import ca.tsc.special_request_tool.pop_up.ShowLoader;
import ca.tsc.special_request_tool.pop_up.WebfactoryLoader;
import ca.tsc.special_request_tool.spread_parser.SpreadParser;

public class SRTMainInterface extends JFrame implements ActionListener, DataReceiver {

	private static final long serialVersionUID = 1L;

	// GUI components
	private ShowTable showTable;
	private ItemTable itemTable;

	// data
	private ArrayList<File> spreads = new ArrayList<File>();

	public SRTMainInterface() {

		super("Special Request Tool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create UI
		setJMenuBar(SRTMenuItem.makeMenuBar(this));

		itemTable = new ItemTable();
		showTable = new ShowTable(itemTable);

		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, showTable.getPane(), itemTable.getPane()));

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void generateSpreadsheetOutput() {
		String output = "";
		for (SRShow show : showTable.getShows()) {
			String str;
			if ((str = show.getSpreadSheetString()) != null)
				output = output.concat(str);

		}
		StringSelection stringSelection = new StringSelection(output);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	private void generateEmailOutput() {

		String output = "";
		for (SRShow show : showTable.getShows()) {
			String str;
			if ((str = show.getEmailString()) != null)
				output = output.concat(str);

		}
		StringSelection stringSelection = new StringSelection(output);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
	}

	@Override
	public void receiveShowData(ArrayList<SRShow> showData) {
		showTable.addShows(showData);
	}

	@Override
	public void receiveWFData(ArrayList<Integer> wfData) {
		showTable.setReferenceList(wfData);

	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		String command = evt.getActionCommand();
		switch (SRTMenuItem.valueOf(command)) {

		case LOAD_SHOWS:
			new ShowLoader(this);
			break;

		case LOAD_HOME_ELECTRONICS:
			JFileChooser fc = new JFileChooser(Commodity.HOME.getFile());
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Home & Electronics Spreadsheets", "xlsx");
			fc.setFileFilter(filter);

			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				showTable.addShows(SpreadParser.parseHEBook(fc.getSelectedFile()));

			break;

		case LOAD_ITEMS:
			new WebfactoryLoader(this);
			break;

		case FIND_SPREADS:
			new Thread(new Runnable() {
				@Override
				public void run() {
					// new SpreadFinder();
					System.out.println("Looking for spreads");
					final long startTime = System.currentTimeMillis();

					spreads = SpecialRequestTool.getSpreads();

					final long endTime = System.currentTimeMillis();

					JOptionPane.showMessageDialog(null, String.format(
							"Total execution time: %.2f sec\n\n", (endTime - startTime) / 1000.0),
							"Spread list populated", JOptionPane.INFORMATION_MESSAGE);

					for (File spread : spreads)
						System.out.println(spread.getName());

				}
			}).start();
			break;

		case MATCH_SPREADS:
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (SRShow show : showTable.getShows())
						show.chooseSpread(spreads);
					showTable.update();
				}
			}).start();
			break;

		case EXIT:
			System.exit(0);
			break;

		case GENERATE_OUTPUT:
			generateSpreadsheetOutput();
			break;

		case GENERATE_EMAIL:
			generateEmailOutput();
			break;

		case OPTIONS:
			// TODO options popup
			// new OptionSetter();
			break;

		case HELP:
			// TODO help popup
			// new HelpPopup();
			break;

		case ABOUT:
			// TODO about popup
			// new InfoPopup();
			break;

		default:
			break;

		}
	}
}