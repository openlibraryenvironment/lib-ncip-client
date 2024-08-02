package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.DeleteItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.DeleteItemResponseData;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.json.JSONObject;

public class DeleteItem  extends NCIPService implements NCIPCircTransaction {

    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    protected String itemIdString;

    public DeleteItem setRegistryId(String registryId) {
        this.registryId = registryId;
        return this;
    }

    public DeleteItem setToAgency(String toAgency) {
        this.toAgency = toAgency;
        return this;
    }

    public DeleteItem setFromAgency(String fromAgency) {
        this.fromAgency = fromAgency;
        return this;
    }

    public DeleteItem setItemIdString(String itemIdString) {
        this.itemIdString = itemIdString;
        return this;
    }

    @Override
    public NCIPInitiationData generateNCIP2Object() {
        DeleteItemInitiationData initiationData = new DeleteItemInitiationData();
        InitiationHeader initiationHeader = new InitiationHeader();

        ToAgencyId toAgencyId = new ToAgencyId();
        toAgencyId.setAgencyId(new AgencyId(toAgency));
        FromAgencyId fromAgencyId = new FromAgencyId();
        fromAgencyId.setAgencyId(new AgencyId(fromAgency));
        initiationHeader.setToAgencyId(toAgencyId);
        initiationHeader.setFromAgencyId(fromAgencyId);

        ItemId itemId = new ItemId();
        itemId.setAgencyId(new AgencyId(fromAgency));
        itemId.setItemIdentifierValue(itemIdString);

        initiationData.setInitiationHeader(initiationHeader);
        initiationData.setItemId(itemId);
        return initiationData;
    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        DeleteItemResponseData deleteItemResponseData;
        try {
            deleteItemResponseData = (DeleteItemResponseData)responseData;
            if (deleteItemResponseData.getProblems() != null && !deleteItemResponseData.getProblems().isEmpty()) {
                return constructProblem(responseData);
            }
        } catch(ClassCastException e) {
            return constructProblem(responseData);
        }
        JSONObject returnJson = new JSONObject();
        if (deleteItemResponseData.getItemId() != null) {
            returnJson.put("itemId", deleteItemResponseData.getItemId().getItemIdentifierValue());
        }
        return returnJson;
    }

    @Override
    public String generateNCIP1Object() {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip1Response(String responseData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject validateRequest() {
        return null;
    }

    @Override
    public JSONObject constructWMSResponse(JSONObject responseJson) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
