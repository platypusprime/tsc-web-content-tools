package ca.tsc.util.vod;

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
 * into an appropriate search string for Windows Explorer.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v1.0:</dt>
 * <dd>14-08-03 - Created script</dd>
 * <dd><b>14-08-03 - Changed to look only for VOD</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since August 3, 2014
 * @version 1.0.1
 */
public class WindowsSearchGenerator {

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
				output = ln + "_VOD";
			else
				output = output.concat(" OR " + ln + "_VOD");
			count++;
		}

		StringSelection stringSelection = new StringSelection(output);
		clipboard.setContents(stringSelection, null);

		JOptionPane.showMessageDialog(null, "Items read: " + count, "Done!",
				JOptionPane.INFORMATION_MESSAGE);

		return;

	}
}
