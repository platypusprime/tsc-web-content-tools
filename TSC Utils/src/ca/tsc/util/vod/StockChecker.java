package ca.tsc.util.vod;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class StockChecker {

	private static final String DEFAULT_STATUS = "";

	public static void main(String[] args) throws UnsupportedFlavorException,
			IOException,
			URISyntaxException {

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Clipboard clipboard = toolkit.getSystemClipboard();
		String result = (String) clipboard.getData(DataFlavor.stringFlavor);

		BufferedReader reader = new BufferedReader(new StringReader(result));
		String ln;

		RequestConfig config = RequestConfig.custom()
				.setSocketTimeout(5000).setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000).build();

		// create HttpClient
		CloseableHttpClient client = HttpClients
				.custom()
				.setDefaultRequestConfig(config)
				.disableAutomaticRetries()
				.build();

		String output = null;

		while ((ln = reader.readLine()) != null) {
			System.out.print(ln + ": ");
			int stock = getStock(ln, client);
			System.out.println(stock == -1 ? "In stock" : stock);
			if (output == null)
				output = stock == 0 ? "S/O" : DEFAULT_STATUS;
			else
				output = output.concat("\n"
						+ (stock == 0 ? "S/O" : DEFAULT_STATUS));
		}

		StringSelection stringSelection = new StringSelection(output);
		clipboard.setContents(stringSelection, null);

		return;
	}

	private static int getStock(String itemNum, CloseableHttpClient client)
			throws ClientProtocolException, IOException {
		HttpUriRequest request = RequestBuilder.get()
				.setUri(
						"https://www.theshoppingchannel.com/pages/productdetails?nav=R:"
								+ itemNum)
				.build();
		CloseableHttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				entity.getContent()));

		int stock = -1;

		String ln;
		while ((ln = reader.readLine()) != null) {
			if (ln.contains("class=\"stock\">")) {
				// System.out.println(ln);
				ln = ln.replaceAll(".*class=\"stock\">", "");
				ln = ln.replaceAll("</span>.*", "");
				if (!ln.equals("In Stock"))
					stock = Integer.parseInt(ln.replaceAll("\\D", ""));
				break;
			} else if (ln.contains("class=\"prodResOutOfStock\">")) {
				stock = 0;
				break;
			}
		}

		EntityUtils.consume(entity);
		response.close();
		return stock;
	}

}
