package ca.tsc.util.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import platypus.util.general.PStringUtils;

/**
 * ScriptSureRequests contains functions for accessing ScriptSure using HTTP
 * requests. Includes authentication, loading shows, and loading scripts.
 * 
 * <dl>
 * <b>Version History</b>
 * <dt>v1.0:</dt>
 * <dd>14-11-30 - Moved methods here from AutoScripter</dd>
 * <dd><b>14-12-06 - Cleaned up libraries</b></dd>
 * </dl>
 * 
 * @author Jingchen Xu
 * @since November 30, 2014
 * @version 1.0.1
 */
public class ScriptSureRequests {

	public static final String SCRIPTSURE_HOME = "http://www.tscscriptsure.com/";
	public static final String SCRIPTSURE_OUTPUT = "http://www.tscscriptsure.com/Output/ScriptsDailyView.aspx";
	private static final String SCRIPT_OUTPUT = "http://www.tscscriptsure.com/Output/Output.aspx";
	private static final String SCRIPT_URL_BASE = "http://www.tscscriptsure.com/Output/Output_ItemPage.aspx?Parameters=";

	// show list constants
	private static final String SHOW_IDENTIFIER = "class=\"labeltextlink\" href=\"Script_Output.aspx?ShowID";

	private static final String LOGIN_VIEWSTATE = "dDwtMTEwODI5MjI2OTs7Pphw5dQVM6hGr7Bo5RREMWXASC1u";
	private static final String INITIAL_VIEWSTATE = "dDwtMTgyNTQ3NjYyNzt0PDtsPGk8MT47aTwzPjs+O2w8dDxwPGw8aW5uZXJodG1sOz47bDwyNzA5MTQwMDAwIC0gQ29va2luZyB3aXRoIFdvbGZnYW5nIFB1Y2s7Pj47Oz47dDw7bDxpPDM+O2k8MTE+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPEN1c3RvbWVyIENhcmU7Pj47Pjs7Pjt0PDtsPGk8MT47aTwzPjtpPDU+O2k8Nz47aTw5PjtpPDExPjtpPDEzPjtpPDE1PjtpPDE3PjtpPDE5PjtpPDIxPjtpPDIzPjtpPDI1PjtpPDI3PjtpPDI5PjtpPDMxPjtpPDMzPjtpPDM1PjtpPDM3PjtpPDM5PjtpPDQxPjtpPDQ1PjtpPDQ3PjtpPDQ5PjtpPDUxPjtpPDU3Pjs+O2w8dDw7bDxpPDE+O2k8Mz47PjtsPHQ8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxDb29raW5nIHdpdGggV29sZmdhbmcgUHVjazs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDwyNzA5MTQwMDAwICh2MS44KTs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PDtsPGk8MD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8U2F0LCBTZXAgMjcgMDA6MDAgKDAxOjAwLjAwKTs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjs+O2w8dDxwPGw8XyFJdGVtQ291bnQ7PjtsPGk8MTI+Oz4+O2w8aTwwPjtpPDE+O2k8Mj47aTwzPjtpPDQ+O2k8NT47aTw2PjtpPDc+O2k8OD47aTw5PjtpPDEwPjtpPDExPjs+O2w8dDw7bDxpPDE+O2k8Mz47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8U2VwIDI2Oz4+Oz47Oz47dDxwPHA8bDxUZXh0O1RhcmdldDtOYXZpZ2F0ZVVybDs+O2w8MTk6MDA7X2JsYW5rO1NjcmlwdF9PdXRwdXQuYXNweD9TaG93SUQ9MzQ5MjM3Jk91dHB1dFR5cGU9NDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PHA8cDxsPFRleHQ7VmlzaWJsZTs+O2w8U2VwIDI2O288Zj47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7VGFyZ2V0O05hdmlnYXRlVXJsOz47bDwyMDowMDtfYmxhbms7U2NyaXB0X091dHB1dC5hc3B4P1Nob3dJRD0zNDkyMzgmT3V0cHV0VHlwZT00Oz4+Oz47Oz47Pj47dDw7bDxpPDE+O2k8Mz47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8U2VwIDI3Oz4+Oz47Oz47dDxwPHA8bDxDc3NDbGFzcztUZXh0O18hU0I7PjtsPGxhYmVsdGV4dDswMDowMDtpPDI+Oz4+Oz47Oz47Pj47dDw7bDxpPDE+O2k8Mz47PjtsPHQ8cDxwPGw8VGV4dDtWaXNpYmxlOz47bDxTZXAgMjc7bzxmPjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDtUYXJnZXQ7TmF2aWdhdGVVcmw7PjtsPDA4OjAwO19ibGFuaztTY3JpcHRfT3V0cHV0LmFzcHg/U2hvd0lEPTM0OTI0MCZPdXRwdXRUeXBlPTQ7Pj47Pjs7Pjs+Pjt0PDtsPGk8MT47aTwzPjs+O2w8dDxwPHA8bDxUZXh0O1Zpc2libGU7PjtsPFNlcCAyNztvPGY+Oz4+Oz47Oz47dDxwPHA8bDxUZXh0O1RhcmdldDtOYXZpZ2F0ZVVybDs+O2w8MTE6MDA7X2JsYW5rO1NjcmlwdF9PdXRwdXQuYXNweD9TaG93SUQ9MzQ5MjQxJk91dHB1dFR5cGU9NDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PHA8cDxsPFRleHQ7VmlzaWJsZTs+O2w8U2VwIDI3O288Zj47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7VGFyZ2V0O05hdmlnYXRlVXJsOz47bDwxNDowMDtfYmxhbms7U2NyaXB0X091dHB1dC5hc3B4P1Nob3dJRD0zNTQ2MzkmT3V0cHV0VHlwZT00Oz4+Oz47Oz47Pj47dDw7bDxpPDE+O2k8Mz47PjtsPHQ8cDxwPGw8VGV4dDtWaXNpYmxlOz47bDxTZXAgMjc7bzxmPjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDtUYXJnZXQ7TmF2aWdhdGVVcmw7PjtsPDE1OjAwO19ibGFuaztTY3JpcHRfT3V0cHV0LmFzcHg/U2hvd0lEPTM0OTI0MyZPdXRwdXRUeXBlPTQ7Pj47Pjs7Pjs+Pjt0PDtsPGk8MT47aTwzPjs+O2w8dDxwPHA8bDxUZXh0O1Zpc2libGU7PjtsPFNlcCAyNztvPGY+Oz4+Oz47Oz47dDxwPHA8bDxUZXh0O1RhcmdldDtOYXZpZ2F0ZVVybDs+O2w8MTU6MzA7X2JsYW5rO1NjcmlwdF9PdXRwdXQuYXNweD9TaG93SUQ9MzU0NzM3Jk91dHB1dFR5cGU9NDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PHA8cDxsPFRleHQ7VmlzaWJsZTs+O2w8U2VwIDI3O288Zj47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7VGFyZ2V0O05hdmlnYXRlVXJsOz47bDwxODowMDtfYmxhbms7U2NyaXB0X091dHB1dC5hc3B4P1Nob3dJRD0zNDkyNDQmT3V0cHV0VHlwZT00Oz4+Oz47Oz47Pj47dDw7bDxpPDE+O2k8Mz47PjtsPHQ8cDxwPGw8VGV4dDtWaXNpYmxlOz47bDxTZXAgMjc7bzxmPjs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDtUYXJnZXQ7TmF2aWdhdGVVcmw7PjtsPDE5OjAwO19ibGFuaztTY3JpcHRfT3V0cHV0LmFzcHg/U2hvd0lEPTM0OTI0NSZPdXRwdXRUeXBlPTQ7Pj47Pjs7Pjs+Pjt0PDtsPGk8MT47aTwzPjs+O2w8dDxwPHA8bDxUZXh0O1Zpc2libGU7PjtsPFNlcCAyNztvPGY+Oz4+Oz47Oz47dDxwPHA8bDxUZXh0O1RhcmdldDtOYXZpZ2F0ZVVybDs+O2w8MTk6MzA7X2JsYW5rO1NjcmlwdF9PdXRwdXQuYXNweD9TaG93SUQ9MzU0NzM4Jk91dHB1dFR5cGU9NDs+Pjs+Ozs+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PHA8cDxsPFRleHQ7VmlzaWJsZTs+O2w8U2VwIDI3O288Zj47Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7VGFyZ2V0O05hdmlnYXRlVXJsOz47bDwyMTowMDtfYmxhbms7U2NyaXB0X091dHB1dC5hc3B4P1Nob3dJRD0zNDkyNDYmT3V0cHV0VHlwZT00Oz4+Oz47Oz47Pj47Pj47Pj47Pj47dDw7bDxpPDE+O2k8Mz47PjtsPHQ8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxSaWNoYXJkIE5lc3Rlcjs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxGcmksIFNlcCAyNiAxMTo0NDs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PDtsPGk8MD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8R3JlZyBHZXR6LCBNYXJpYW4gR2V0eiwgV29sZmdhbmcgUHVjazs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxOYWRpbmUgUXVlZW5zYm9yb3VnaDs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PDtsPGk8MD47aTwyPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxMZXNsaWUgTWlsbmU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCgpOz4+Oz47Oz47Pj47dDw7bDxpPDA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPEFsaXNoYSBNaXRoYTs+Pjs+Ozs+Oz4+Oz4+O3Q8cDxsPHN0eWxlOz47bDxESVNQTEFZOm5vbmVcOzs+PjtsPGk8MT47aTwzPjs+O2w8dDw7bDxpPDA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPFxlOz4+Oz47Oz47Pj47dDw7bDxpPDA+O2k8MT47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8SG9tZTs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8ICguKTs+Pjs+Ozs+Oz4+Oz4+O3Q8cDxwPGw8VGV4dDs+O2w8XGU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPG5vbmU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPG5vbmU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPG5vbmU7Pj47Pjs7Pjt0PDtsPGk8Mj47aTwzPjs+O2w8dDxwPGw8Ymdjb2xvcjs+O2w8TGlnaHRHcmV5Oz4+Ozs+O3Q8cDxsPGJnY29sb3I7PjtsPExpZ2h0R3JleTs+PjtsPGk8MD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8UmVjb3JkOz4+Oz47Oz47Pj47Pj47dDw7bDxpPDA+Oz47bDx0PDtsPGk8MT47aTwzPjtpPDU+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDU4OTM3Mjs+PjtwPGw8b25jbGljaztvbm1vdXNlb3Zlcjs+O2w8aWYgKChzY3JlZW4ud2lkdGggPT0gODAwKSAmJiAoc2NyZWVuLmhlaWdodCA9PSA2MDApKSB7IHZhciB3PXdpbmRvdy5vcGVuKCcuLi9Qb3B1cHMvUHJvZHVjdC5hc3B4P0l0ZW1OdW1iZXI9NTg5MzcyJlNjaGVkdWxlRGF0ZT17MX0nLCAnVmlld0RldGFpbHMnLCAnd2lkdGg9NzkwLGhlaWdodD01MjAsbGVmdD0wLHRvcD0wLHRpdGxlYmFyPW5vLHNjcm9sbGJhcnM9eWVzJylcO3cuZm9jdXMoKVw7IH0gZWxzZSB7IHZhciB3PXdpbmRvdy5vcGVuKCcuLi9Qb3B1cHMvUHJvZHVjdC5hc3B4P0l0ZW1OdW1iZXI9NTg5MzcyJlNjaGVkdWxlRGF0ZT17MX0nLCAnVmlld0RldGFpbHMnLCAnd2lkdGg9ODI1LGhlaWdodD03MDAsbGVmdD0wLHRvcD0wLHRpdGxlYmFyPW5vLHNjcm9sbGJhcnM9eWVzJylcO3cuZm9jdXMoKVw7IH07dGhpcy5zdHlsZS5jdXJzb3I9J2hhbmQnOz4+Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPCwgU0VUIE9GIDUgU0lNVUxBVEVEIFBFQVJMIEJVVFRPTiBFQVJSSU5HUywgJDQ5Ljk5Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDxcZTs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDE+O2k8NT47PjtsPHQ8cDxwPGw8Q3NzQ2xhc3M7VGV4dDtfIVNCOz47bDxsYWJlbHRleHQ7bm9uZTtpPDI+Oz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDxcZTs+Pjs+Ozs+Oz4+Oz4+O3Q8cDxsPHN0eWxlOz47bDxESVNQTEFZOm5vbmVcOzs+PjtsPGk8MT47PjtsPHQ8O2w8aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxcZTs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjs+O2w8dDw7bDxpPDE+O2k8Mz47aTw1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxNaW5pbXVtIDYgRWFzeSBQYXkgUGF5bWVudHM7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPFxlOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDxcZTs+Pjs+Ozs+Oz4+Oz4+O3Q8O2w8aTwxPjtpPDM+Oz47bDx0PDtsPGk8MD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8JDEwMC4wMCBLOz4+Oz47Oz47Pj47dDw7bDxpPDA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPDM0Ljg4ICU7Pj47Pjs7Pjs+Pjs+Pjt0PDtsPGk8MT47aTwzPjs+O2w8dDw7bDxpPDA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPCQxMDIuMTYgSzs+Pjs+Ozs+Oz4+O3Q8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDw1MjUgdW5pdHM7Pj47Pjs7Pjs+Pjs+Pjt0PHA8bDxzdHlsZTs+O2w8RElTUExBWTpub25lXDs7Pj47bDxpPDE+Oz47bDx0PDtsPGk8MD47PjtsPHQ8cDxwPGw8VGV4dDs+O2w8NDg7Pj47Pjs7Pjs+Pjs+Pjt0PDtsPGk8MT47PjtsPHQ8O2w8aTwwPjs+O2w8dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47Oz47Pj47Pj47dDxwPGw8VmlzaWJsZTs+O2w8bzxmPjs+Pjs7Pjt0PHA8bDxzdHlsZTs+O2w8RElTUExBWTpub25lXDs7Pj47bDxpPDE+Oz47bDx0PDtsPGk8MD47aTwyPjtpPDQ+O2k8Nj47aTw4Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxUaGUgU291bmQgb2YgQm9zZTs+PjtwPGw8b25jbGljaztvbm1vdXNlb3Zlcjs+O2w8dmFyIHB0ciA9IHdpbmRvdy5vcGVuKCdTY3JpcHRfT3V0cHV0LmFzcHg/U2hvd0lEPTM1NDUxNiZPdXRwdXRUeXBlPTQnKVw7IHB0ci5mb2N1cygpXDs7dGhpcy5zdHlsZS5jdXJzb3I9J2hhbmQnOz4+Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPChUYXBlZC8pOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDwwMTowMC4wMDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8XGU7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPFxlOz4+Oz47Oz47Pj47Pj47dDxwPGw8VGV4dDs+O2w8XDx0ciB2YWxpZ249InRvcCJcPlw8dGQgY2xhc3M9ImxhYmVsdGV4dCJcPlNob3dyb29tOlw8L3RkXD5cPHRkIGNvbHNwYW49IjQiIGNsYXNzPSJsYWJlbHRleHQiXD5QbGFubmVyczpcPGJyXD42YSAtIDRwID0gQWxpc2hhIGV4dC4gODUxMFw8YnJcPjJwIC0gTWlkbmlnaHQgPSBOYWRpbmUgZXh0LiA3OTIyIFw8YnJcPjlhIC0gNXAgPSBJcmluYSBleHQuIDc3NDIgICYgSW5lc3NhIGV4dC4gNzc2NVw8YnJcPlw8YnJcPkd1ZXN0KHMpOlw8YnJcPldvbGZnYW5nIFB1Y2sgXDxiclw+TWFyaWFuIEdldHpcPC90ZFw+XDwvdHJcPjs+Pjs7Pjt0PHA8bDxzdHlsZTs+O2w8RElTUExBWTpub25lXDs7Pj47Oz47dDxwPGw8c3R5bGU7PjtsPERJU1BMQVk6bm9uZVw7Oz4+Ozs+O3Q8cDxsPHN0eWxlOz47bDxESVNQTEFZOm5vbmVcOzs+Pjs7Pjt0PEAwPHA8cDxsPERhdGFLZXlzO18hSXRlbUNvdW50O1BhZ2VDb3VudDtfIURhdGFTb3VyY2VJdGVtQ291bnQ7PjtsPGw8PjtpPDE+O2k8MT47aTwxPjs+Pjs+Ozs7Ozs7Ozs7Oz47bDxpPDA+Oz47bDx0PDtsPGk8MT47PjtsPHQ8O2w8aTwwPjs+O2w8dDxwPHA8bDxUZXh0Oz47bDxJdGVtICM1MTM3NDIgYWRkZWQgdG8gc2VxdWVuY2UuOz4+Oz47Oz47Pj47Pj47Pj47Pj47Pj47Pj47Pky3JFEu0mTCkSSKaCFPBFPhu5x7";

