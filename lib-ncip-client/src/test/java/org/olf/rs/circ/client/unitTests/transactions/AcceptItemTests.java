package org.olf.rs.circ.client.unitTests.transactions;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.AcceptItemResponseData;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.Constants;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;



public class AcceptItemTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testConstructNcip1Response() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneAcceptItemXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		AcceptItem acceptItem = new AcceptItem();
		JSONObject acceptItemResponse = acceptItem.constructResponseNcip1Response(xmlAsString);
		assertEquals(acceptItemResponse.get("itemId"),"100556043-10");
	}
	
	@Test
	public void testConstructNcip2Response() throws Exception {
		AcceptItem acceptItem = new AcceptItem();
		AcceptItemResponseData acceptItemResponseData = new AcceptItemResponseData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue("2938470293874");
		acceptItemResponseData.setItemId(itemId);
		RequestId requestId = new RequestId();
		requestId.setRequestIdentifierValue("ABC-123456");
		acceptItemResponseData.setRequestId(requestId);
		JSONObject jsonObject = acceptItem.constructResponseNcip2Response(acceptItemResponseData);
		assertEquals(jsonObject.get("itemId"),"2938470293874");
		assertEquals(jsonObject.get("requestId"),"ABC-123456");
	}
	
	@Test
	public void testGenerateNcip1Object() throws Exception {
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setFromAgency("ABC");
		acceptItem.setToAgency("DEF");
		acceptItem.setItemId("ABC-2387402374");
		acceptItem.setRequestId("ABC-20200501");
		acceptItem.setPickupLocation("MAINLIBRARY");
		String acceptItemXML = acceptItem.generateNCIP1Object();
		Document document = Jsoup.parse(acceptItemXML,"",Parser.xmlParser());
		String  fromAgencyId = document.select("AcceptItem > InitiationHeader > FromAgencyId > UniqueAgencyId > Value").text();
		String  toAgencyId = document.select("AcceptItem > InitiationHeader > ToAgencyId > UniqueAgencyId > Value").text();
		String  itemId = document.select("AcceptItem > UniqueItemId > ItemIdentifierValue").text();
		String  requestId = document.select("AcceptItem > UniqueRequestId > RequestIdentifierValue").text();
		assertEquals(fromAgencyId,"ABC");
		assertEquals(toAgencyId,"MAINLIBRARY");
		assertEquals(itemId,"ABC-2387402374");
		assertEquals(requestId,"ABC-20200501");
		String asString = acceptItem.toString();
		assertTrue(asString.contains("MAINLIBRARY"));
		
	}
	
	@Test 
	public void testGenerateNcip2Object() {
		AcceptItem acceptItem = new AcceptItem()
				.setToAgency("ABC")
				.setItemId("ABC-2387402374")
				.setChargeDefaultPatronFee(true);
		AcceptItemInitiationData initData = (AcceptItemInitiationData) acceptItem.generateNCIP2Object();
		String toAgencyId = initData.getInitiationHeader().getToAgencyId().getAgencyId().getValue();
		String itemId = initData.getItemId().getItemIdentifierValue();
		assertEquals(toAgencyId,"ABC");
		assertEquals(itemId,"ABC-2387402374");
		assertEquals(Constants.CHARGE_DEFAULT_PATRON_FEE, initData.getFiscalTransactionInformation().getFiscalActionType().getValue());
	}

	@Test
	public void testGenerateNcip2ObjectNoFee() {
		AcceptItem acceptItem = new AcceptItem()
				.setToAgency("ABC")
				.setItemId("ABC-2387402374")
				.setChargeDefaultPatronFee(false);
		AcceptItemInitiationData initData = (AcceptItemInitiationData) acceptItem.generateNCIP2Object();
		String toAgencyId = initData.getInitiationHeader().getToAgencyId().getAgencyId().getValue();
		String itemId = initData.getItemId().getItemIdentifierValue();
		assertEquals(toAgencyId,"ABC");
		assertEquals(itemId,"ABC-2387402374");
		assertNull(initData.getFiscalTransactionInformation());
	}
	
	@Test 
	public void testToString() throws Exception {
		AcceptItem acceptItem = new AcceptItem();
		acceptItem.setToAgency("ABC");
		acceptItem.setItemId("ABC-2387402374");
		String acceptItemAsString = acceptItem.toString();
		assertTrue(acceptItemAsString.contains("ABC-2387402374"));
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
