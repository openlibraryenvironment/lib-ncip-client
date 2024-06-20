package org.olf.rs.circ.client.unitTests.transactions;

import org.extensiblecatalog.ncip.v2.service.DeleteItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.DeleteItemResponseData;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.json.JSONObject;
import org.junit.Test;
import org.olf.rs.circ.client.DeleteItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeleteItemTests {

    @Test
    public void testConstructNcip2Response() {
        DeleteItem deleteItem = new DeleteItem();
        DeleteItemResponseData deleteItemResponseData = new DeleteItemResponseData();
        ItemId itemId = new ItemId();
        itemId.setItemIdentifierValue("22334455");
        deleteItemResponseData.setItemId(itemId);
        JSONObject jsonObject = deleteItem.constructResponseNcip2Response(deleteItemResponseData);
        assertEquals(jsonObject.getString("itemId"), "22334455");
    }

    @Test
    public void testConstructNcip2Request() {
        DeleteItem deleteItem = new DeleteItem()
                .setFromAgency("ABC")
                .setToAgency("CBA")
                .setItemIdString("it-987");

        DeleteItemInitiationData initiationData = (DeleteItemInitiationData)deleteItem.generateNCIP2Object();
        assertEquals("ABC", initiationData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue());
        assertEquals("CBA", initiationData.getInitiationHeader().getToAgencyId().getAgencyId().getValue());
        assertEquals("it-987", initiationData.getItemId().getItemIdentifierValue());
    }


    @Test
    public void testToString() {
        DeleteItem deleteItem = new DeleteItem()
                .setFromAgency("ABC")
                .setToAgency("CBA")
                .setItemIdString("it-987")
                .setRegistryId("44556677");
        String asString = deleteItem.toString();
        assertTrue(asString.contains("CBA"));
        assertTrue(asString.contains("ABC"));
        assertTrue(asString.contains("it-987"));
    }
}
