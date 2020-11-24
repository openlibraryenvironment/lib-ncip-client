package org.olf.rs.circ.client.unitTests.clients;


import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.NCIPClientException;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;



public class NCIP1ClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testSetEndpoint() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		NCIP1Client ncipClient = new NCIP1Client("https://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("endpoint");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(value,"https://google.com");
	}
	
	@Test
	public void setUseSocket() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("useSocket", true);
		NCIP1Client ncipClient = new NCIP1Client("https://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("useSocket");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(((Boolean)value).booleanValue(),true);
	}
	
	@Test
	public void testCutomSocket() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("socketTimeout", 888);
		NCIP1Client ncipClient = new NCIP1Client("https://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("socketTimeout");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(value,888);
	}
	
	@Test(expected=NCIPClientException.class) 
	public void testSetSocketAsString() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("socketTimeout", "888");
		NCIP1Client ncipClient = new NCIP1Client("https://google.com",inputParms);
		Field field = ncipClient.getClass().getDeclaredField("socketTimeout");
		field.setAccessible(true);
		Object value = field.get(ncipClient);
		assertEquals(value,888);
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
