package org.olf.rs.circ.client.unitTests.transactions;


import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.TestConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CheckoutItemTests {
	

	@Test
	public void testConstructNcip1Response() {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneCheckoutItemXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		CheckoutItem checkoutItem = new CheckoutItem();
		JSONObject checkoutItemResponse = checkoutItem.constructResponseNcip1Response(xmlAsString);
		assertEquals(checkoutItemResponse.get("itemId"),"5555555025805");
		assertEquals(checkoutItemResponse.get("dueDate"),"2021-06-30T16:45:00Z");
		assertEquals(checkoutItemResponse.get("userId"),"TNSNOTREAL");
	}
	
	@Test
	public void testConstructNcip2Response() {
		CheckoutItem checkoutItem = new CheckoutItem();
		CheckOutItemResponseData checkOutItemResponseData = new CheckOutItemResponseData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue("2938470293874");
		UserId userId = new UserId();
		userId.setUserIdentifierValue("5551212");
		ItemId loanId = new ItemId();
		loanId.setItemIdentifierType(new ItemIdentifierType("Scheme", "loanUuid"));
		loanId.setItemIdentifierValue("1231231234");
		checkOutItemResponseData.setItemId(itemId);
		checkOutItemResponseData.setUserId(userId);
		checkOutItemResponseData.setLoanUuid(loanId);
		ItemOptionalFields itemOptionalFields = new ItemOptionalFields();
		ItemDescription itemDescription = new ItemDescription();
		itemDescription.setCallNumber("c123");
		itemOptionalFields.setItemDescription(itemDescription);
		checkOutItemResponseData.setItemOptionalFields(itemOptionalFields);

		JSONObject jsonObject = checkoutItem.constructResponseNcip2Response(checkOutItemResponseData);
		assertEquals("2938470293874", jsonObject.get("itemId"));
		assertEquals("5551212", jsonObject.get("userId"));
		assertEquals("1231231234", jsonObject.get("loanUuid"));
		assertEquals("c123", jsonObject.get("callNumber"));
	}

	
	
	@Test
	public void testGenerateNcip1Object() {
		CheckoutItem checkoutItem = new CheckoutItem();
		checkoutItem.setFromAgency("ABC");
		checkoutItem.setToAgency("DEF");
		checkoutItem.setItemId("ABC-2387402374");
		String xml = checkoutItem.generateNCIP1Object();
		Document document = Jsoup.parse(xml,"",Parser.xmlParser());
		String  fromAgencyId = document.select("CheckOutItem > InitiationHeader > FromAgencyId > UniqueAgencyId > Value").text();
		String  toAgencyId = document.select("CheckOutItem > InitiationHeader > ToAgencyId > UniqueAgencyId > Value").text();
		String  itemId = document.select("CheckOutItem > UniqueItemId > ItemIdentifierValue").text();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(itemId,"ABC-2387402374");
		
	}
	
	@Test 
	public void testGenerateNcip2Object() {
		CheckoutItem checkoutItem = new CheckoutItem();
		checkoutItem.setFromAgency("ABC");
		checkoutItem.setToAgency("DEF");
		checkoutItem.setItemId("ABC-2387402374");
		CheckOutItemInitiationData initData = (CheckOutItemInitiationData) checkoutItem.generateNCIP2Object();
		String toAgencyId = initData.getInitiationHeader().getToAgencyId().getAgencyId().getValue();
		String itemId = initData.getItemId().getItemIdentifierValue();
		String fromAgencyId = initData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"DEF");
		assertEquals(itemId,"ABC-2387402374");
	}
	
	@Test 
	public void testToString() {
		CheckoutItem checkoutItem = new CheckoutItem();
		checkoutItem.setFromAgency("ABC");
		checkoutItem.setToAgency("DEF");
		checkoutItem.setItemId("ABC-2387402374");
		String checkoutItemAsString = checkoutItem.toString();
		assertTrue(checkoutItemAsString.contains("ABC-2387402374"));
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
