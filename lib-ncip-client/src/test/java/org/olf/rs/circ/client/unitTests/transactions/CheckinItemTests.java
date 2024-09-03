package org.olf.rs.circ.client.unitTests.transactions;


import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.TestConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CheckinItemTests {
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testConstructNcip1Response() {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneCheckinItemXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		CheckinItem checkinItem = new CheckinItem();
		JSONObject checkinItemResponse = checkinItem.constructResponseNcip1Response(xmlAsString);
		assertEquals(checkinItemResponse.get("itemId"),"55531234752954");
	}
	
	@Test
	public void testConstructNcip2Response() {
		CheckinItem checkinItem = new CheckinItem();
		CheckInItemResponseData checkInItemResponseData = new CheckInItemResponseData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue("2938470293874");

		ItemId loanId = new ItemId();
		loanId.setItemIdentifierType(new ItemIdentifierType("Scheme", "loanUuid"));
		loanId.setItemIdentifierValue("1231231234");

		UserId userId = new UserId();
		userId.setUserIdentifierType(new UserIdentifierType("Scheme", "userUuid"));
		userId.setUserIdentifierValue("91231543543");

		checkInItemResponseData.setLoanUuid(loanId);
		checkInItemResponseData.setUserUuid(userId);
		checkInItemResponseData.setItemId(itemId);
		JSONObject jsonObject = checkinItem.constructResponseNcip2Response(checkInItemResponseData);
		assertEquals(jsonObject.get("itemId"),"2938470293874");
		assertEquals("1231231234", jsonObject.get("loanUuid"));
		assertEquals("91231543543", jsonObject.get("userUuid"));
	}
	
	@Test
	public void testGenerateNcip1Object() {
		CheckinItem checkinItem = new CheckinItem();
		checkinItem.setFromAgency("ABC");
		checkinItem.setToAgency("DEF");
		checkinItem.setItemId("ABC-2387402374");
		String xml = checkinItem.generateNCIP1Object();
		Document document = Jsoup.parse(xml,"",Parser.xmlParser());
		String  fromAgencyId = document.select("CheckInItem > InitiationHeader > FromAgencyId > UniqueAgencyId > Value").text();
		String  toAgencyId = document.select("CheckInItem > InitiationHeader > ToAgencyId > UniqueAgencyId > Value").text();
		String  itemId = document.select("CheckInItem > UniqueItemId > ItemIdentifierValue").text();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(itemId,"ABC-2387402374");
	}
	
	@Test 
	public void testGenerateNcip2Object() {
		CheckinItem checkinItem = new CheckinItem();
		checkinItem.setFromAgency("ABC");
		checkinItem.setToAgency("DEF");
		checkinItem.setItemId("ABC-2387402374");
		CheckInItemInitiationData initData = (CheckInItemInitiationData) checkinItem.generateNCIP2Object();
		String toAgencyId = initData.getInitiationHeader().getToAgencyId().getAgencyId().getValue();
		String itemId = initData.getItemId().getItemIdentifierValue();
		String fromAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(itemId,"ABC-2387402374");
	}
	
	@Test 
	public void testToString() {
		CheckinItem checkinItem = new CheckinItem();
		checkinItem.setFromAgency("ABC");
		checkinItem.setToAgency("DEF");
		checkinItem.setItemId("ABC-2387402374");
		String checkinItemAsString = checkinItem.toString();
		assertTrue(checkinItemAsString.contains("ABC-2387402374"));
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

