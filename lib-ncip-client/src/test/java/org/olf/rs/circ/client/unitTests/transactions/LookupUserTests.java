package org.olf.rs.circ.client.unitTests.transactions;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.AgencyUserPrivilegeType;
import org.extensiblecatalog.ncip.v2.service.ElectronicAddress;
import org.extensiblecatalog.ncip.v2.service.ElectronicAddressType;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserResponseData;
import org.extensiblecatalog.ncip.v2.service.NameInformation;
import org.extensiblecatalog.ncip.v2.service.PersonalNameInformation;
import org.extensiblecatalog.ncip.v2.service.SchemeValuePair;
import org.extensiblecatalog.ncip.v2.service.StructuredPersonalUserName;
import org.extensiblecatalog.ncip.v2.service.UserAddressInformation;
import org.extensiblecatalog.ncip.v2.service.UserAddressRoleType;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserOptionalFields;
import org.extensiblecatalog.ncip.v2.service.UserPrivilege;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatus;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatusType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;



public class LookupUserTests {
	

	@Test
	public void testConstructNcip1Response() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneLookupUserXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		LookupUser lookupUser = new LookupUser();
		JSONObject lookupUserResponse = lookupUser.constructResponseNcip1Response(xmlAsString);
		assertEquals(lookupUserResponse.getString("firstName").trim(),"Jane");
		String status = getValueByKey(lookupUserResponse.getJSONArray("privileges"),"STATUS");
		assertEquals(status,"OK");
		assertEquals(lookupUserResponse.get("userId"),"N55551212");
	}
	
	@Test
	public void testConstructNcip1BlockedResponse() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneLookupUserXmlResponseBlockedExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		LookupUser lookupUser = new LookupUser();
		JSONObject lookupUserResponse = lookupUser.constructResponseNcip1Response(xmlAsString);
		assertEquals(lookupUserResponse.getString("firstName").trim(),"Jane");
		String status = getValueByKey(lookupUserResponse.getJSONArray("privileges"),"STATUS");
		assertEquals(status,"BLOCKED");
		assertEquals(lookupUserResponse.get("userId"),"N55551212");
	}
	
	@Test
	public void testConstructNcip1WithStructName() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneStructuredName.xml";
		String xmlAsString = readLineByLine(mockFileName);
		LookupUser lookupUser = new LookupUser();
		JSONObject lookupUserResponse = lookupUser.constructResponseNcip1Response(xmlAsString);
		assertEquals(lookupUserResponse.getString("firstName").trim(),"John");
		assertEquals(lookupUserResponse.get("userId"),"share501");
	}
	
	@Test
	public void testConstructNcip2Response() throws Exception {
		LookupUser lookupUser = new LookupUser();
		LookupUserResponseData lookupUserResponseData = new LookupUserResponseData();
		UserId userId = new UserId();
		userId.setUserIdentifierValue("5551212");
		UserOptionalFields userOptionalFields = new UserOptionalFields();
		NameInformation nameInformation = new NameInformation();
		PersonalNameInformation personalNameInformation = new PersonalNameInformation();
		StructuredPersonalUserName structuredPersonalUserName = new StructuredPersonalUserName();
		structuredPersonalUserName.setGivenName("Jane");
		structuredPersonalUserName.setSurname("Doe");
		personalNameInformation.setStructuredPersonalUserName(structuredPersonalUserName);
		nameInformation.setPersonalNameInformation(personalNameInformation);
		userOptionalFields.setNameInformation(nameInformation);
		lookupUserResponseData.setUserOptionalFields(userOptionalFields);
		lookupUserResponseData.setUserId(userId);
		ArrayList<UserPrivilege> list = new ArrayList<UserPrivilege>();
		UserPrivilege userPrivilege = new UserPrivilege();
		UserPrivilege up = new UserPrivilege();
    	up.setUserPrivilegeDescription("library status");
    	AgencyId agencyId = new AgencyId("ABC");
    	up.setAgencyId(agencyId);
    	up.setAgencyUserPrivilegeType(new AgencyUserPrivilegeType(null,"status"));
    	UserPrivilegeStatus ups = new UserPrivilegeStatus();
    	ups.setUserPrivilegeStatusType(new UserPrivilegeStatusType(null,"OK"));
    	up.setUserPrivilegeStatus(ups);
    	list.add(up);
		userOptionalFields.setUserPrivileges(list);
		JSONObject jsonObject = lookupUser.constructResponseNcip2Response(lookupUserResponseData);
		assertEquals(jsonObject.get("userId"),"5551212");
		assertEquals(jsonObject.get("firstName"),"Jane");
		assertEquals(jsonObject.get("lastName"),"Doe");
		assertEquals(getValueByKey(jsonObject.getJSONArray("privileges"),"status"),"OK");
	}
	

	@Test(expected=Exception.class) 
	public void testConstructNcip2ResponseWithNull() throws Exception {
		LookupUser lookupUser = new LookupUser();
		JSONObject jsonObject = lookupUser.constructResponseNcip2Response(null);
	}

	@Test(expected=Exception.class) 
	public void testConstructNcip1ResponseWithNull() throws Exception {
		LookupUser lookupUser = new LookupUser();
		JSONObject jsonObject = lookupUser.constructResponseNcip1Response(null);
	}

	
	@Test
	public void testGenerateNcip1Object() throws Exception {
		LookupUser lookupUser = new LookupUser();
		lookupUser.setFromAgency("ABC");
		lookupUser.setToAgency("DEF");
		lookupUser.setUserId("P623111");
		String xml = lookupUser.generateNCIP1Object();
		Document document = Jsoup.parse(xml,"",Parser.xmlParser());
		String  fromAgencyId = document.select("LookupUser > InitiationHeader > FromAgencyId > UniqueAgencyId > Value").text();
		String  toAgencyId = document.select("LookupUser > InitiationHeader > ToAgencyId > UniqueAgencyId > Value").text();
		String  userId = document.select("LookupUser > UniqueUserId > UserIdentifierValue").text();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(userId,"P623111");
		
	}
	
	@Test 
	public void testGenerateNcip2Object() throws Exception {
		LookupUser lookupUser = new LookupUser();
		lookupUser.setFromAgency("ABC");
		lookupUser.setToAgency("DEF");
		lookupUser.setUserId("P67098660");
		LookupUserInitiationData initData = (LookupUserInitiationData) lookupUser.generateNCIP2Object();
		String toAgencyId = initData.getInitiationHeader().getToAgencyId().getAgencyId().getValue();
		String userId = initData.getUserId().getUserIdentifierValue();
		String fromAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(userId,"P67098660");
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
	
	 
		public String getValueByKey(JSONArray jsonArray,String desiredKey) {
			Iterator<Object> iterator = jsonArray.iterator();
			while(iterator.hasNext()){
			    JSONObject jsonObject = (JSONObject) iterator.next();
			    if (jsonObject.getString("key").equalsIgnoreCase("status"))  return jsonObject.getString("value");
			}
			return null;
		}
		
		@Test
		public void checkEmailPattern() {
			LookupUser lookupUser = new LookupUser();
			LookupUserResponseData lookupUserResponseData = new LookupUserResponseData();
			UserId userId = new UserId();
			userId.setUserIdentifierValue("5551212");
			UserOptionalFields userOptionalFields = new UserOptionalFields();
			NameInformation nameInformation = new NameInformation();
			PersonalNameInformation personalNameInformation = new PersonalNameInformation();
			StructuredPersonalUserName structuredPersonalUserName = new StructuredPersonalUserName();
			structuredPersonalUserName.setGivenName("Jane");
			structuredPersonalUserName.setSurname("Doe");
			personalNameInformation.setStructuredPersonalUserName(structuredPersonalUserName);
			nameInformation.setPersonalNameInformation(personalNameInformation);
			userOptionalFields.setNameInformation(nameInformation);
			lookupUserResponseData.setUserOptionalFields(userOptionalFields);
			lookupUserResponseData.setUserId(userId);
			UserAddressInformation userAddressInformation = new UserAddressInformation();
			UserAddressRoleType userAddressRoleType = new UserAddressRoleType("OTH");
			ElectronicAddress electronicAddress = new ElectronicAddress();
			electronicAddress.setElectronicAddressData("test@test.com");
			userAddressInformation.setElectronicAddress(electronicAddress);
			userAddressInformation.setUserAddressRoleType(userAddressRoleType);
			List<UserAddressInformation> addresses = new ArrayList<UserAddressInformation>();
			addresses.add(userAddressInformation);
			lookupUserResponseData.getUserOptionalFields().setUserAddressInformations(addresses);
			ArrayList<UserPrivilege> list = new ArrayList<UserPrivilege>();
			UserPrivilege userPrivilege = new UserPrivilege();
			UserPrivilege up = new UserPrivilege();
	    	up.setUserPrivilegeDescription("library status");
	    	AgencyId agencyId = new AgencyId("ABC");
	    	up.setAgencyId(agencyId);
	    	up.setAgencyUserPrivilegeType(new AgencyUserPrivilegeType(null,"status"));
	    	UserPrivilegeStatus ups = new UserPrivilegeStatus();
	    	ups.setUserPrivilegeStatusType(new UserPrivilegeStatusType(null,"OK"));
	    	up.setUserPrivilegeStatus(ups);
	    	list.add(up);
			userOptionalFields.setUserPrivileges(list);
			//CHECK FOR EMAIL
			JSONObject jsonObject = lookupUser.constructResponseNcip2Response(lookupUserResponseData);
			assertEquals(jsonObject.getJSONArray("electronicAddresses").getJSONObject(0).getString("value"),"test@test.com");
			lookupUserResponseData.getUserOptionalFields().getUserAddressInformation(0).getElectronicAddress().setElectronicAddressData("6105551212");
			//MAKE SURE PHONE IS NOT CONFUSED FOR EMAIL
			JSONObject jsonObjectWithoutEmail = lookupUser.constructResponseNcip2Response(lookupUserResponseData);
			assertTrue(!jsonObjectWithoutEmail.getJSONArray("electronicAddresses").getJSONObject(0).has("emailAddress"));
			lookupUserResponseData.getUserOptionalFields().getUserAddressInformations().add(userAddressInformation);
			//MAKE SURE BOTH PHONE AND EMAIL ADDRESS ARE IN THE RETURN
			JSONObject jsonObjectWithBoth = lookupUser.constructResponseNcip2Response(lookupUserResponseData);
			assertTrue(jsonObjectWithBoth.getJSONArray("electronicAddresses").length() == 2);
			userAddressInformation.getElectronicAddress().setElectronicAddressData("");
			//IGNORE BLANK VALUES
			lookupUserResponseData.getUserOptionalFields().getUserAddressInformations().add(userAddressInformation);
			assertTrue(jsonObjectWithBoth.getJSONArray("electronicAddresses").length() == 2);
			JSONObject blankEmail = lookupUser.constructResponseNcip2Response(lookupUserResponseData);

		}

		@Test
		public void checkMissingFirstName() {
			LookupUser lookupUser = new LookupUser();
			LookupUserResponseData lookupUserResponseData = new LookupUserResponseData();
			UserId userId = new UserId();
			userId.setUserIdentifierValue("5551212");
			UserOptionalFields userOptionalFields = new UserOptionalFields();
			NameInformation nameInformation = new NameInformation();
			PersonalNameInformation personalNameInformation = new PersonalNameInformation();
			StructuredPersonalUserName structuredPersonalUserName = new StructuredPersonalUserName();
			structuredPersonalUserName.setGivenName("");
			structuredPersonalUserName.setSurname("Doe, Jane");
			personalNameInformation.setStructuredPersonalUserName(structuredPersonalUserName);
			nameInformation.setPersonalNameInformation(personalNameInformation);
			userOptionalFields.setNameInformation(nameInformation);
			lookupUserResponseData.setUserOptionalFields(userOptionalFields);
			lookupUserResponseData.setUserId(userId);
			UserAddressInformation userAddressInformation = new UserAddressInformation();
			UserAddressRoleType userAddressRoleType = new UserAddressRoleType("OTH");
			ElectronicAddress electronicAddress = new ElectronicAddress();
			electronicAddress.setElectronicAddressData("test@test.com");
			userAddressInformation.setElectronicAddress(electronicAddress);
			userAddressInformation.setUserAddressRoleType(userAddressRoleType);
			List<UserAddressInformation> addresses = new ArrayList<UserAddressInformation>();
			addresses.add(userAddressInformation);
			lookupUserResponseData.getUserOptionalFields().setUserAddressInformations(addresses);
			ArrayList<UserPrivilege> list = new ArrayList<UserPrivilege>();
			UserPrivilege userPrivilege = new UserPrivilege();
			UserPrivilege up = new UserPrivilege();
	    	up.setUserPrivilegeDescription("library status");
	    	AgencyId agencyId = new AgencyId("ABC");
	    	up.setAgencyId(agencyId);
	    	up.setAgencyUserPrivilegeType(new AgencyUserPrivilegeType(null,"status"));
	    	UserPrivilegeStatus ups = new UserPrivilegeStatus();
	    	ups.setUserPrivilegeStatusType(new UserPrivilegeStatusType(null,"OK"));
	    	up.setUserPrivilegeStatus(ups);
	    	list.add(up);
			userOptionalFields.setUserPrivileges(list);
			
			JSONObject response = lookupUser.constructResponseNcip2Response(lookupUserResponseData);

			assertEquals(response.getString("firstName"), "Jane");
			assertEquals(response.getString("lastName"), "Doe");
			

		}
		
		
		@Test
		public void testPatronNotFound() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserNotFound.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject parsedLookupUser = lookupUser.constructWMSResponse(responseObject);
			assertTrue(parsedLookupUser.has("problems"));
			JSONArray problems = parsedLookupUser.getJSONArray("problems");
			String errorType = problems.getJSONObject(0).getString("type");
			String element = problems.getJSONObject(0).getString("element");
			String details = problems.getJSONObject(0).getString("detail");
			assertTrue(errorType.contains("Unknown User"));
			assertTrue(element.contains("User"));
			assertTrue(details.contains("did not return a matching patron"));
		}
		
		@Test
		public void testBlockedPatron() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserBlockedPatron.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertEquals(lookupUserResponse.getString("firstName").trim(),"Jane");
			String status = getValueByKey(lookupUserResponse.getJSONArray("privileges"),"STATUS");
			assertEquals(status,"BLOCKED");
			assertEquals(lookupUserResponse.get("userId"),"joe_blocker");
		}
		
		@Test
		public void testExpiredPatron() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserExpired.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertEquals(lookupUserResponse.getString("firstName").trim(),"Jane");
			String status = getValueByKey(lookupUserResponse.getJSONArray("privileges"),"STATUS");
			assertEquals(status,"BLOCKED");
			assertEquals(lookupUserResponse.get("userId"),"doe_expired");
		}
		
		@Test
		public void testGoodPatron() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserGoodPatron.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertEquals(lookupUserResponse.getString("firstName").trim(),"Jane");
			String status = getValueByKey(lookupUserResponse.getJSONArray("privileges"),"STATUS");
			assertEquals("OK", status);
			assertEquals(lookupUserResponse.get("userId"),"2200555");
		}		
		
		@Test(expected=JSONException.class) 
		public void testNotAuthorized() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserMissingPermissions.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertTrue(lookupUserResponse.has("problems"));
			JSONArray problems = lookupUserResponse.getJSONArray("problems");
		}
		
		@Test(expected=JSONException.class) 
		public void testBadRequest() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserUnableToHandle.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertTrue(lookupUserResponse.has("problems"));
			JSONArray problems = lookupUserResponse.getJSONArray("problems");
		}
		
		@Test(expected=JSONException.class) 
		public void testMissingPermission() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserMissingPermissions.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertTrue(lookupUserResponse.has("problems"));
			JSONArray problems = lookupUserResponse.getJSONArray("problems");
		}
		
		@Test(expected=JSONException.class) 
		public void testUnsupportedEndpoint() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserUnsupportedEndpoint.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertTrue(lookupUserResponse.has("problems"));
			JSONArray problems = lookupUserResponse.getJSONArray("problems");
		}
		
		@Test(expected=JSONException.class) 
		public void testUnsupportedMedia() {
			String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "wmsLookupUserUnsupportedMediaType.json";
			String mockResponse = readLineByLine(mockFileName);
			JSONObject responseObject = new JSONObject(mockResponse);
			LookupUser lookupUser = new LookupUser();
			JSONObject lookupUserResponse = lookupUser.constructWMSResponse(responseObject);
			assertTrue(lookupUserResponse.has("problems"));
			JSONArray problems = lookupUserResponse.getJSONArray("problems");
		}
		
		@Test 
		public void testToString() throws Exception {
			LookupUser lookupUser = new LookupUser();
			lookupUser.setUserId("5551212");
			String lookupUserAsString = lookupUser.toString();
			assertTrue(lookupUserAsString.contains("5551212"));
		}
		

}
