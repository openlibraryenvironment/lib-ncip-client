package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.Amount;
import org.extensiblecatalog.ncip.v2.service.CreateUserFiscalTransactionInitiationData;
import org.extensiblecatalog.ncip.v2.service.CreateUserFiscalTransactionResponseData;
import org.extensiblecatalog.ncip.v2.service.FiscalActionType;
import org.extensiblecatalog.ncip.v2.service.FiscalTransactionInformation;
import org.extensiblecatalog.ncip.v2.service.FiscalTransactionType;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONObject;

import java.math.BigDecimal;

public class CreateUserFiscalTransaction extends NCIPService implements NCIPCircTransaction {
    protected String registryId; //WMS ONLY
    protected String toAgency;
    protected String fromAgency;
    protected String useridString;
    protected boolean chargeDefaultPatronFee;

    public CreateUserFiscalTransaction setRegistryId(String registryId) {
        this.registryId = registryId;
        return this;
    }

    public CreateUserFiscalTransaction setToAgency(String toAgency) {
        this.toAgency = toAgency;
        return this;
    }

    public CreateUserFiscalTransaction setFromAgency(String fromAgency) {
        this.fromAgency = fromAgency;
        return this;
    }

    public CreateUserFiscalTransaction setUseridString(String useridString) {
        this.useridString = useridString;
        return this;
    }

    public CreateUserFiscalTransaction setChargeDefaultPatronFee(boolean chargeDefaultPatronFee) {
        this.chargeDefaultPatronFee = chargeDefaultPatronFee;
        return this;
    }

    @Override
    public NCIPInitiationData generateNCIP2Object() {
        CreateUserFiscalTransactionInitiationData initiationData = new CreateUserFiscalTransactionInitiationData();
        InitiationHeader initiationHeader = new InitiationHeader();
        FromAgencyId fromAgencyId = new FromAgencyId();
        fromAgencyId.setAgencyId(new AgencyId(fromAgency));
        ToAgencyId toAgencyId = new ToAgencyId();
        toAgencyId.setAgencyId(new AgencyId(toAgency));
        initiationHeader.setToAgencyId(toAgencyId);
        initiationHeader.setFromAgencyId(fromAgencyId);

        UserId userId = new UserId();
        userId.setAgencyId(new AgencyId(fromAgency));
        userId.setUserIdentifierValue(useridString);

        initiationData.setInitiationHeader(initiationHeader);
        initiationData.setUserId(userId);

        if (chargeDefaultPatronFee) {
            FiscalTransactionInformation fiscalTransactionInformation = getFiscalTransactionInformation();
            initiationData.setFiscalTransactionInformation(fiscalTransactionInformation);
        }

        return initiationData;
    }

    private static FiscalTransactionInformation getFiscalTransactionInformation() {
        Amount amount = new Amount();
        amount.setMonetaryValue(BigDecimal.ZERO);
        FiscalTransactionInformation fiscalTransactionInformation = new FiscalTransactionInformation();
        fiscalTransactionInformation.setAmount(amount);
        fiscalTransactionInformation.setFiscalActionType(new FiscalActionType(Constants.SCHEME, Constants.CHARGE_DEFAULT_PATRON_FEE));
        fiscalTransactionInformation.setFiscalTransactionType(new FiscalTransactionType(Constants.SCHEME, Constants.ILL_FEE));
        return fiscalTransactionInformation;
    }

    @Override
    public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
        throw new NotImplementedException();
    }

    @Override
    public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
        CreateUserFiscalTransactionResponseData transResponseData;
        try {
            transResponseData = (CreateUserFiscalTransactionResponseData)responseData;
            if (transResponseData.getProblems() != null && !transResponseData.getProblems().isEmpty()) {
                return constructProblem(responseData);
            }
        } catch(ClassCastException e) {
            return constructProblem(responseData);
        }
        JSONObject returnJson = new JSONObject();
        if (transResponseData.getUserId() != null) {
            returnJson.put("userId", transResponseData.getUserId().getUserIdentifierValue());
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
