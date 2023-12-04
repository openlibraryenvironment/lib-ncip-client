package org.olf.rs.circ.client;

import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;

public class RequestItem extends NCIPService implements NCIPCircTransaction {

    private String applicationProfileTypeString;
    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    private String useridString;
    private String bibliographicRecordIdString;
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
        bibliographicId.setBibliographicRecordId(bibliographicRecordId);



        return null;
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
        return null;
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
        return null;
    }

    public RequestItem setApplicationProfileType(String profileType) {
        applicationProfileTypeString = profileType;
        return this;
    }

}
