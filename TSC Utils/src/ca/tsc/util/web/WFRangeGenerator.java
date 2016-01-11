package ca.tsc.util.web;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Vector;

/**
 * Parses a line-separated list of item numbers and creates a list of number
 * ranges which can be used as a search query in WebFactory.
 * 
 * @deprecated use <code>WFListGenerator</code> instead
 * @author Jingchen Xu
 * @since July 9, 2014
 * @version 0.0.1
 */
public class WFRangeGenerator {

	public static void main(String[] args) throws UnsupportedFlavorException, IOException,
			URISyntaxException {

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);

		BufferedReader reader = new BufferedReader(new StringReader(result));
		String ln;

		Vector<Integer> bank = new Vector<Integer>();

		while ((ln = reader.readLine()) != null) {
			bank.add(Integer.parseInt(ln.trim()));
		}

		String output = "";

		for (int i = 0; i < bank.size(); i++) {
			if (i == bank.size() - 1) {
				output = output.concat(Integer.toString(bank.get(i)));
			} else if (i == 0) {
				output = output.concat(bank.get(i) + "-");
			} else if (bank.get(i) - bank.get(i - 1) != 1) {
				output = output.concat(bank.get(i - 1) + "," + bank.get(i) + "-");
			}
		}

		StringSelection stringSelection = new StringSelection(output);
		clipboard.setContents(stringSelection, null);

		System.out.println(bank.size() + " items in range");

		return;

	}
}
