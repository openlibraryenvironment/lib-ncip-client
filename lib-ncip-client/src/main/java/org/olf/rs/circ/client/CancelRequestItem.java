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

public class CancelRequestItem extends NCIPService implements NCIPCircTransaction {

    private static final Logger logger = Logger.getLogger(CancelRequestItem.class);
    private String applicationProfileTypeString;
    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    protected String userIdString;
    protected String requestIdString;

    public CancelRequestItem setToAgency(String toAgency) {
        this.toAgency = toAgency;
        return this;
    }

    public CancelRequestItem setFromAgency(String fromAgency) {
        this.fromAgency = fromAgency;
        return this;
    }

    public CancelRequestItem setUserId(String userId) {
        this.userIdString = userId;
        return this;
    }

    public CancelRequestItem setRequestId(String requestId) {
        this.requestIdString = requestId;
        return this;
    }

    @Override
    public NCIPInitiationData generateNCIP2Object() {
        CancelRequestItemInitiationData cancelRequestItemInitiationData = new CancelRequestItemInitiationData();
        InitiationHeader initiationHeader = new InitiationHeader();
        ApplicationProfileType applicationProfileType
                = new ApplicationProfileType(null, applicationProfileTypeString);

        ToAgencyId toAgencyId = new ToAgencyId();
        toAgencyId.setAgencyId(new AgencyId(toAgency));
        FromAgencyId fromAgencyId = new FromAgencyId();
        fromAgencyId.setAgencyId(new AgencyId(fromAgency));
        initiationHeader.setToAgencyId(toAgencyId);
        initiationHeader.setFromAgencyId(fromAgencyId);

        UserId userId = new UserId();
        userId.setAgencyId(new AgencyId(fromAgency));
        userId.setUserIdentifierValue(userIdString);

        RequestId requestId = new RequestId();
        requestId.setAgencyId(new AgencyId(fromAgency));
        requestId.setRequestIdentifierValue(requestIdString);

        cancelRequestItemInitiationData.setUserId(userId);
        cancelRequestItemInitiationData.setRequestId(requestId);
        cancelRequestItemInitiationData.setInitiationHeader(initiationHeader);

        return cancelRequestItemInitiationData;

    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        CancelRequestItemResponseData cancelRequestItemResponseData = null;
        try {
            cancelRequestItemResponseData = (CancelRequestItemResponseData)responseData;
            if (cancelRequestItemResponseData.getProblems() != null && cancelRequestItemResponseData.getProblems().size() > 0) {
                return constructProblem(responseData);
            }
        }
        catch(ClassCastException e) {
            return constructProblem(responseData);
        }
        JSONObject returnJson = new JSONObject();
        returnJson.put("itemId", cancelRequestItemResponseData.getItemId().getItemIdentifierValue());
        returnJson.put("requestId", cancelRequestItemResponseData.getRequestId().getRequestIdentifierValue());

        return returnJson;
    }

    @Override
    public String generateNCIP1Object() {
        return generateNCIP1Object("/templates/cancelRequestItem");
    }

    @Override
    public JSONObject constructResponseNcip1Response(String responseData) {
        JSONObject returnJson = new JSONObject();
        try {
            Document document = Jsoup.parse(responseData, "", Parser.xmlParser());
            Elements problems = document.select("NCIPMessage > CancelRequestItemResponse > Problem");
            if (problems != null && !problems.isEmpty()) {
                return constructeNcipOneProblems(problems);
            }
            String itemId = document.select(
                    "NCIPMessage > CancelRequestItemResponse > UniqueItemId > ItemIdentifierValue").text();
            String requestId = document.select(
                    "NCIPMessage > CancelRequestItemResponse > UniqueRequestId > RequestIdentifiervalue").text();
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
