package org.olf.rs.circ.client.unitTests.clients;


import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.NCIP2Client;
import static org.junit.Assert.*;



public class NCIP1ClientTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testSetEndpoint() throws Exception {
		NCIP2Client ncipClient = new NCIP2Client();
		ncipClient.setEndpoint("https://google.com");
		assertEquals(ncipClient.getEndpoint(),"https://google.com");
	}
	

	
	


}
