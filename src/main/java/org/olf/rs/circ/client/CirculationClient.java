package org.olf.rs.circ.client;

import java.io.IOException;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONObject;

public interface CirculationClient {
	
	void setEndpoint(String endpoint);
	void setUserid(String userid);
	void setPassword(String password);
	void addHttpHeader(String key, String value); 
	JSONObject send(NCIPCircTransaction service) throws ServiceException, ValidationException, IOException;
	
	
	

}
