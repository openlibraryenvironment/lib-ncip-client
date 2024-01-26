package org.olf.rs.circ.client.unitTests.transactions;

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
    public void testToString() {
        CancelRequestItem cancelRequestItem = new CancelRequestItem();
        cancelRequestItem.setRequestId("123");
        cancelRequestItem.setFromAgency("ABC");
        cancelRequestItem.setToAgency("CBA");
        String asString = cancelRequestItem.toString();
        assertTrue(asString.contains("123"));
        assertTrue(asString.contains("CBA"));
    }
}
