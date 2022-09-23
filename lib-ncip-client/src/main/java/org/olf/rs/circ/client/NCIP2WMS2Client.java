package org.olf.rs.circ.client;
import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.FromSystemId;
import org.extensiblecatalog.ncip.v2.service.ToSystemId;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.RequestIdentifierType;
import org.extensiblecatalog.ncip.v2.service.RequestedActionType;
import org.extensiblecatalog.ncip.v2.service.UserIdentifierType;

import org.apache.log4j.Logger;

import java.util.Map;

public class NCIP2WMS2Client extends NCIP2WMSClient {

  private static final Logger logger = Logger.getLogger(NCIP2WMS2Client.class);

  private static String WMS_FROM_SYSTEM_ID = "ReShare"; //Does this need to be dynamic?
  private static String WMS_TO_SYSTEM_ID = "WMS Circ";
  private static String WMS_SCHEME_SYSTEMID = "http://oclc.org/ncip/schemes/systemid/oclcsystemid.scm";
  private static String WMS_SCHEME_AGENCYID = "http://oclc.org/ncip/schemes/agencyid/institutionid.scm";
  private static String WMS_SCHEME_APPLICATION_PROFILE_TYPE = "http://oclc.org/ncip/schemes/applicationprofiletype/circillapplicationprofiletype.scm";
  private static String WMS_SCHEME_REQUEST_IDENTIFIER_TYPE = "http://oclc.org/ncip/schemes/requestidentifiertype/circillrequestidentifiertype.scm";
  private static String WMS_SCHEME_REQUESTED_ACTION_TYPE = "http://www.oclc.org/ncip/schemes/ws-illv1_0requestedactiontype/requestedactiontype.scm";
  private static String WMS_SCHEME_USER_IDENTIFIER_TYPE = "http://oclc.org/ncip/schemes/userid/oclcids.scm";
  private static String WMS_APPLICATION_PROFILE_TYPE = "2.00";
  private static String WMS_REQUEST_IDENTIFIER_VALUE = "ILL Request Id";
  private static String WMS_REQUESTED_ACTION_TYPE_VALUE = "Hold For Fulfillment";
  private static String WMS_USER_IDENTIFIER_TYPE_VALUE = "OCLC Institution Symbol";
  
  public NCIP2WMS2Client(String endpoint, Map<String, Object> inputParams) throws NCIPClientException {
    super(endpoint, inputParams);
  }

  @Override
  protected NCIPInitiationData modifyTransactionForWMS(NCIPCircTransaction transaction, NCIPInitiationData initiationData) {
		NCIPInitiationData modifiedInitiationData = transaction.modifyForWMS(initiationData);

    if (transaction.getClass() == AcceptItem.class) {
      AcceptItemInitiationData acceptItemInitiationData = (AcceptItemInitiationData)initiationData;
      InitiationHeader initiationHeader = acceptItemInitiationData.getInitiationHeader();
      
      FromSystemId fromSystemId = new FromSystemId(WMS_SCHEME_SYSTEMID, WMS_FROM_SYSTEM_ID);
      initiationHeader.setFromSystemId(fromSystemId);

      FromAgencyId fromAgencyId = initiationHeader.getFromAgencyId();
      String fromAgency = fromAgencyId.getAgencyId().getValue();
      FromAgencyId newFromAgencyId = new FromAgencyId();
      newFromAgencyId.setAgencyId(new AgencyId(WMS_SCHEME_AGENCYID, fromAgency));
      initiationHeader.setFromAgencyId(newFromAgencyId);

      ToSystemId toSystemId = new ToSystemId(WMS_SCHEME_SYSTEMID, WMS_TO_SYSTEM_ID);
      initiationHeader.setToSystemId(toSystemId);

      ToAgencyId oldToAgencyId = initiationHeader.getToAgencyId();
      String toAgency = oldToAgencyId.getAgencyId().getValue();
      ToAgencyId newToAgencyId = new ToAgencyId();
      newToAgencyId.setAgencyId(new AgencyId(WMS_SCHEME_AGENCYID, toAgency));
      initiationHeader.setToAgencyId(newToAgencyId);

      ApplicationProfileType applicationProfileType = 
        new ApplicationProfileType(WMS_SCHEME_APPLICATION_PROFILE_TYPE, WMS_APPLICATION_PROFILE_TYPE);
      initiationHeader.setApplicationProfileType(applicationProfileType);

      RequestId oldRequestId = acceptItemInitiationData.getRequestId();
      String oldRequestIdentifierValue = oldRequestId.getRequestIdentifierValue();
      RequestIdentifierType requestIdentifierType = new RequestIdentifierType(WMS_SCHEME_REQUEST_IDENTIFIER_TYPE, WMS_REQUEST_IDENTIFIER_VALUE);
      RequestId newRequestId = new RequestId();
      String newRequestIdentifierValue = "ReShareRequest" + oldRequestIdentifierValue;
      newRequestId.setRequestIdentifierType(requestIdentifierType);
      newRequestId.setRequestIdentifierValue(newRequestIdentifierValue);
      acceptItemInitiationData.setRequestId(newRequestId);

      RequestedActionType requestedActionType = new RequestedActionType(WMS_SCHEME_REQUESTED_ACTION_TYPE, WMS_REQUESTED_ACTION_TYPE_VALUE);
      acceptItemInitiationData.setRequestedActionType(requestedActionType);

      UserId oldUserId = acceptItemInitiationData.getUserId();
      String userIdentifierValue = oldUserId.getUserIdentifierValue();
      UserIdentifierType userIdentifierType = new UserIdentifierType(WMS_SCHEME_USER_IDENTIFIER_TYPE, WMS_USER_IDENTIFIER_TYPE_VALUE);
      UserId newUserId = new UserId();
      newUserId.setUserIdentifierType(userIdentifierType);
      newUserId.setUserIdentifierValue(userIdentifierValue);
      acceptItemInitiationData.setUserId(newUserId);



    } else if (transaction.getClass() == CheckinItem.class) {

    } else if (transaction.getClass() == CheckoutItem.class) {

    } else if (transaction.getClass() == LookupUser.class) {

    }

    return modifiedInitiationData;
	}
  
}
