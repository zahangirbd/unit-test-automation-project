package com.zahangiralam.test;

public enum HttpDataContentType {
	APPLICATION_JSON("application/json"),
	APPLICATION_XML("application/xml");
		
	private String value;
	
	private HttpDataContentType(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
}
