package org.olf.rs.circ.client;

public interface CirculationClient {
	
	void setEndpoint(String endpoint);
	void setUserid(String userid);
	void setPassword(String password);
	void addHttpHeader(String key, String value); 
	
	
	

}
