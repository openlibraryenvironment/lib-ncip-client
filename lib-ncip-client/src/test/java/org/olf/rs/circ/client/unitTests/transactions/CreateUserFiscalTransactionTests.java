package org.olf.rs.circ.client.unitTests.transactions;

import org.extensiblecatalog.ncip.v2.service.CreateUserFiscalTransactionInitiationData;
import org.extensiblecatalog.ncip.v2.service.CreateUserFiscalTransactionResponseData;
import org.extensiblecatalog.ncip.v2.service.DeleteItemResponseData;
import org.extensiblecatalog.ncip.v2.service.FiscalTransactionReferenceId;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONObject;
import org.junit.Test;
import org.olf.rs.circ.client.CreateUserFiscalTransaction;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CreateUserFiscalTransactionTests {

    @Test
    public void testConstructNcip2Response() {
        CreateUserFiscalTransaction createUserFiscalTransaction = new CreateUserFiscalTransaction();
        CreateUserFiscalTransactionResponseData responseData = new CreateUserFiscalTransactionResponseData();
        UserId userId = new UserId();
        userId.setUserIdentifierValue("123");
        FiscalTransactionReferenceId fiscalTransactionReferenceId = new FiscalTransactionReferenceId();
        fiscalTransactionReferenceId.setFiscalTransactionIdentifierValue("f123");
        responseData.setFiscalTransactionReferenceId(fiscalTransactionReferenceId);
        responseData.setUserId(userId);
        JSONObject jsonObject = createUserFiscalTransaction.constructResponseNcip2Response(responseData);
        assertEquals(jsonObject.getString("userUuid"), "123");
        assertEquals(jsonObject.getString("feeUuid"), "f123");
    }

    @Test
    public void testConstructNcip2ResponseProblem() {
        CreateUserFiscalTransaction createUserFiscalTransaction = new CreateUserFiscalTransaction();
        CreateUserFiscalTransactionResponseData responseData = new CreateUserFiscalTransactionResponseData();
        Problem problem = new Problem(new ProblemType("p1"), "p2", "p3");
        responseData.setProblems(new ArrayList<>());
        responseData.getProblems().add(problem);
        JSONObject jsonObject = createUserFiscalTransaction.constructResponseNcip2Response(responseData);
        assertEquals(jsonObject.getJSONArray("problems").get(0).toString(), "{\"type\":\"p1\",\"value\":\"p3\",\"element\":\"p2\"}");
    }

    @Test
    public void testConstructNcip2ResponseIncorrect() {
        CreateUserFiscalTransaction createUserFiscalTransaction = new CreateUserFiscalTransaction();
        DeleteItemResponseData responseData = new DeleteItemResponseData();
        Problem problem = new Problem(new ProblemType("p1"), "p2", "p3");
        responseData.setProblems(new ArrayList<>());
        responseData.getProblems().add(problem);
        JSONObject jsonObject = createUserFiscalTransaction.constructResponseNcip2Response(responseData);
        assertEquals(jsonObject.getJSONArray("problems").get(0).toString(), "{\"type\":\"p1\",\"value\":\"p3\",\"element\":\"p2\"}");
    }

    @Test
    public void generateNCIP2Object() {
        CreateUserFiscalTransaction createUserFiscalTransaction = new CreateUserFiscalTransaction()
                .setFromAgency("ABC")
                .setToAgency("CBA")
                .setUserId("123")
                .setChargeDefaultPatronFee(true)
                .setRegistryId("R1")
                .setItemId("i123");

        CreateUserFiscalTransactionInitiationData initiationData = (CreateUserFiscalTransactionInitiationData)
                createUserFiscalTransaction.generateNCIP2Object();
        assertEquals("ABC", initiationData.getInitiationHeader().getFromAgencyId().getAgencyId().getValue());
        assertEquals("CBA", initiationData.getInitiationHeader().getToAgencyId().getAgencyId().getValue());
        assertEquals("123", initiationData.getUserId().getUserIdentifierValue());
        assertEquals("charge-default-patron-fee", initiationData.getFiscalTransactionInformation().getFiscalActionType().getValue());
        assertEquals("i123", initiationData.getFiscalTransactionInformation().getItemDetails().getItemId().getItemIdentifierValue());
    }
}
