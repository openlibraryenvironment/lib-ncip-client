package org.olf.rs.circ.client;

import org.json.JSONObject;

public interface CirculationClient {
	
	JSONObject send(NCIPCircTransaction transaction) throws NCIPClientException;
	String printRequest(NCIPCircTransaction transaction) throws NCIPClientException;

}
