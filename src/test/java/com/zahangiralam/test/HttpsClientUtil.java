package com.zahangiralam.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class HttpsClientUtil {
	private static Logger logger = Logger.getLogger(HttpsClientUtil.class.getName());
	public static final String CONTENT_TYPE_TXT = "Content-Type";
	public static final String HTTP_POST_METHOD = "POST";
	public static final String HTTP_GET_METHOD = "GET";
	
	//private static CloseableHttpClient httpClient;
	private static boolean isContextValid = false;
	static{
		
		try{
	    	SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	        SSLContext.setDefault(ctx);	        
	        isContextValid = true;
		}catch(Exception e){
			logger.log(Level.SEVERE, "setting TrustManager", e);
		}

		//httpClient = HttpClients.createDefault();
	}
	
	/*
	public static String post(String url, HttpDataContentType contentType, String data){
		logger.info("post(): url = " + url + ";contentType=" + contentType);
		if(!isContextValid) throw new RuntimeException("SSLContext is not configured correctly");
		if(null == url || url.trim().length() == 0) throw new IllegalArgumentException("given url is invalid (either null or empty)");
		if(null == data || data.isEmpty()) throw new IllegalArgumentException("given data is invalid (either null or empty)");
		
		HttpPost httpPost = new HttpPost(url);
		
		//setting HTTP Headers
		Map<String, String> headerParams = new HashMap<String, String>();
		headerParams.put(CONTENT_TYPE_TXT, contentType.getValue());
		httpPost.setHeaders(getHeaders(headerParams));
		
		StringEntity params = null;
		try {
			params = new StringEntity(data);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "setting string formatted data into " + url, e);
		}
		httpPost.setEntity(params);
		 		
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (IOException e) {
			logger.log(Level.SEVERE, url, e);
			throw new RuntimeException(e);
		}
		
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = response.getStatusLine();
		logger.log(Level.INFO, url, "response status = " + statusLine.getStatusCode());
		
		String content = null;
		if(statusLine.getStatusCode() == 200 
				|| statusLine.getStatusCode() == 302){
			try {
				content = getContent(entity);
			} catch (IllegalStateException | IOException e) {
				logger.log(Level.SEVERE, "read content of the response of " + url, e);
			}
		}
				
		try {
			EntityUtils.consume(entity);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "read content of the response of " + url, e);
			throw new RuntimeException(e);
		}
		
		return content;
	}
	*/
	
	
	public static String post(String urlTxt, HttpDataContentType contentType, String data){
		if(!isContextValid) throw new RuntimeException("SSLContext is not set properly");
		
		logger.info("post(urlTxt,contentType,data): url = " + urlTxt + ";contentType=" + contentType.getValue() + ";Data = " + data);
		if(null == urlTxt || urlTxt.trim().length() == 0) throw new IllegalArgumentException("given url is invalid (either null or empty)");
		if(null == data || data.isEmpty()) throw new IllegalArgumentException("given data is invalid (either null or empty)");

		String inputLine, response = "";
		URL url=null;
		try {
			url = new URL(urlTxt);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "invalid url " + url, e);
			throw new IllegalArgumentException(e);
		}
		
		HttpsURLConnection request=null;
		try {
			request = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to open connection with url " + url, e);
			throw new IllegalArgumentException(e);
		}
		
		request.setHostnameVerifier(new HostnameVerifier() {
	            @Override
	            public boolean verify(String arg0, SSLSession arg1) {
	                return true;
	            }
	    });

		request.setUseCaches(false);
		request.setDoOutput(true);
		request.setDoInput(true);
		
		//setting HTTP headers
		request.setRequestProperty(CONTENT_TYPE_TXT, contentType.getValue());

		StringEntity params = null;
		try {
			params = new StringEntity(data);
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "data has unsupported encoding", e);
			throw new IllegalArgumentException(e);
		}

		try {
			request.setRequestMethod(HTTP_POST_METHOD);
		} catch (ProtocolException e) {
			logger.log(Level.SEVERE, "failed to set HTTP POST method for " + url, e);
			throw new RuntimeException(e);
		}
		
		OutputStream post = null;
		try {
			post = request.getOutputStream();
			params.writeTo(post);
			post.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to get output steam or writing data into HttpsURLConnection", e);
			closeOutputStream(post);
			throw new RuntimeException(e);
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				response += inputLine + "\n";
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to read input steam on HttpsURLConnection", e);
			//closing the input stream as we are throwing exception
			closeOutputStream(post);			
			closeBufferedReader(in);
			throw new RuntimeException(e);

		}		 
		
		closeOutputStream(post);			
		closeBufferedReader(in);
					
		return response;
	}
	
	private static void closeOutputStream(OutputStream outputStream){
		if(outputStream == null) return;
		try {
			outputStream.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "closeOutputStream(OutputStream):failed to close OutputStream on HttpsURLConnection", e);
		}					
	}

	private static void closeBufferedReader(BufferedReader reader){
		if(reader == null) return;
		try {
			reader.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "closeBufferedReader(BufferedReader):failed to close BufferedReader on HttpsURLConnection", e);
		}					
	}

	private static String getContent(HttpEntity entity) throws IllegalStateException, IOException {
		StringBuffer rslt = new StringBuffer();
		if(null == entity) return rslt.toString();
		InputStream instream = entity.getContent();
		byte[] buf = new byte[2048];
		while((instream.read(buf)) != -1){
			rslt.append(new String(buf));
		}
		return rslt.toString();
	}

	public static String get(String urlTxt){
		if(!isContextValid) throw new RuntimeException("SSLContext is not set properly");
		
		logger.info("get(urlTxt): url = " + urlTxt);
		if(null == urlTxt || urlTxt.trim().length() == 0) throw new IllegalArgumentException("given url is invalid (either null or empty)");

		String inputLine, response = "";
		URL url=null;
		try {
			url = new URL(urlTxt);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "invalid url " + url, e);
			throw new IllegalArgumentException(e);
		}
		
		HttpsURLConnection request=null;
		try {
			request = (HttpsURLConnection) url.openConnection();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to open connection with url " + url, e);
			throw new IllegalArgumentException(e);
		}
		
		request.setHostnameVerifier(new HostnameVerifier() {
	            @Override
	            public boolean verify(String arg0, SSLSession arg1) {
	                return true;
	            }
	    });

		request.setUseCaches(false);
		request.setDoOutput(true);
		request.setDoInput(true);
		
		//setting HTTP headers
		//request.setRequestProperty(CONTENT_TYPE_TXT, contentType.getValue());

		try {
			request.setRequestMethod(HTTP_GET_METHOD);
		} catch (ProtocolException e) {
			logger.log(Level.SEVERE, "failed to set HTTP GET method for " + url, e);
			throw new RuntimeException(e);
		}
		
		OutputStream post = null;
		try {
			post = request.getOutputStream();
			post.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to get output steam into HttpsURLConnection", e);
			closeOutputStream(post);
			throw new RuntimeException(e);
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			while ((inputLine = in.readLine()) != null) {
				response += inputLine + "\n";
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "failed to read input steam on HttpsURLConnection", e);
			//closing the input stream as we are throwing exception
			closeOutputStream(post);			
			closeBufferedReader(in);
			throw new RuntimeException(e);

		}		 
		
		closeOutputStream(post);			
		closeBufferedReader(in);
					
		return response;
	}
	
	private static Header [] getHeaders(Map<String, String> headerParams){
		List<Header> headers = new ArrayList<>();
		if(null == headerParams || headerParams.isEmpty()) return headers.toArray(new Header[headers.size()]);
		for(String key:headerParams.keySet()){
			headers.add(new BasicHeader(key, headerParams.get(key)));
		}
		return headers.toArray(new Header[headers.size()]);
	}
	
}
