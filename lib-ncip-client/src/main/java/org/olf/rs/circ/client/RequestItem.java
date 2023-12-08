package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RequestItem extends NCIPService implements NCIPCircTransaction {

    private String applicationProfileTypeString;
    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    private String useridString;
    private String bibliographicRecordIdString;
    private String bibliographicRecordIdCodeString;
    private String requestIdString;

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
        this.useridString = userId;
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
        userid.setUserIdentifierValue(useridString);

        BibliographicId bibliographicId = new BibliographicId();
        BibliographicRecordId bibliographicRecordId = new BibliographicRecordId();
        bibliographicRecordId.setBibliographicRecordIdentifier(bibliographicRecordIdString);
        bibliographicRecordId.setBibliographicRecordIdentifierCode(
                new BibliographicRecordIdentifierCode(null, bibliographicRecordIdCodeString));
        bibliographicId.setBibliographicRecordId(bibliographicRecordId);

        RequestId requestId = new RequestId();
        requestId.setAgencyId(new AgencyId(fromAgency));
        requestId.setRequestIdentifierValue(requestIdString);

        RequestType requestType = new RequestType(null, "Loan");

        RequestScopeType requestScopeType = new RequestScopeType(null, "Bibliographic Item");

        List<BibliographicId> bibIdList = new ArrayList<>();
        bibIdList.add(bibliographicId);
        requestItemInitiationData.setBibliographicIds(bibIdList);
        requestItemInitiationData.setUserId(userid);
        requestItemInitiationData.setRequestId(requestId);
        requestItemInitiationData.setInitiationHeader(initiationHeader);
        requestItemInitiationData.setRequestType(requestType);
        requestItemInitiationData.setRequestScopeType(requestScopeType);

        return requestItemInitiationData;

    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        return null;
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        return null;
    }

    @Override
    public String generateNCIP1Object() {
        return generateNCIP1Object("/templates/requestItem.hbs");
    }

    @Override
    public JSONObject constructResponseNcip1Response(String responseData) {
        return null;
    }

    @Override
    public JSONObject validateRequest() {
        return null;
    }

    @Override
    public JSONObject constructWMSResponse(JSONObject responseJson) {
        throw new NotImplementedException();
    }

}
