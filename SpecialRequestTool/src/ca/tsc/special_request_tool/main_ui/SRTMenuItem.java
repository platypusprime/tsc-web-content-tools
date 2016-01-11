package ca.tsc.special_request_tool.main_ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

enum SRTMenuItem {

	// menu bar
	LOAD_SHOWS("Load shows from ScriptSure", KeyEvent.VK_L),
	LOAD_HOME_ELECTRONICS("Load home/electronics shows", KeyEvent.VK_H, KeyStroke.getKeyStroke(
			KeyEvent.VK_L, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK)),
	LOAD_ITEMS("Add WebFactory report", KeyEvent.VK_R),
	FIND_SPREADS("Find spreadsheets", KeyEvent.VK_F),
	MATCH_SPREADS("Match spreadsheets", KeyEvent.VK_M),
	EXIT("Close", KeyEvent.VK_E, null),
	GENERATE_OUTPUT("Generate spreadsheet output", KeyEvent.VK_O),
	GENERATE_EMAIL("Generate e-mail", KeyEvent.VK_E),
	OPTIONS("Options (under construction)", KeyEvent.VK_O, null),
	HELP("Help (under construction)", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)),
	ABOUT("About (under construction)", KeyEvent.VK_A, null),

	// show context menu
	EDIT_SHOW_NAME("Edit show name", KeyEvent.VK_N, null),
	EDIT_SHOW_DATES("Edit show dates", KeyEvent.VK_D, null),
	EDIT_SHOW_COMMODITY("Edit show commodity", KeyEvent.VK_C, null),
	CHOOSE_SPREAD("Choose spreadsheet", KeyEvent.VK_S, null),
	CLEAR_SPREAD("Clear spreadsheet", KeyEvent.VK_L, null),
	REMOVE_SHOW("Remove show(s)", KeyEvent.VK_R, null),

	// item context menu
	FLAG("Flag for SR", KeyEvent.VK_A, null),
	UNFLAG("Unflag for SR", KeyEvent.VK_A, null),
	WF_SEARCH("Copy WebFactory search string", KeyEvent.VK_W, null),
	CROSS_CHECK("Check against WebFactory results", KeyEvent.VK_A, null);

	private final String text;
	private final int mnemonic;
	private final KeyStroke accelerator;

	private SRTMenuItem(String text, int mnemonic, KeyStroke accelerator) {
		this.text = text;
		this.mnemonic = mnemonic;
		this.accelerator = accelerator;
	}

	private SRTMenuItem(String text, int mnemonic) {
		this(text, mnemonic, KeyStroke.getKeyStroke(mnemonic, ActionEvent.CTRL_MASK));
	}

	public JMenuItem menuItem() {
		JMenuItem mi = new JMenuItem(text);
		mi.setMnemonic(mnemonic);
		if (accelerator != null)
			mi.setAccelerator(accelerator);
		mi.setActionCommand(this.name());
		return mi;
	}

	protected static JMenuBar makeMenuBar(ActionListener listener) {

		JMenuBar mb = new JMenuBar();

		// make file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		mb.add(fileMenu);

		JMenuItem loadShows = LOAD_SHOWS.menuItem();
		JMenuItem loadHE = LOAD_HOME_ELECTRONICS.menuItem();
		JMenuItem loadItems = LOAD_ITEMS.menuItem();
		JMenuItem findSpreads = FIND_SPREADS.menuItem();
		JMenuItem matchSpreads = MATCH_SPREADS.menuItem();
		JMenuItem exit = EXIT.menuItem();

		loadShows.addActionListener(listener);
		loadHE.addActionListener(listener);
		loadItems.addActionListener(listener);
		findSpreads.addActionListener(listener);
		matchSpreads.addActionListener(listener);
		exit.addActionListener(listener);

		fileMenu.add(loadShows);
		fileMenu.add(loadHE);
		fileMenu.add(loadItems);
		fileMenu.add(findSpreads);
		fileMenu.add(matchSpreads);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		// make edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		mb.add(editMenu);

		JMenuItem generateOutput = GENERATE_OUTPUT.menuItem();
		JMenuItem generateEmail = GENERATE_EMAIL.menuItem();
		JMenuItem options = OPTIONS.menuItem();

		generateOutput.addActionListener(listener);
		generateEmail.addActionListener(listener);
		options.addActionListener(listener);

		editMenu.add(generateOutput);
		editMenu.add(generateEmail);
		editMenu.addSeparator();
		editMenu.add(options);

		// make help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		mb.add(helpMenu);

		JMenuItem help = HELP.menuItem();
		JMenuItem about = ABOUT.menuItem();

		help.addActionListener(listener);
		about.addActionListener(listener);

		helpMenu.add(help);
		helpMenu.addSeparator();
		helpMenu.add(about);

		return mb;
	}

	protected static JPopupMenu makeShowsContextMenu(ActionListener listener) {

		JPopupMenu showMenu = new JPopupMenu();

		JMenuItem showName = EDIT_SHOW_NAME.menuItem();
		JMenuItem showCommodity = EDIT_SHOW_COMMODITY.menuItem();
		JMenuItem showDate = EDIT_SHOW_DATES.menuItem();
		JMenuItem showChooseFile = CHOOSE_SPREAD.menuItem();
		JMenuItem showClearFile = CLEAR_SPREAD.menuItem();
		JMenuItem showRemove = REMOVE_SHOW.menuItem();

		showName.addActionListener(listener);
		showCommodity.addActionListener(listener);
		showDate.addActionListener(listener);
		showChooseFile.addActionListener(listener);
		showClearFile.addActionListener(listener);
		showRemove.addActionListener(listener);

		showMenu.add(showName);
		showMenu.add(showCommodity);
		showMenu.add(showDate);
		showMenu.add(showChooseFile);
		showMenu.add(showClearFile);
		showMenu.add(showRemove);

		return showMenu;
	}

	protected static JPopupMenu makeItemsContextMenu(ActionListener listener) {

		JPopupMenu itemMenu = new JPopupMenu();

		JMenuItem flag = FLAG.menuItem();
		JMenuItem unflag = UNFLAG.menuItem();
		JMenuItem searchString = WF_SEARCH.menuItem();
		JMenuItem crossCheck = CROSS_CHECK.menuItem();

		flag.addActionListener(listener);
		unflag.addActionListener(listener);
		searchString.addActionListener(listener);
		crossCheck.addActionListener(listener);

		itemMenu.add(flag);
		itemMenu.add(unflag);
		itemMenu.add(searchString);
		itemMenu.add(crossCheck);

		return itemMenu;
	}

}
