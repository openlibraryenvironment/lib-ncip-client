package org.olf.rs.circ.client.unitTests.clients;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.NCIPClientWrapper;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;



public class NCIPClientWrapperTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testConstructorForNCIPOne() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		assertEquals(ncipOneClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP1Client");
		Field field = ncipOneClientWrapper.circulationClient.getClass().getDeclaredField("useSocket");
		field.setAccessible(true);
		Object value = field.get(ncipOneClientWrapper.circulationClient);
		assertEquals(((Boolean)value).booleanValue(),false);
		
	}
	
	@Test
	public void testConstructorForNCIPTwo() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP2);
		NCIPClientWrapper ncipTwoClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		assertEquals(ncipTwoClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP2Client");
		
	}
	
	@Test
	public void testConstructorForNCIPWMS() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.WMS);
		NCIPClientWrapper ncipTwoClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		assertEquals(ncipTwoClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP2WMSClient");
	}
	
	@Test
	public void testConstructorForNCIPSocket() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1_SOCKET);
		NCIPClientWrapper ncipTwoClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		assertEquals(ncipTwoClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP1Client");
	}
	
	
	@Test
	public void testSetTimeout() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		inputParms.put("socketTimeout", 99);
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		assertEquals(ncipOneClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP1Client");
		Field field = ncipOneClientWrapper.circulationClient.getClass().getDeclaredField("socketTimeout");
		field.setAccessible(true);
		Object value = field.get(ncipOneClientWrapper.circulationClient);
		assertEquals(((Integer)value).intValue(),99);
		
	}
	
	@Test
	public void testToMap() throws Exception {
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneLookupUserExampleResponse.json";
		String jsonAsString = readLineByLine(mockFileName);
		JSONObject mockResponseAsJson = new JSONObject(jsonAsString);
		Map<String, Object> map = ncipOneClientWrapper.toMap(mockResponseAsJson);
		assertEquals(((Map)map.get( "privileges" )).get( "STATUS" ),"OK");
		assertEquals((String)map.get( "firstName" ),"Jane Marie");
		
	}
	
	@Test
	public void testToMapTwo() throws Exception {
		
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "lightNcipRespone.json";
		String jsonAsString = readLineByLine(mockFileName);
		JSONObject mockResponseAsJson = new JSONObject(jsonAsString);
		Map<String, Object> map = ncipOneClientWrapper.toMap(mockResponseAsJson);
		assertEquals(((Map)map.get( "privileges" )).get( "STATUS" ),"OK");
		assertEquals((String)map.get( "firstName" ),"JANE");
	}
	
	@Test
	public void testConstructException() throws Exception {
		
		
		Map<String,Object> inputParms = new HashMap<String,Object>();
		inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",inputParms);
		
		
		Method method = ncipOneClientWrapper.circulationClient.getClass().getDeclaredMethod("constructException",String.class,String.class,String.class);
		method.setAccessible(true);
		JSONObject jsonObject =  (JSONObject) method.invoke(ncipOneClientWrapper.circulationClient,"localized message", "message", "to string value");
		//JSONObject jsonObject = ncipOneClientWrapper.constructException("localized message", "message", "to string value");
		
		assertEquals(jsonObject.getJSONArray("problems").getJSONObject(0).get("type"),"localized message");
		
	}
	
	

	
	  private static String readLineByLine(String filePath) {
		  
	      StringBuilder contentBuilder = new StringBuilder();
	      try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
	      {
	          stream.forEach(s -> contentBuilder.append(s).append("\n"));
	      }
	      catch (IOException e)
	      {
	          e.printStackTrace();
	      }
	      return contentBuilder.toString();
	  }
	  
	

}
