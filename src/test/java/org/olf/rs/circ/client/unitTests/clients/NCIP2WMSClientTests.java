package org.olf.rs.circ.client.unitTests.clients;


import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP2WMSClient;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;



public class NCIP2WMSClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testSetEndpoint() throws Exception {
		NCIP2WMSClient ncipClient = new NCIP2WMSClient();
		ncipClient.setEndpoint("https://google.com");
		assertEquals(ncipClient.getEndpoint(),"https://google.com");
	}
	
	@Test
	public void testAcceptItemNotSupported() throws Exception {
		NCIP2WMSClient ncipClient = new NCIP2WMSClient();
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
	public void testNoEndpoint() throws Exception {
		NCIP2WMSClient ncipClient = new NCIP2WMSClient();
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing endpoint"));
	}
	
	@Test
	public void testNoApiKey() throws Exception {
		NCIP2WMSClient ncipClient = new NCIP2WMSClient();
		ncipClient.setEndpoint("http://google.com");
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing credentials"));
	}
	
	@Test
	public void testNoApiSecret() throws Exception {
		NCIP2WMSClient ncipClient = new NCIP2WMSClient();
		ncipClient.setEndpoint("http://google.com");
		ncipClient.setApiKey("not a real api key");
		LookupUser lookupUser = new LookupUser();
		JSONObject response = ncipClient.send(lookupUser);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("type");
		assertTrue(details.contains("Missing credentials"));
	}
	

	
	


}
