package org.olf.rs.circ.client.unitTests.clients;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",NCIPClientWrapper.NCIP1);
		assertEquals(ncipOneClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP1Client");
		Field field = ncipOneClientWrapper.circulationClient.getClass().getDeclaredField("useSockets");
		field.setAccessible(true);
		Object value = field.get(ncipOneClientWrapper.circulationClient);
		assertEquals(((Boolean)value).booleanValue(),false);
		
	}
	
	@Test
	public void testConstructorForNCIPTwo() throws Exception {
		
		NCIPClientWrapper ncipTwoClientWrapper = new NCIPClientWrapper("http://google.com",NCIPClientWrapper.NCIP2);
		assertEquals(ncipTwoClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP2Client");
		
	}
	
	
	@Test
	public void testSetTimeout() throws Exception {
		
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",NCIPClientWrapper.NCIP1);
		ncipOneClientWrapper.setTimeout(99);
		assertEquals(ncipOneClientWrapper.circulationClient.getClass().getName(),"org.olf.rs.circ.client.NCIP1Client");
		Field field = ncipOneClientWrapper.circulationClient.getClass().getDeclaredField("socketTimeout");
		field.setAccessible(true);
		Object value = field.get(ncipOneClientWrapper.circulationClient);
		assertEquals(((Integer)value).intValue(),99);
		
	}
	
	@Test
	public void testToMap() throws Exception {
		
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",NCIPClientWrapper.NCIP1);
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneLookupUserExampleResponse.json";
		String jsonAsString = readLineByLine(mockFileName);
		JSONObject mockResponseAsJson = new JSONObject(jsonAsString);
		Map<String, Object> map = ncipOneClientWrapper.toMap(mockResponseAsJson);
		assertEquals(((Map)map.get( "privileges" )).get( "STATUS" ),"OK");
		assertEquals((String)map.get( "firstName" ),"Jane Marie");
		
	}
	
	@Test
	public void testConstructException() throws Exception {
		
		NCIPClientWrapper ncipOneClientWrapper = new NCIPClientWrapper("http://google.com",NCIPClientWrapper.NCIP1);
		JSONObject jsonObject = ncipOneClientWrapper.constructException("localized message", "message", "to string value");
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
