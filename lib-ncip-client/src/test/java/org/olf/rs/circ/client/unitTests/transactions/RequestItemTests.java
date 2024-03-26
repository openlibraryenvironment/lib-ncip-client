package org.olf.rs.circ.client.unitTests.transactions;

import static org.junit.Assert.*;

import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.junit.Test;
import org.olf.rs.circ.client.RequestItem;
import org.olf.rs.circ.client.TestConstants;


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
        RequestItemInitiationData initData
                = (RequestItemInitiationData) requestItem.generateNCIP2Object();
        InitiationHeader initiationHeader = initData.getInitiationHeader();
        assertEquals("CBA", initiationHeader.getToAgencyId().getAgencyId().getValue());
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
        requestItemResponseData.setItemId(itemId);
        requestItemResponseData.setRequestId(requestId);
        JSONObject jsonObject = requestItem.constructResponseNcip2Response(requestItemResponseData);
        assertEquals(jsonObject.getString("itemId"), "22334455");
        assertEquals(jsonObject.getString("requestId"), "RESH-99");
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
}
