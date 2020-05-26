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
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserResponseData;
import org.extensiblecatalog.ncip.v2.service.NameInformation;
import org.extensiblecatalog.ncip.v2.service.PersonalNameInformation;
import org.extensiblecatalog.ncip.v2.service.StructuredPersonalUserName;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserOptionalFields;
import org.extensiblecatalog.ncip.v2.service.UserPrivilege;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatus;
import org.extensiblecatalog.ncip.v2.service.UserPrivilegeStatusType;
import org.json.JSONArray;
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


}
