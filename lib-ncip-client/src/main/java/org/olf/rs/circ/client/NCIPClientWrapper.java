package org.olf.rs.circ.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NCIPClientWrapper {

	public static final String NCIP1 = "NCIP1";
	public static final String NCIP2 = "NCIP2";
	public static final String NCIP1_SOCKET = "NCIP1_SOCKET";
	public static final String NCIP1_STRICT_SOCKET = "NCIP1_STRICT_SOCKET";
	public static final String WMS = "WMS";
	public static final Logger logger = Logger.getLogger(NCIPClientWrapper.class);

	public CirculationClient circulationClient = null;


	/*
	 * @param endpoint endpoint for the NCIP server (or IDM server for WMS LookupUser)
	 * @param protocol specifics the type of NCIP server (e.g. NCIP1, NCIP1_SOCKET, NCIP2, WMS)
	 */
	public NCIPClientWrapper(String endpoint, Map<String,Object> inputParms) throws NCIPClientException {
		CaseInsensitiveMap<String,Object> inputMap = new CaseInsensitiveMap<String,Object>();
		inputMap.putAll(inputParms);
		String protocol = (String) inputMap.get("protocol");
		//INSTANTIATE CLIENT
		if (protocol == null) {
			throw new NCIPClientException("Protocol must be NCIP1, NCIP2, NCIP1_SOCKET, NCIP1_STRICT_SOCKET or WMS");
		}
		if (protocol.equalsIgnoreCase(NCIP1)) {
			this.circulationClient = new NCIP1Client(endpoint,inputParms);
		}
		else if (protocol.equalsIgnoreCase(NCIP1_SOCKET)) {
			inputParms.put("useSocket", true);
			this.circulationClient = new NCIP1Client(endpoint,inputParms);
		}
		else if (protocol.equalsIgnoreCase(NCIP1_STRICT_SOCKET)) {
			inputParms.put("useSocket", true);
			inputParms.put("strictSocket", true);
			this.circulationClient = new NCIP1Client(endpoint,inputParms);
		}
		else if (protocol.equalsIgnoreCase(NCIP2)) {
			this.circulationClient = new NCIP2Client(endpoint,inputParms);
		}
		else if (protocol.equalsIgnoreCase(WMS)) {
			this.circulationClient = new NCIP2WMSClient(endpoint,inputParms);
		}
		else {
			throw new NCIPClientException("Protocol must be NCIP1, NCIP2, NCIP1_SOCKET or WMS");
		}
	}
	
	public String printRequest(NCIPCircTransaction transaction) {
		try {
			return this.circulationClient.printRequest(transaction);
		}
		catch(Exception e) {
			logger.fatal("NCIP2ClientWrapper print request failed");
			JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2Client print request call failed.");
			return r.toString();
		}
	
	}
	
	
	/*
	 * @param transaction transaction represents the NCIP call (LookupUser, CheckinItem, CheckoutItem, AcceptItem)
	 */
	public Map<String, Object> send(NCIPCircTransaction transaction) {
		try {
			JSONObject jsonObject = this.circulationClient.send(transaction);
			Map<String, Object> responseAsMap = toMap(jsonObject);
			if (responseAsMap == null || responseAsMap.isEmpty()) {
				responseAsMap.put("success", false);
			}
			else if (responseAsMap.containsKey("problems")) {
				responseAsMap.put("success", false);
			}
			else {
				responseAsMap.put("success", true);
			}
			return responseAsMap;
		}
		catch(Exception e) {
			logger.fatal(e.getLocalizedMessage());
			logger.fatal(e.getMessage());
			logger.fatal(e.toString());
			JSONObject errorDetails = constructException(e.getLocalizedMessage(), e.getMessage(), e.toString());
			Map<String, Object> responseAsMap = toMap(errorDetails);
			responseAsMap.put("success", false);
			return responseAsMap;
		}

	}


	public Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (key.equalsIgnoreCase("problems")) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONArray) {
				Map<String, Object> privMap = new HashMap<String, Object>();
				Iterator i = ((JSONArray) value).iterator();
				while (i.hasNext()) {
					JSONObject priv = (JSONObject) i.next();
					privMap.put(priv.optString("key",""), priv.optString("value",""));
				}
				value = privMap;
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}


	public List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}


	private JSONObject constructException(String localizedMessage ,String message ,String toStringValue) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject problem = new JSONObject();
		problem.put("type",localizedMessage);
		problem.put("detail", toStringValue);
		problem.put("element", message);
		array.put(problem);
		returnJson.put("problems", array);
		return returnJson;
	}


}
