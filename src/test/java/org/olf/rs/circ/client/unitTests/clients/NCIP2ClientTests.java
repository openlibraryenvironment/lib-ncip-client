package org.olf.rs.circ.client.unitTests.clients;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.NCIP2WMSClient;
import org.olf.rs.circ.client.NCIPClientException;

import static org.junit.Assert.*;



public class NCIP2ClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test 
	public void testClientPrint() throws NCIPClientException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		NCIP2Client ncipClient = new NCIP2Client("http://google.com",inputParms);
		CheckoutItem checkoutItem = new CheckoutItem();
		checkoutItem.setApplicationProfileType("my-application-profile");
		checkoutItem.setItemId("5551212");
		checkoutItem.setUserId("8377360");
		String checkoutItemAsString = ncipClient.printRequest(checkoutItem);
		System.out.println(checkoutItemAsString);
		assertTrue(checkoutItemAsString.contains("5551212"));
		checkoutItemAsString = checkoutItem.toString();
		assertTrue(checkoutItemAsString.contains("8377360"));
	}
	

	

}
