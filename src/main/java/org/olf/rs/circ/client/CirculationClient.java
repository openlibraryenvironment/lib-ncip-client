package org.olf.rs.circ.client;

import org.json.JSONObject;

public interface CirculationClient {
	
	JSONObject send(NCIPCircTransaction service) throws NCIPClientException;

}
