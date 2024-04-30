package org.olf.rs.circ.client.unitTests.transactions;

import org.extensiblecatalog.ncip.v2.service.CancelRequestItemResponseData;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.json.JSONObject;
import org.junit.Test;
import org.olf.rs.circ.client.CancelRequestItem;
import org.olf.rs.circ.client.TestConstants;

import static org.junit.Assert.*;

public class CancelRequestItemTests {
    @Test
    public void testGenerateNcip1Object() {
        CancelRequestItem cancelRequestItem = new CancelRequestItem();
        cancelRequestItem.setRequestId("123");
        cancelRequestItem.setFromAgency("ABC");
        cancelRequestItem.setToAgency("CBA");
        String xml = cancelRequestItem.generateNCIP1Object();
        assertNotNull(xml);
    }

    @Test
    public void testGenerateNcip1Response() {
        String mockFileName = TestConstants.PATH_TO_MOCK_FILES + "ncipOneCancelRequestItemXmlResponseExample.xml";
        String xmlAsString = TestUtil.readLineByLine(mockFileName);
        CancelRequestItem cancelRequestItem = new CancelRequestItem();
        JSONObject cancelRequestItemResponse = cancelRequestItem.constructResponseNcip1Response(xmlAsString);
        assertEquals("12345", cancelRequestItemResponse.get("requestId"));

    }

    @Test
    public void testConstructNcip2Response() throws Exception {
        CancelRequestItem cancelRequestItem = new CancelRequestItem();
        CancelRequestItemResponseData cancelRequestItemResponseData = new CancelRequestItemResponseData();
        ItemId itemId = new ItemId();
        itemId.setItemIdentifierValue("22334455");
        RequestId requestId = new RequestId();
        requestId.setRequestIdentifierValue("RESH-99");
        cancelRequestItemResponseData.setItemId(itemId);
        cancelRequestItemResponseData.setRequestId(requestId);
        JSONObject jsonObject = cancelRequestItem.constructResponseNcip2Response(cancelRequestItemResponseData);
        assertEquals(jsonObject.getString("itemId"), "22334455");
        assertEquals(jsonObject.getString("requestId"), "RESH-99");
    }

    @Test
    public void testToString() {
        CancelRequestItem cancelRequestItem = new CancelRequestItem();
        cancelRequestItem.setRequestId("123");
        cancelRequestItem.setFromAgency("ABC");
        cancelRequestItem.setToAgency("CBA");
        cancelRequestItem.setRegistryId("44556677");
        String asString = cancelRequestItem.toString();
        assertTrue(asString.contains("123"));
        assertTrue(asString.contains("CBA"));
    }
}
