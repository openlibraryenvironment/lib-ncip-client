package org.olf.rs.circ.client.unitTests.transactions;

import static org.junit.Assert.*;

import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.RequestItemInitiationData;
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
        String xml = requestItem.generateNCIP1Object();
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
}
