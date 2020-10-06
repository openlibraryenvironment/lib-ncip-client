package org.olf.rs.circ.client.mockncipserver;


import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.NCIP2WMSClient;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;



public class NcipWms {
	
	String baseNcipEndpoint = "http://localhost:8890/ncip";
	String lookupUserBaseEndpoint = "http://localhost:8890/api";
	//String itemId = UUID.randomUUID().toString();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void lookupBlockedUser() throws Exception {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserBlocked");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		LookupUser lookupUser = new LookupUser()
				.setApplicationProfileType("thisIsATest")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.setUserId("5551000");
		JSONObject response = ncipWmsClient.send(lookupUser);			
		JSONArray privileges = response.getJSONArray("privileges");
		assertEquals(getValueByKey(privileges, "status"), "BLOCKED");
	}
	
	@Test
	public void lookupActiveUser() throws Exception {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/wmsLookupUser" );
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		LookupUser lookupUser = new LookupUser()
				.setApplicationProfileType("thisIsATest")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.setUserId("5551001");
		JSONObject response = ncipWmsClient.send(lookupUser);			
		JSONArray privileges = response.getJSONArray("privileges");
		assertEquals(getValueByKey(privileges, "status"), "OK");
		
	}
	
	
	@Test
	public void acceptItemMissingRequestId() throws Exception {
		NCIP2WMSClient ncipTwoClient = new NCIP2WMSClient(baseNcipEndpoint);
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setUserId("9999999");
		acceptItem.setTitle("ABC Book");
		acceptItem.setToAgency("ABC");
		acceptItem.setFromAgency("DEF");
		acceptItem.setItemId("123456");
		acceptItem.setRequestActionType("hold for pickup");
		JSONObject response = ncipTwoClient.send(acceptItem);		
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
	public void checkOutThenIn() throws Exception {
		String itemId = "39151000209805";
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(baseNcipEndpoint);
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");		
		CheckoutItem checkoutItem = new CheckoutItem()
				.setUserId("5551002")
				.setFromAgency("ABC")
				.setToAgency("DEF")
				.setItemId(itemId);
		JSONObject response = ncipWmsClient.send(checkoutItem);		
		assertEquals(response.getString("itemId").trim(),itemId);
		CheckinItem checkinItem = new CheckinItem()
				.setItemId(itemId)
				.setFromAgency("ABC")
				.setToAgency("DEF");
		response = ncipWmsClient.send(checkinItem);		
		assertEquals(response.getString("itemId").trim(),itemId);
	}
	
	@Test
	public void missingEndpoint() throws Exception {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient();
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("N00206454")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.includeNameInformation()
				.setToAgency("TNS")
				.setFromAgency("RSH");		
		JSONObject response = ncipWmsClient.send(lookupUser);			
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("Missing"));
	}
	
	
	@Test
	public void testPatronNotFound() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserNotFound");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("not-a-real-patron");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		String element = problems.getJSONObject(0).getString("element");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(errorType.contains("Unknown User"));
		assertTrue(element.contains("User"));
		assertTrue(details.contains("did not return a matching patron"));
	}
	
	@Test
	public void testBlockedPatron() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserBlocked");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("bobby_blocker");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertEquals(response.getString("firstName").trim(),"Joe");
		String status = getValueByKey(response.getJSONArray("privileges"),"STATUS");
		assertEquals(status,"BLOCKED");
		assertEquals(response.get("userId"),"joe_blocker");
	}
	
	@Test
	public void testExpiredPatron() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserExpired");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("expired");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertEquals(response.getString("firstName").trim(),"Jane");
		String status = getValueByKey(response.getJSONArray("privileges"),"STATUS");
		assertEquals(status,"BLOCKED");
		assertEquals(response.get("userId"),"doe_expired");
	}
	
	@Test
	public void testMissingPermissions() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserMissingPerms");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("n-a");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(details.contains("sufficient permissions"));
	}
	
	@Test
	public void testUnauthorized() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserUnauthorized");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("n-a");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(details.contains("Missing or invalid authorization token"));
	}
	
	@Test
	public void testUnsupportedMedia() throws ServiceException, ValidationException, IOException, ToolkitException {
		NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(lookupUserBaseEndpoint + "/ncipwms/lookupUserUnsupportedMedia");
		ncipWmsClient.setApiKey("anapikey");
		ncipWmsClient.setApiSecret("anApiSecret");
		ncipWmsClient.setOAuthEndpointOveride(lookupUserBaseEndpoint + "/ncipwms/mockAuthenicate");
		LookupUser lookupUser = new LookupUser()
				.setUserId("n-a");
		JSONObject response = ncipWmsClient.send(lookupUser);		
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(details.contains("Unsupported media type"));
	}
	
	
	
	
	

	
	public String getValueByKey(JSONArray jsonArray,String desiredKey) {
		Iterator<Object> iterator = jsonArray.iterator();
		while(iterator.hasNext()){
		    JSONObject jsonObject = (JSONObject) iterator.next();
		    if (jsonObject.getString("key").equalsIgnoreCase("status"))  return jsonObject.getString("value");
		}
		return null;
	}
	



}
