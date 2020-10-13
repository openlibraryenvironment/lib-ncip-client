package org.olf.rs.circ.client.unitTests.clients;


import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2WMSClient;
import org.olf.rs.circ.client.NCIPClientException;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;



public class NCIP2WMSClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	

	
	@Test
	public void testAcceptItemNotSupported() throws NCIPClientException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		NCIP2WMSClient ncipClient = new NCIP2WMSClient("endpoint",inputParms);
		AcceptItem acceptItem = new AcceptItem();
		JSONObject response = ncipClient.send(acceptItem);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		String element = problems.getJSONObject(0).getString("element");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(errorType.contains("transaction type"));
		assertTrue(element.contains("AcceptItem"));
		assertTrue(details.contains("does not support"));
	}
	
	@Test
	public void testNoEndpoint() throws NCIPClientException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("apiKey", "not a real api key");
		inputParms.put("apiSecret", "not real");
		NCIP2WMSClient ncipClient = new NCIP2WMSClient(null,inputParms);
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing endpoint"));
	}
	
	@Test
	public void testNoApiKey() throws NCIPClientException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		NCIP2WMSClient ncipClient = new NCIP2WMSClient("http://google.com",inputParms);
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing credentials"));
	}
	
	@Test
	public void testNoApiSecret() throws NCIPClientException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("apiKey", "not a real api key");
		NCIP2WMSClient ncipClient = new NCIP2WMSClient("http://google.com",inputParms);
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing credentials"));
	}
	
	@Test
	public void testSetVars() throws NCIPClientException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		NCIP2WMSClient ncipClient = new NCIP2WMSClient("http://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("endpoint");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(value,"http://google.com");
	}
	@Test 
	public void testCaseInsensitive() throws NCIPClientException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("apikey", "not a real api key");
		NCIP2WMSClient ncipClient = new NCIP2WMSClient("http://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("apiKey");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(value,"not a real api key");
	}
	

	
	


}
