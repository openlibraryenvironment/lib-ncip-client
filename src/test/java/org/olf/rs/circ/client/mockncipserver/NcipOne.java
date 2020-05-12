package org.olf.rs.circ.client.mockncipserver;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.hamcrest.CoreMatchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;

import junit.framework.Assert;

import static org.junit.Assert.*;



public class NcipOne {
	
	String baseNcipEndpoint = "http://localhost:8889/api/ncipone";
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void lookupUserSuccess() throws Exception  {
			NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/lookupUser",false);
			LookupUser lookupUser = new LookupUser()
					.setUserId("N00206454")
					.includeUserAddressInformation()
					.includeUserPrivilege()
					.includeNameInformation()
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
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/lookupUserError",false);
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
		NCIP1Client ncipOneClient = new NCIP1Client(baseNcipEndpoint + "/incorrect",false);
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
	public void missingEndpoint() throws Exception {
		NCIP1Client ncipOneClient = new NCIP1Client();
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
	
	
	
	//TODO
	//test timeout - no response received



}