	// script parsing constants
	private static final Pattern SCRIPT_NUMBER_PATTERN = Pattern.compile("\\d{10}");
	private static final String ITEM_BLOCK_IDENTIFIER = "mLabel\" class=\"labeltextblack\">";
	private static final String ITEM_BLOCK_TERMINATOR = "</tr>";
	private static final String ITEM_NUMBER_IDENTIFIER = "ItemNumberTextBox\').value=\'";
	private static final String ITEM_NUMBER_TERMINATOR = "\'";
	private static final String START_TIME_IDENTIFIER = "StartTimeLabel\" class=\"labeltextblack\">";
	private static final String START_TIME_TERMINATOR = "<";
	private static final String HOUR_TERMINATOR = ":";
	private static final String DURATION_IDENTIFIER = "ationLabel\" class=\"labeltextblack\">";
	private static final String DURATION_TERMINATOR = "<";

	private static CloseableHttpClient client;

	private static CloseableHttpClient getHttpClient() {

		// create client if not set up
		if (client == null) {
			// set up cookies
			RequestConfig globalConfig = RequestConfig.custom()
					.setCookieSpec(CookieSpecs.BEST_MATCH).build();
			CookieStore cookieStore = (CookieStore) new BasicCookieStore();
			HttpClientContext context = HttpClientContext.create();
			context.setCookieStore((org.apache.http.client.CookieStore) cookieStore);

			// set up timeout
			RequestConfig config = RequestConfig.custom()
					.setSocketTimeout(5000).setConnectTimeout(5000)
					.setConnectionRequestTimeout(5000).build();

			// create HttpClient
			client = HttpClients
					.custom()
					.setDefaultRequestConfig(globalConfig)
					.setDefaultCookieStore(
							(org.apache.http.client.CookieStore) cookieStore)
					.setDefaultRequestConfig(config).disableAutomaticRetries()
					.build();
		}

		return client;
	}

