package mirador.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
public class HttpConnector {
 
  private String cookies;
  static private HttpClient client = HttpClientBuilder.create().build();
  //HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
  
  
  //private DefaultHttpClient client = new DefaultHttpClient();
  //client.setRedirectStrategy(new LaxRedirectStrategy());

  private final String USER_AGENT = "Mozilla/5.0";
 
  
  
  public HttpConnector(){
	  
  }
  

	public static void upload(String username, String password, String url,String db, String var1, String var2, String ranges, String historystring) throws ConnectException, Exception{
		CookieHandler.setDefault(new CookieManager());
		 
		HttpConnector http = new HttpConnector();
		
	 
		String result = http.GetPageContent(url);
		System.out.println(result);
	 
		System.out.println("Got the add submission page. Let's parse.");
		
		client = HttpClientBuilder.create().build();
		
		List<NameValuePair> submissionParams = 
	            http.getFormParams(result,username,password, db, var1, var2, ranges,historystring);
		http.sendPost(url, submissionParams);
		
		
	}

	public static boolean authenticate(String username, String password) throws Exception, ConnectException{

		String url = "http://localhost/classes/access_user/login.php";
		CookieHandler.setDefault(new CookieManager());
		HttpConnector http = new HttpConnector();
		String page = http.GetPageContent(url);
		List<NameValuePair> postParams = 
	               http.getFormParams(page, username, password);
		
		boolean authenticated = http.sendPost(url, postParams);
		
		//System.out.println(authenticated);
		return authenticated;
		
		
	}

 
  private boolean sendPost(String url, List<NameValuePair> postParams) 
        throws Exception, HttpHostConnectException{

 
	HttpPost post = new HttpPost(url);
 
	// add header
	post.setHeader("Host", "localhost");
	post.setHeader("User-Agent", USER_AGENT);
	post.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
	post.setHeader("Cookie", getCookies());
	post.setHeader("Connection", "keep-alive");
	post.setHeader("Referer", "http://localhost/classes/access_user/login.php");
	post.setHeader("Content-Type", "application/x-www-form-urlencoded");
 
	post.setEntity(new UrlEncodedFormEntity(postParams));
 
	HttpResponse response = client.execute(post);
 
	int responseCode = response.getStatusLine().getStatusCode();
	String reasonPhrase = response.getStatusLine().getReasonPhrase();
 
	System.out.println("\nSending 'POST' request to URL : " + url);
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);
	System.out.println("Reason phrase : " + reasonPhrase);
 
	BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
 
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
 
	 System.out.println(result.toString());
	 
	 if (responseCode == 302){
		 return true;
	 }
	 else{
		 return false;
	 }
 
	 
  }
 
  private String GetPageContent(String url) throws Exception {
 
	HttpGet request = new HttpGet(url);
 
	request.setHeader("User-Agent", USER_AGENT);
	request.setHeader("Accept",
		"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	request.setHeader("Accept-Language", "en-US,en;q=0.5");
 
	HttpResponse response = client.execute(request);
	int responseCode = response.getStatusLine().getStatusCode();
 
	System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);
 
	BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));
 
	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
 
	// set cookies
	setCookies(response.getFirstHeader("Set-Cookie") == null ? "" : 
                     response.getFirstHeader("Set-Cookie").toString());
 
	return result.toString();
 
  }
 
  public List<NameValuePair> getFormParams(
             String html, String username, String password)
			throws UnsupportedEncodingException {
 
	System.out.println("Extracting form's data...");
 
	Document doc = Jsoup.parse(html);
 
	// Google form id
	//Element loginform = doc.select("form1").first();
	Element loginform = doc.getElementsByTag("form").first();
	Elements inputElements = loginform.getElementsByTag("input");
 
	List<NameValuePair> paramList = new ArrayList<NameValuePair>();
 
	for (Element inputElement : inputElements) {
		String key = inputElement.attr("name");
		String value = inputElement.attr("value");
 
		if (key.equals("login"))
			value = username;
		else if (key.equals("password"))
			value = password;
 
		paramList.add(new BasicNameValuePair(key, value));
 
	}
 
	return paramList;
  }
  
  public List<NameValuePair> getFormParams(
          String html, String username, String password, String db, String var1, String var2, String ranges,String history)
			throws UnsupportedEncodingException {

	System.out.println("Extracting form's data...");

	Document doc = Jsoup.parse(html);

	// Google form id
	//Element loginform = doc.select("form1").first();
	Element loginform = doc.getElementsByTag("form").first();
	Elements inputElements = loginform.getElementsByTag("input");

	List<NameValuePair> paramList = new ArrayList<NameValuePair>();

	for (Element inputElement : inputElements) {
		String key = inputElement.attr("name");
		String value = inputElement.attr("value");

		if (key.equals("login"))
			value = username;
		else if (key.equals("password"))
			value = password;
		else if (key.equals("db"))
			value = db;
		else if (key.equals("var1"))
			value = var1;
		else if (key.equals("var2"))
			value = var2;
		else if (key.equals("ranges"))
			value = ranges;
		else if (key.equals("history"))
		  value = history;

		paramList.add(new BasicNameValuePair(key, value));

	}

	return paramList;
}
 
  public String getCookies() {
	return cookies;
  }
 
  public void setCookies(String cookies) {
	this.cookies = cookies;
  }
 
}