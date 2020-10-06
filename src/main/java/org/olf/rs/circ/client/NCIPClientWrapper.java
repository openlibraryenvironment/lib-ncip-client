package org.olf.rs.circ.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NCIPClientWrapper {
	
	public static final String NCIP1 = "NCIP1";
	public static final String NCIP2 = "NCIP2";
	public static final String NCIP1_SOCKET = "NCIP1_SOCKET";
	public static final String WMS = "WMS";
	private static final Logger logger = Logger.getLogger(NCIPClientWrapper.class);
	private String apiKey;
	private String apiSecret;
	private String idmEndpoint;

	public String getIdmEndpoint() {
		return idmEndpoint;
	}
	public void setIdmEndpoint(String idmEndpoint) {
		this.idmEndpoint = idmEndpoint;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getApiSecret() {
		return apiSecret;
	}
	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public CirculationClient circulationClient = null;

	/*
	 * @param endpoint endpoint for the NCIP server 
	 * @param protocol specifics the type of NCIP server (e.g. NCIP1, NCIP1_SOCKET, NCIP2, WMS)
	 * @param apiKey apiKey for NCIP (or IDM server) authentication
	 * @param apiSecret apiSecret for NCIP (or IDM server) authentication
	 */
	public NCIPClientWrapper(String endpoint, String protocol, String apiKey, String apiSecret) throws Exception {
		this(endpoint,protocol);
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}


	/*
	 * @param endpoint endpoint for the NCIP server (or IDM server for WMS LookupUser)
	 * @param protocol specifics the type of NCIP server (e.g. NCIP1, NCIP1_SOCKET, NCIP2, WMS)
	 */
	public NCIPClientWrapper(String endpoint,String protocol) throws Exception {
		
		
		//INSTANTIATE CLIENT
		if (protocol.equalsIgnoreCase(NCIP1)) {
			this.circulationClient = new NCIP1Client(endpoint,false);
		}
		else if (protocol.equalsIgnoreCase(NCIP1_SOCKET)) {
			this.circulationClient = new NCIP1Client(endpoint,true);
		}
		else if (protocol.equalsIgnoreCase(NCIP2)) {
			this.circulationClient = new NCIP2Client(endpoint);
		}
		else if (protocol.equalsIgnoreCase(WMS)) {
			this.circulationClient = new NCIP2WMSClient(endpoint,this.apiKey,this.apiSecret,this.idmEndpoint);
		}
		else {
			throw new Exception("Protocol must be NCIP1, NCIP2, NCIP1_SOCKET or WMS");
		}
		
	}
	/**
	 * Override the default socketTimeout
	 */
	public void setTimeout(int socketTimeout) throws Exception {
		try {
			((NCIP1Client)this.circulationClient).setSocketTimeout(socketTimeout);
		}
		catch(Exception e) {
			throw new Exception("Protocol must be NCIP1 to set socketTimeout Value");
		}
	}
	
	public Map<String, Object> send(NCIPCircTransaction transaction) {
		try {

			//IF THIS IS A WMS CLIENT - SET THE USERID AND PASSWORD
			try {
				((NCIP2WMSClient)this.circulationClient).setApiKey(apiKey);
				((NCIP2WMSClient)this.circulationClient).setApiSecret(apiSecret);
			}
			catch(Exception e) {
				//FINE IF THIS FAILS, THE OTHER CLIENTS
				//DO NOT NEED THESE VALUES SET
			}

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
	
	
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
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
	
	
	public static List<Object> toList(JSONArray array) throws JSONException {
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
	
	
	public JSONObject constructException(String localizedMessage ,String message ,String toStringValue) {
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
