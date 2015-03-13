package com.zahangiralam.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


public class HttpClientUtil {
	private static Logger logger = Logger.getLogger(HttpClientUtil.class.getName());
	private static final String USER_AGENT = "Mozilla/5.0";
	
	public static String get(String url){
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
	 
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			logger.log(Level.SEVERE, "due to invalid protocol, failed to execute request for " + url, e);
			throw new RuntimeException(e);

		} catch (IOException e) {
			logger.log(Level.SEVERE, "due to IO problem, failed to execute request for " + url, e);
			throw new RuntimeException(e);
		}
	 
		System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode());
	
		StringBuffer result = new StringBuffer();
		BufferedReader rd;
		try {
			rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}		
		} catch (IllegalStateException | IOException e) {
			logger.log(Level.SEVERE, "due to Illegal state or IO problem, failed to read response for " + url, e);
			throw new RuntimeException(e);
		}		
		return result.toString();
	}

}