	/**
	 * Sends a login request to ScriptSure with the specified credentials.
	 * TODO handle case where credentials are invalid.
	 * 
	 * @param username username to login with
	 * @param password password to login with
	 * @throws IOException if the request fails for any reason
	 * @return true if successful, false otherwise
	 */
	public static boolean login(String username, String password)
			throws IOException {

		HttpUriRequest login = null;
		try {
			login = RequestBuilder.post()
					.setUri(new URI(SCRIPTSURE_HOME))
					.addParameter("LoginButton", "Login")
					.addParameter("UserNameTextBox", username)
					.addParameter("PasswordTextBox", password)
					.addParameter("__VIEWSTATE", LOGIN_VIEWSTATE).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

		CloseableHttpResponse response = null;
		try {
			response = getHttpClient().execute(login);
		} finally {
			if (response != null) {
				EntityUtils.consume(response.getEntity());
				response.close();
			}
		}
		return true;
	}

	/**
	 * Obtains a VIEWSTATE parameter for use in subsequent ScriptSure output
	 * HTTP requests.
	 * 
	 * @return the VIEWSTATE key given by ScriptSure for the next request
	 * @throws IOException if the request fails for any reason
	 */
	public static String getOutputViewState() throws IOException {

		String viewState = null;

		HttpUriRequest request;
		try {
			request = RequestBuilder.get()
					.setUri(new URI(SCRIPTSURE_OUTPUT)).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		CloseableHttpResponse response = null;
		try {
			response = getHttpClient().execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String ln;
			while ((ln = reader.readLine()) != null) {
				if (ln.contains("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"")) {
					viewState = PStringUtils.substring(ln, "value=\"", "\" />");
					break;
				}
			}

		} finally {
			if (response != null) {
				EntityUtils.consume(response.getEntity());
				response.close();
			}
		}

		return viewState;
	}

	/**
	 * Change a parameter for the output page. Notably, the date to be loaded
	 * can be changed through this method.
	 * 
	 * @param param the name of the parameter
	 * @param val the new value of the parameter
	 * @param viewState the VIEWSTATE key to use for the request
	 * @return the VIEWSTATE key given by ScriptSure for the next request
	 * @throws IOException if the request fails for any reason
	 */
	public static String changeOutputParam(String param, String val,
			String viewState) throws IOException {

		String newState = null;

		HttpUriRequest request;
		try {
			request = RequestBuilder.post()
					.setUri(new URI(SCRIPTSURE_OUTPUT))
					.addParameter(param, val)
					.addParameter("OutputDropDownList", "4")
					.addParameter("__VIEWSTATE", viewState)
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		CloseableHttpResponse response = null;
		try {
			response = getHttpClient().execute(request);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String ln;
			while ((ln = reader.readLine()) != null) {
				if (ln.contains("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"")) {
					newState = PStringUtils.substring(ln, "value=\"", "\" />");
					break;
				}
			}
		} finally {
			if (response != null) {
				EntityUtils.consume(response.getEntity());
				response.close();
			}
		}

		return newState;
	}

	public static ArrayList<Show> listShows() throws IOException,
			URISyntaxException {
		return listShows(Calendar.getInstance());
	}

	public static ArrayList<Show> listShows(Calendar date)
			throws IOException, URISyntaxException {

		ArrayList<Show> shows = new ArrayList<Show>();

		// set date parameters
		String viewState = ScriptSureRequests.getOutputViewState();
		Calendar today = Calendar.getInstance();
		if (date.get(Calendar.YEAR) != today.get(Calendar.YEAR)
				|| date.get(Calendar.MONTH) != today.get(Calendar.MONTH)
				|| date.get(Calendar.DATE) != today.get(Calendar.DATE)) {

			// set year
			viewState = changeOutputParam("YearDropDownList",
					Integer.toString(date.get(Calendar.YEAR)), viewState);

			// set month
			viewState = changeOutputParam("MonthDropDownList",
					Integer.toString(date.get(Calendar.MONTH) + 1), viewState);
		}

		// build request for show list
		HttpUriRequest request = RequestBuilder
				.post()
				.setUri(new URI(SCRIPTSURE_OUTPUT))
				.addParameter("DayDropDownList",
						Integer.toString(date.get(Calendar.DATE)))
				.addParameter("OutputDropDownList", "4")
				.addParameter("__VIEWSTATE", viewState).build();

		// send request for show list
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		try {
			response = client.execute(request);
			entity = response.getEntity();

			// read page source
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				String ln;
				while ((ln = reader.readLine()) != null) {
					if (ln.contains(SHOW_IDENTIFIER)) {
						URI link = new URI(
								"http://www.tscscriptsure.com/Output/"
										+ PStringUtils.substring(ln, "href=\"",
												"\" "));
						ln = reader.readLine();
						String showName = PStringUtils.substring(ln, "\">", "<")
								.replaceAll("Destination Style: ", "")
								.replaceAll("Destination Beauty: ", "");

						while (!ln.matches(".+\\d{10}.+"))
							ln = reader.readLine();

						Matcher matcher = SCRIPT_NUMBER_PATTERN.matcher(ln);
						matcher.find();
						String scriptNum = matcher.group(0);

						Show show = new Show(showName, scriptNum,
								link);

						// add if not duplicate
						if (!shows.contains(show)) {
							shows.add(show);
						}
					}
				}
			} finally {
				reader.close();
			}
		} finally {
			// clean up
			EntityUtils.consume(entity);
			response.close();
		}

		return shows;
	}

	public static ArrayList<OnAirItem> scrapeShow(URI showURI)
			throws IOException {

		// load show (in overview tab)
		HttpUriRequest showRequest = RequestBuilder.get()
				.setUri(showURI)
				.build();

		// scrape page for parameter to access show details
		String param = null;
		CloseableHttpResponse showResponse = null;
		HttpEntity showEntity = null;
		try {
			showResponse = getHttpClient().execute(showRequest);
			showEntity = showResponse.getEntity();

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(showEntity.getContent()));
				String ln = null;
				while ((ln = reader.readLine()) != null) {
					if (ln.contains("input name=\"ParameterTextBox\"")) {
						param = PStringUtils.substring(ln, "value=\"", "\" ");
						break;
					}
				}
			} finally {
				if (reader != null)
					reader.close();
			}
		} finally {
			if (showResponse != null) {
				EntityUtils.consume(showEntity);
				showResponse.close();
			}
		}

		// send request for script details
		HttpUriRequest detailsRequest;
		try {
			detailsRequest = RequestBuilder.put()
					.setUri(new URI(SCRIPT_OUTPUT))
					.addParameter("ParameterTextBox", param)
					.addParameter("__EVENTARGUMENT", "")
					.addParameter("__VIEWSTATE", INITIAL_VIEWSTATE)
					.addParameter("__EVENTTARGET", "ScriptDetailLinkButton")
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		getHttpClient().execute(detailsRequest).close();

		// build request for script details content
		HttpUriRequest detailContentRequest;
		try {
			detailContentRequest = RequestBuilder
					.get()
					.setUri(new URI(SCRIPT_URL_BASE
							+ param.replaceAll("\\|", "%7C"))).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		// try to execute the request
		CloseableHttpResponse scriptResponse = null;
		HttpEntity scriptEntity = null;
		boolean timeout = true;
		while (timeout) {
			try {
				scriptResponse = getHttpClient().execute(detailContentRequest);
				scriptEntity = scriptResponse.getEntity();
				ArrayList<OnAirItem> items = parseScript(scriptEntity.getContent());
				timeout = false;
				return items;

			} catch (SocketTimeoutException e) {
				System.out.println("Socket timeout when reading script; "
						+ "retrying");
			} finally {
				if (scriptResponse != null) {
					EntityUtils.consume(scriptEntity);
					scriptResponse.close();
				}
			}
		}
		return null;
	}

	public static ArrayList<OnAirItem> parseScript(InputStream in)
			throws IOException {

		ArrayList<OnAirItem> items = new ArrayList<OnAirItem>();

		// read page
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String ln;
			while ((ln = reader.readLine()) != null) {

				if (ln.contains(ITEM_BLOCK_IDENTIFIER)) { // IT BEGINS

					OnAirItem newItem = new OnAirItem();
					while (!(ln = reader.readLine())
							.contains(ITEM_BLOCK_TERMINATOR)) {

						// item number
						if (ln.contains(ITEM_NUMBER_IDENTIFIER)) {
							String itemNumber = PStringUtils.substring(
									ln,
									ITEM_NUMBER_IDENTIFIER,
									ITEM_NUMBER_TERMINATOR);
							newItem.setNumber(itemNumber);
						}

						// start time and hour
						if (ln.contains(START_TIME_IDENTIFIER)) {
							String hour = PStringUtils.substring(ln,
									START_TIME_IDENTIFIER,
									HOUR_TERMINATOR);
							newItem.setHour(hour + ":00");
							String time = PStringUtils.substring(ln,
									START_TIME_IDENTIFIER,
									START_TIME_TERMINATOR);
							newItem.setTime(time);
						}

						// duration
						if (ln.contains(DURATION_IDENTIFIER)) {
							String duration = PStringUtils.substring(
									ln,
									DURATION_IDENTIFIER,
									DURATION_TERMINATOR);
							newItem.setDuration(duration);
						}
					}

					boolean added = false;
					if (newItem.isComplete()
							&& !items.contains(newItem)) {
						for (int x = 0; x < items.size(); x++) {
							if (newItem.replaces(items.get(x))) {
								items.get(x).setHour(newItem.getHour());
								added = true;
								break;
							}
						}

						if (!added)
							items.add(newItem);
					}
				}

			}

		} finally {
			// clean up
			if (reader != null)
				reader.close();
		}

		return items;
	}

	public static void cleanUp() throws IOException {
		if (client != null) {
			client.close();
			client = null;
		}
	}

}
