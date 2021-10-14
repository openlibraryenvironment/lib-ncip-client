package org.olf.rs.circ.client.mockncipserver;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.hamcrest.CoreMatchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;

import junit.framework.Assert;

import static org.junit.Assert.*;



public class NcipOneWithSocket {
	
	String baseNcipEndpoint = "http://localhost:8890/api";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void lookupUserSuccess() throws Exception  {
			Map<String, Object> inputParms = new HashMap<String,Object>();
			inputParms.put("useSocket", true);
			NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/lookupUser",inputParms);
			LookupUser lookupUser = new LookupUser()
					.setUserId("N00206454")
					.includeUserAddressInformation()
					.includeUserPrivilege()
					.includeNameInformation()
					.includeUserAddressInformation()
					.includeUserPrivilege()
					.setToAgency("TNS")
					.setFromAgency("RSH");		
			JSONObject response = ncipOneClient.send(lookupUser);			
			JSONArray privileges = response.getJSONArray("privileges");
			assertEquals(getValueByKey(privileges, "status"), "OK");
			assertEquals(response.getString("firstName").trim(),"Jane Marie");
			assertEquals(response.getString("userId").trim(),"N5551212");
		
	}
	
	
	@Test
	public void lookUpUserNcipError() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/lookupUserError",inputParms);
		LookupUser lookupUser = new LookupUser()
				.setUserId("N00206454")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.includeNameInformation()
				.setToAgency("TNS")
				.setFromAgency("RSH");		
		JSONObject response = ncipOneClient.send(lookupUser);			
		assertTrue(!response.has("privileges"));
		assertTrue(response.has("problems"));
		
	}
	
	@Test
	public void lookupUserHttpError() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/incorrect",inputParms);
		LookupUser lookupUser = new LookupUser()
				.setUserId("N00206454")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.includeNameInformation()
				.setToAgency("TNS")
				.setFromAgency("RSH");
		JSONObject response = ncipOneClient.send(lookupUser);
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("404"));
	}
	
	@Test
	public void lookupUserTimeout() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/timeout/twomins",inputParms);
		LookupUser lookupUser = new LookupUser()
				.setUserId("N00206454")
				.includeUserAddressInformation()
				.includeUserPrivilege()
				.includeNameInformation()
				.setToAgency("TNS")
				.setFromAgency("RSH");		
		JSONObject response = ncipOneClient.send(lookupUser);			
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("timeout"));
	}
	
	
	@Test
	public void acceptItem() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/acceptitem",inputParms);
		AcceptItem acceptItem = new AcceptItem()
				.setItemId("54321")
				.setRequestId("TST-54321")
				.setUserId("8377360")
				.setAuthor("Jane Doe")
				.setTitle("One Fish Two Fish")
				.setIsbn("5551212980")
				.setPickupLocation("DELIVERY")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.setRequestActionType("Hold for pickup");
		JSONObject response = ncipOneClient.send(acceptItem);
		assertEquals(response.getString("itemId").trim(),"100556043-10");
	}
	
	@Test
	public void acceptItemWithError() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/acceptitemError",inputParms);
		AcceptItem acceptItem = new AcceptItem()
				.setItemId("54321")
				.setRequestId("TST-54321")
				.setUserId("8377360")
				.setAuthor("Jane Doe")
				.setTitle("One Fish Two Fish")
				.setIsbn("5551212980")
				.setPickupLocation("DELIVERY")
				.setToAgency("ABC")
				.setFromAgency("DEF")
				.setRequestActionType("Hold for pickup");
		JSONObject response = ncipOneClient.send(acceptItem);
		assertTrue(response.has("problems"));
	}
	
	@Test
	public void checkoutItem() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/checkoutItem",inputParms);
		CheckoutItem checkoutItem = new CheckoutItem()
				.setUserId("8377630")
				.setItemId("54321")
				.setRequestId("TST-54321")
				.setToAgency("ABC")
				.setFromAgency("DEF");
		JSONObject response = ncipOneClient.send(checkoutItem);
		assertEquals(response.getString("itemId").trim(),"31207052000951");
		
	}
	
	@Test
	public void checkoutItemWithError() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/checkoutItemError",inputParms);
		CheckoutItem checkoutItem = new CheckoutItem()
				.setUserId("8377630")
				.setItemId("54321")
				.setRequestId("TST-54321")
				.setToAgency("ABC")
				.setFromAgency("DEF");
		JSONObject response = ncipOneClient.send(checkoutItem);
		assertTrue(response.has("problems"));
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("Unknown Item"));
	}
	
	@Test
	public void checkinItem() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/checkinItem",inputParms);
		CheckinItem checkinItem = new CheckinItem();
		checkinItem.setFromAgency("ABC");
		checkinItem.setToAgency("DEF");
		checkinItem.setItemId("54321");
		JSONObject response = ncipOneClient.send(checkinItem);
		assertEquals(response.getString("itemId").trim(),"TNS-11423128");
	}
	
	@Test
	public void checkinItemWithError() throws Exception {
		Map<String, Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/ncipone/checkinItemError",inputParms);
		CheckinItem checkinItem = new CheckinItem();
		checkinItem.setFromAgency("ABC");
		checkinItem.setToAgency("DEF");
		checkinItem.setItemId("54321");
		JSONObject response = ncipOneClient.send(checkinItem);
		JSONArray problems = response.getJSONArray("problems");
		String errorType = problems.getJSONObject(0).getString("type");
		assertTrue(errorType.contains("Item Not Checked Out"));
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
