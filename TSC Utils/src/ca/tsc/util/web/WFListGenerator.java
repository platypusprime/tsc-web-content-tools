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

import javax.swing.JOptionPane;

/**
 * Turns a line-separated list of item numbers (e.g. column copied from Excel)
 * into an appropriate search string for WebFactory.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v1.0:</dt>
 * <dd>14-07-16 - Created script</dd>
 * <dd><b>14-07-30 - Added item count popup and minor fixes</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since July 16, 2014
 * @version 1.0.1
 */
public class WFListGenerator {

	public static void main(String[] args) throws UnsupportedFlavorException, IOException,
			URISyntaxException {

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);

		BufferedReader reader = new BufferedReader(new StringReader(result));
		String ln;

		String output = null;
		int count = 0;
		while ((ln = reader.readLine()) != null) {
			if (output == null)
				output = ln;
			else
				output = output.concat("," + ln);
			count++;
		}

		StringSelection stringSelection = new StringSelection(output);
		clipboard.setContents(stringSelection, null);

		JOptionPane.showMessageDialog(null, "Items read: " + count, "Done!",
				JOptionPane.INFORMATION_MESSAGE);

		return;

	}
}
