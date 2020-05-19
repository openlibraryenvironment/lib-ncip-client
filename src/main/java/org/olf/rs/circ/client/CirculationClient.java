package org.olf.rs.circ.client;

import java.io.IOException;
import java.net.URISyntaxException;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONObject;

public interface CirculationClient {
	
	void setEndpoint(String endpoint);
	JSONObject send(NCIPCircTransaction service) throws ServiceException, ValidationException, IOException, URISyntaxException, Exception;

}
