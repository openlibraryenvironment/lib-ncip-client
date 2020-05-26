package org.olf.rs.circ.client.unitTests.clients;


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



public class NCIP2ClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testAddHeader() throws Exception {
		NCIP2Client ncipClient = new NCIP2Client();
		ncipClient.setEndpoint("https://google.com");
		ncipClient.addHttpHeader("Accept", "application/xml");
		assertEquals(ncipClient.getHttpHeader().get("Accept"),"application/xml");
		
		
	}
	
	@Test 
	public void testToolkitUtil() throws Exception {
		NCIP2Client ncipClient = new NCIP2Client();
		String translatorClass = ncipClient.getXcToolkitUtil().translator.getClass().getName();
		assertTrue(translatorClass.contains("JAXBDozerTranslator"));
		
	}
}
