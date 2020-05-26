package org.olf.rs.circ.client.unitTests.transactions;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemResponseData;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.TestConstants;
import static org.junit.Assert.*;



public class CheckinItemTests {
	

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testConstructNcip1Response() throws Exception {
		String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipOneCheckinItemXmlResponseExample.xml";
		String xmlAsString = readLineByLine(mockFileName);
		CheckinItem checkinItem = new CheckinItem();
		JSONObject checkinItemResponse = checkinItem.constructResponseNcip1Response(xmlAsString);
		assertEquals(checkinItemResponse.get("itemId"),"55531234752954");
	}
	
	@Test
	public void testConstructNcip2Response() throws Exception {
		CheckinItem checkinItem = new CheckinItem();
		CheckInItemResponseData checkInItemResponseData = new CheckInItemResponseData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue("2938470293874");
		checkInItemResponseData.setItemId(itemId);
		JSONObject jsonObject = checkinItem.constructResponseNcip2Response(checkInItemResponseData);
		assertEquals(jsonObject.get("itemId"),"2938470293874");
	}
	
	
	
	@Test
	public void testGenerateNcip1Object() throws Exception {
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
	public void testGenerateNcip2Object() throws Exception {
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

