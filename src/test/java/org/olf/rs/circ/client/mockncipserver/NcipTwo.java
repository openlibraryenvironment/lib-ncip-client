package org.olf.rs.circ.client.mockncipserver;


import java.util.Iterator;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP2Client;
import static org.junit.Assert.*;



public class NcipTwo {
	
	String baseNcipEndpoint = "http://localhost:8889/ncip";
	String itemId = UUID.randomUUID().toString();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void lookupBlockedUser() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
		LookupUser lookupUser = new LookupUser()
				.setApplicationProfileType("thisIsATest")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.setUserId("5551000");
		JSONObject response = ncipTwoClient.send(lookupUser);			
		JSONArray privileges = response.getJSONArray("privileges");
		assertEquals(getValueByKey(privileges, "status"), "BLOCKED");
	}
	
	@Test
	public void lookupActiveUser() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
		LookupUser lookupUser = new LookupUser()
				.setApplicationProfileType("thisIsATest")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.setUserId("5551001");
		JSONObject response = ncipTwoClient.send(lookupUser);			
		JSONArray privileges = response.getJSONArray("privileges");
		assertEquals(getValueByKey(privileges, "status"), "OK");
		
	}
	
	
	@Test
	public void acceptItemMissingRequestId() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
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
		assertTrue(errorType.contains("Missing element"));
		assertTrue(element.contains("Request ID"));
	}
	
	@Test
	public void acceptItemInvalidPatronId() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setUserId("9999999");
		acceptItem.setTitle("ABC Book");
		acceptItem.setToAgency("ABC");
		acceptItem.setFromAgency("DEF");
		acceptItem.setItemId("123456");
		acceptItem.setRequestActionType("hold for pickup");
		acceptItem.setRequestId("TST-123456");
		acceptItem.setPickupLocation("DELIVERY");
		JSONObject response = ncipTwoClient.send(acceptItem);		
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String details = problems.getJSONObject(0).getString("detail");
		assertTrue(details.contains("does not exist"));
	}
	
	@Test
	public void acceptItemSuccess() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setUserId("5551002");
		acceptItem.setTitle("ABC Book");
		acceptItem.setToAgency("ABC");
		acceptItem.setFromAgency("DEF");
		acceptItem.setItemId(itemId);
		acceptItem.setRequestActionType("hold for pickup");
		acceptItem.setRequestId("TST-123456");
		acceptItem.setPickupLocation("DELIVERY");
		JSONObject response = ncipTwoClient.send(acceptItem);		
		assertEquals(response.getString("itemId").trim(),itemId);
	}
	
	@Test
	public void checkOutThenIn() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client(baseNcipEndpoint);
		//CREATE AN ITEM TO CHECKOUT/CHECK IN
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setUserId("5551002");
		acceptItem.setTitle("ABC Book");
		acceptItem.setToAgency("ABC");
		acceptItem.setFromAgency("DEF");
		acceptItem.setItemId(itemId);
		acceptItem.setRequestActionType("hold for pickup");
		acceptItem.setRequestId("TST-123456");
		acceptItem.setPickupLocation("DELIVERY");
		ncipTwoClient.send(acceptItem);		
		CheckoutItem checkoutItem = new CheckoutItem()
				.setUserId("5551002")
				.setFromAgency("ABC")
				.setToAgency("DEF")
				.setItemId(itemId);
		JSONObject response = ncipTwoClient.send(checkoutItem);		
		assertEquals(response.getString("itemId").trim(),itemId);
		CheckinItem checkinItem = new CheckinItem()
				.setItemId(itemId)
				.setFromAgency("ABC")
				.setToAgency("DEF");
		response = ncipTwoClient.send(checkinItem);		
		assertEquals(response.getString("itemId").trim(),itemId);
	}
	
	@Test
	public void missingEndpoint() throws Exception {
		NCIP2Client ncipTwoClient = new NCIP2Client();
		LookupUser lookupUser = new LookupUser()
				.setUserId("N00206454")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.includeNameInformation()
				.setToAgency("TNS")
				.setFromAgency("RSH");		
		JSONObject response = ncipTwoClient.send(lookupUser);			
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("Missing"));

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
