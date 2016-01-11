package ca.tsc.util.web;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Opens a line-separated list of item numbers (e.g. column copied from Excel)
 * sequentially in the system browser. Make sure that new pages are set to open
 * in a new tab on the browser. Opening too many items may take a long time or
 * even fail.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v1.0:</dt>
 * <dd><b>14-07-01 - Created script</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since July 1, 2014
 * @version 1.0.0
 */
public class QALinkOpener {

	public static final String LIVE_SITE = "http://tsc.ca/pages/productdetails?nav=R:";
	public static final String TEST_SITE = "http://tsc.wfactory.ca/pages/productdetails?nav=R:";

	public static void main(String[] args) throws UnsupportedFlavorException, IOException,
			URISyntaxException {

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);

		BufferedReader reader = new BufferedReader(new StringReader(result));
		String ln;

		Desktop desktop = Desktop.getDesktop();
		while ((ln = reader.readLine()) != null)
			desktop.browse(new URI(LIVE_SITE + ln));
		return;

	}
}
