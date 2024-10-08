package org.olf.rs.circ.client.unitTests.transactions;

import static org.junit.Assert.*;

import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.junit.Test;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.RequestItem;
import org.olf.rs.circ.client.TestConstants;
import org.olf.rs.circ.client.XCToolkitUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;


public class RequestItemTests {

    @Test
    public void testGenerateNcip1Object() {
        RequestItem requestItem = new RequestItem();
        requestItem.setRequestId("123");
        requestItem.setFromAgency("ABC");
        requestItem.setToAgency("CBA");
        requestItem.setUserId("v12345");
        requestItem.setBibliographicRecordId("pn234092945");
        requestItem.setBibliographicRecordIdCode("LCOC");
        requestItem.setRequestType("Loan");
        String xml = requestItem.generateNCIP1Object();
        assertTrue(xml.contains("v12345"));
        assertNotNull(xml);
    }

    @Test
    public void testGenerateNcip2Object() {
        RequestItem requestItem = new RequestItem();
        requestItem.setRequestId("123");
        requestItem.setFromAgency("ABC");
        requestItem.setToAgency("CBA");
        requestItem.setUserId("v12345");
        requestItem.setBibliographicRecordId("pn234092945");
        requestItem.setBibliographicRecordIdCode("LCOC");
        requestItem.setRequestType("Loan");
        requestItem.setItemLocationCode("NC");
        RequestItemInitiationData initData
                = (RequestItemInitiationData) requestItem.generateNCIP2Object();
        InitiationHeader initiationHeader = initData.getInitiationHeader();
        assertEquals("CBA", initiationHeader.getToAgencyId().getAgencyId().getValue());
        assertEquals("NC", initData.getItemOptionalFields().getLocation(0).getLocationName()
                .getLocationNameInstance(0).getLocationNameValue());
    }

    @Test
    public void testGenerateNcip1Response() {
        String mockFileName = TestConstants.PATH_TO_MOCK_FILES + "ncipOneRequestItemXmlResponseExample.xml";
        String xmlAsString = TestUtil.readLineByLine(mockFileName);
        RequestItem requestItem = new RequestItem();
        JSONObject requestItemResponse = requestItem.constructResponseNcip1Response(xmlAsString);
        assertEquals("7355677", requestItemResponse.get("requestId"));
        assertEquals("1669775-1001", requestItemResponse.get("itemId"));
    }

    @Test
    public void testConstructNcip2Response() throws Exception {
        RequestItem requestItem = new RequestItem();
        RequestItemResponseData requestItemResponseData = new RequestItemResponseData();
        ItemId itemId = new ItemId();
        itemId.setItemIdentifierValue("22334455");
        RequestId requestId = new RequestId();
        requestId.setRequestIdentifierValue("RESH-99");
        LocationNameInstance locationNameInstance = new LocationNameInstance();
        locationNameInstance.setLocationNameLevel(new BigDecimal(4));
        locationNameInstance.setLocationNameValue("Main library");
        LocationName locationName = new LocationName();
        locationName.setLocationNameInstances(Collections.singletonList(locationNameInstance));
        Location location = new Location();
        location.setLocationName(locationName);
        ItemDescription itemDescription = new ItemDescription();
        itemDescription.setCopyNumber("b-123");
        itemDescription.setCallNumber("TK5105.88815 . A58 2004 FT MEADE");
        ItemOptionalFields itemOptionalFields = new ItemOptionalFields();
        itemOptionalFields.setItemDescription(itemDescription);
        itemOptionalFields.setLocations(Collections.singletonList(location));
        requestItemResponseData.setItemId(itemId);
        requestItemResponseData.setRequestId(requestId);
        requestItemResponseData.setItemOptionalFields(itemOptionalFields);
        JSONObject jsonObject = requestItem.constructResponseNcip2Response(requestItemResponseData);
        assertEquals(jsonObject.getString("itemId"), "22334455");
        assertEquals(jsonObject.getString("requestId"), "RESH-99");
        assertEquals(jsonObject.getString("barcode"), "b-123");
        assertEquals(jsonObject.getString("callNumber"), "TK5105.88815 . A58 2004 FT MEADE");
        assertEquals(jsonObject.getString("location"), "Main library");
    }

    @Test
    public void testConstructNcip2ResponseWithoutItemId() throws Exception {
        RequestItem requestItem = new RequestItem();
        RequestItemResponseData requestItemResponseData = new RequestItemResponseData();
        RequestId requestId = new RequestId();
        requestId.setRequestIdentifierValue("RESH-99");
        requestItemResponseData.setRequestId(requestId);
        JSONObject jsonObject = requestItem.constructResponseNcip2Response(requestItemResponseData);
        assertEquals(jsonObject.getString("requestId"), "RESH-99");
    }

    @Test
    public void testToString() {
        RequestItem requestItem = new RequestItem();
        requestItem.setRequestId("123");
        requestItem.setFromAgency("ABC");
        requestItem.setToAgency("CBA");
        requestItem.setUserId("v12345");
        requestItem.setBibliographicRecordId("pn234092945");
        requestItem.setBibliographicRecordIdCode("LCOC");
        String asString = requestItem.toString();
        assertTrue(asString.contains("pn234092945"));
        assertTrue(asString.contains("v12345"));
    }

    @Test
    public void testConvertResponseStringToJson() throws Exception{
        XCToolkitUtil xcToolkitUtil = XCToolkitUtil.getInstance();
        NCIP2Client.setUpMapping();
        String mockFileName =  TestConstants.PATH_TO_MOCK_FILES + "ncipTwoRequestItem.xml";
        String responseString = LookupUserTests.readLineByLine(mockFileName);
        InputStream stream = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));
        NCIPResponseData responseData = xcToolkitUtil.translator.createResponseData(xcToolkitUtil.serviceContext, stream);
        RequestItem requestItem = new RequestItem();
        JSONObject jsonObject = requestItem.constructResponseNcip2Response(responseData);
        assertEquals(jsonObject.getString("itemId"), "980001");
        assertEquals(jsonObject.getString("requestId"), "99873b27-893f-4611-98bf-72a92a9a82c8");
        assertEquals(jsonObject.getString("callNumber"), "Holdings magazine Q1");
        assertEquals(jsonObject.getString("location"), "Main Library");
        assertEquals(jsonObject.getString("library"), "Datalogisk Institut");
        assertEquals(jsonObject.getString("userUuid"), "764fe3bf-e09b-4fcc-b4a9-c78aab6995f1");
    }
}
