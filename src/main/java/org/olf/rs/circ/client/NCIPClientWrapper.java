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
	private static final Logger logger = Logger.getLogger(NCIPClientWrapper.class);
	private int socketTimeOut = 0;
	
	public CirculationClient circulationClient = null;
	
	
	
	public NCIPClientWrapper(String endpoint,String protocol) throws Exception {
		
		
		//INSTANTIATE CLIENT
		if (protocol.equalsIgnoreCase(NCIP1)) {
			this.circulationClient = new NCIP1Client(endpoint,false);
		}
		else if (protocol.equalsIgnoreCase(NCIP1_SOCKET)) {
			if (this.socketTimeOut == 0)
				this.circulationClient = new NCIP1Client(endpoint,true,socketTimeOut);
			else
				this.circulationClient = new NCIP1Client(endpoint,true);
		}
		else if (protocol.equalsIgnoreCase(NCIP2)) {
			this.circulationClient = new NCIP2Client(endpoint);
		}
		else {
			throw new Exception("Protocol must be NCIP1, NCIP2 OR NCIP1_SOCKET");
		}
		
	}
	
	public void setTimeout(int socketTimeout) {
		this.socketTimeOut = socketTimeout;
	}
	
	public Map<String, Object> send(NCIPCircTransaction transaction) {
		try {
			JSONObject jsonObject = this.circulationClient.send(transaction);
			return toMap(jsonObject);
		}
		catch(Exception e) {
			logger.fatal(e.getLocalizedMessage());
			logger.fatal(e.getMessage());
			logger.fatal(e.toString());
			JSONObject errorDetails = constructException(e.getLocalizedMessage(), e.getMessage(), e.toString());
			return toMap(errorDetails);
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
	        		privMap.put(priv.getString("key"), priv.get("value"));
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
//