package org.olf.rs.circ.client.unitTests.transactions;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;

import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemResponseData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemResponseData;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;



public class CheckoutItemTests {
	

	@Test
	public void testConstructNcip1Response() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneCheckoutItemXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		CheckoutItem checkoutItem = new CheckoutItem();
		JSONObject checkoutItemResponse = checkoutItem.constructResponseNcip1Response(xmlAsString);
		assertEquals(checkoutItemResponse.get("itemId"),"5555555025805");
		assertEquals(checkoutItemResponse.get("dueDate"),"2021-06-30T16:45:00Z");
		assertEquals(checkoutItemResponse.get("userId"),"TNSNOTREAL");
	}
	
	@Test
	public void testConstructNcip2Response() throws Exception {
		CheckoutItem checkoutItem = new CheckoutItem();
		CheckOutItemResponseData checkOutItemResponseData = new CheckOutItemResponseData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue("2938470293874");
		UserId userId = new UserId();
		userId.setUserIdentifierValue("5551212");
		checkOutItemResponseData.setItemId(itemId);
		checkOutItemResponseData.setUserId(userId);
		JSONObject jsonObject = checkoutItem.constructResponseNcip2Response(checkOutItemResponseData);
		assertEquals(jsonObject.get("itemId"),"2938470293874");
		assertEquals(jsonObject.get("userId"),"5551212");
	}

	
	
	@Test
	public void testGenerateNcip1Object() throws Exception {
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
	public void testGenerateNcip2Object() throws Exception {
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
