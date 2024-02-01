package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class RequestItem extends NCIPService implements NCIPCircTransaction {

    private static final Logger logger = Logger.getLogger(RequestItem.class);
    private String applicationProfileTypeString;
    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    private String userIdString;
    private String bibliographicRecordIdString;
    private String bibliographicRecordIdCodeString;
    private String requestIdString;
    private String itemIdString;

    public RequestItem setRegistryId(String id) {
        this.registryId = id;
        return this;
    }

    public RequestItem setToAgency(String toAgency) {
        this.toAgency = toAgency;
        return this;
    }

    public RequestItem setFromAgency(String fromAgency) {
        this.fromAgency = fromAgency;
        return this;
    }

    public RequestItem setUserId(String userId) {
        this.userIdString = userId;
        return this;
    }

    public RequestItem setBibliographicRecordId(String bibId) {
        this.bibliographicRecordIdString = bibId;
        return this;
    }

    public RequestItem setBibliographicRecordIdCode(String bibIdCode) {
        this.bibliographicRecordIdCodeString = bibIdCode;
        return this;
    }

    public RequestItem setRequestId(String requestId) {
        this.requestIdString = requestId;
        return this;
    }

    public RequestItem setApplicationProfileType(String profileType) {
        applicationProfileTypeString = profileType;
        return this;
    }

    public RequestItem setItemId(String itemIdString) {
        this.itemIdString = itemIdString;
        return this;
    }

    @Override
    public NCIPInitiationData generateNCIP2Object() {
        RequestItemInitiationData requestItemInitiationData = new RequestItemInitiationData();
        InitiationHeader initiationHeader = new InitiationHeader();
        ApplicationProfileType applicationProfileType = new ApplicationProfileType(
                null, applicationProfileTypeString);
        requestItemInitiationData.setInitiationHeader(initiationHeader);
        ToAgencyId toAgencyId = new ToAgencyId();
        toAgencyId.setAgencyId(new AgencyId(toAgency));
        FromAgencyId fromAgencyId = new FromAgencyId();
        fromAgencyId.setAgencyId(new AgencyId(fromAgency));
        initiationHeader.setToAgencyId(toAgencyId);
        initiationHeader.setFromAgencyId(fromAgencyId);

        UserId userid = new UserId();
        userid.setAgencyId(new AgencyId(fromAgency));
        userid.setUserIdentifierValue(userIdString);

        /* If we have a bibliographic id set, use that, otherwise use the item id */
        if (bibliographicRecordIdString != null) {
            BibliographicId bibliographicId = new BibliographicId();
            BibliographicRecordId bibliographicRecordId = new BibliographicRecordId();
            bibliographicRecordId.setBibliographicRecordIdentifier(bibliographicRecordIdString);
            bibliographicRecordId.setBibliographicRecordIdentifierCode(
                    new BibliographicRecordIdentifierCode(null, bibliographicRecordIdCodeString));
            bibliographicId.setBibliographicRecordId(bibliographicRecordId);
            List<BibliographicId> bibIdList = new ArrayList<>();
            bibIdList.add(bibliographicId);
            requestItemInitiationData.setBibliographicIds(bibIdList);
        } else if (itemIdString != null) {
            ItemId itemId = new ItemId();
            itemId.setAgencyId(new AgencyId(fromAgency));
            itemId.setItemIdentifierValue(itemIdString);
            List<ItemId> itemIdList = new ArrayList<>();
            itemIdList.add(itemId);
            requestItemInitiationData.setItemIds(itemIdList);
        }

        RequestId requestId = new RequestId();
        requestId.setAgencyId(new AgencyId(fromAgency));
        requestId.setRequestIdentifierValue(requestIdString);

        RequestType requestType = new RequestType(null, "Loan");

        RequestScopeType requestScopeType = new RequestScopeType(null, "Bibliographic Item");

        requestItemInitiationData.setUserId(userid);
        requestItemInitiationData.setRequestId(requestId);
        requestItemInitiationData.setInitiationHeader(initiationHeader);
        requestItemInitiationData.setRequestType(requestType);
        requestItemInitiationData.setRequestScopeType(requestScopeType);

        return requestItemInitiationData;

    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        RequestItemResponseData requestItemResponseData = null;
        try {
            requestItemResponseData = (RequestItemResponseData)responseData;
            if (requestItemResponseData.getProblems() != null && requestItemResponseData.getProblems().size() > 0) {
                return constructProblem(responseData);
            }
        }
        catch(ClassCastException e) {
            return constructProblem(responseData);
        }
        JSONObject returnJson = new JSONObject();
        returnJson.put("itemId", requestItemResponseData.getItemId().getItemIdentifierValue());
        returnJson.put("requestId", requestItemResponseData.getRequestId().getRequestIdentifierValue());

        return returnJson;
    }

    @Override
    public String generateNCIP1Object() {
        return generateNCIP1Object("/templates/requestItem");
    }

    @Override
    public JSONObject constructResponseNcip1Response(String responseData) {
        JSONObject returnJson = new JSONObject();
        try {
            Document document = Jsoup.parse(responseData, "", Parser.xmlParser());
            Elements problems = document.select("NCIPMessage > RequestItemResponse > Problem");
            if (problems != null && !problems.isEmpty()) {
                return constructeNcipOneProblems(problems);
            }
            String itemId = document.select(
                    "NCIPMessage > RequestItemResponse > UniqueItemId > ItemIdentifierValue").text();
            String requestId = document.select(
                    "NCIPMessage > RequestItemResponse > UniqueRequestId > RequestIdentifiervalue").text();
            returnJson.put("itemId", itemId);
            returnJson.put("requestId", requestId);
        } catch(Exception e) {
            logger.fatal("Failed to parse the NCIP XML Response: " + responseData);
            logger.fatal(e.getLocalizedMessage());
            throw e;
        }
        return returnJson;
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
