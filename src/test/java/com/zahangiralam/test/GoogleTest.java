package com.zahangiralam.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GoogleTest {
	
	@BeforeClass
	public static void setup(){
	}
	
	@Test
	public void searchMyName(){
		String url = "http://www.google.com/search?q=httpClient";
		String response = HttpClientUtil.get(url);
		System.out.println("response = " + response);
		if(response != null && response.contains("Search2222")){
			//do nothing
		}else{
			Assert.fail("response doesn't contain Search");
		}
	}
}
