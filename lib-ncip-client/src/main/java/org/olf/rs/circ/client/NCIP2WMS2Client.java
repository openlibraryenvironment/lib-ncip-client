package org.olf.rs.circ.client;
import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.FromSystemId;
import org.apache.log4j.Logger;

import java.util.Map;

public class NCIP2WMS2Client extends NCIP2WMSClient {

  private static final Logger logger = Logger.getLogger(NCIP2WMS2Client.class);

  private static String WMS_SYSTEM_ID = "ReShare"; //Does this need to be dynamic?
  private static String WMS_SCHEME_SYSTEMID = "http://oclc.org/ncip/schemes/systemid/oclcsystemid.scm";
  
  public NCIP2WMS2Client(String endpoint, Map<String, Object> inputParams) throws NCIPClientException {
    super(endpoint, inputParams);
  }

  @Override
  protected NCIPInitiationData modifyTransactionForWMS(NCIPCircTransaction transaction, NCIPInitiationData initiationData) {
		NCIPInitiationData modifiedInitiationData = transaction.modifyForWMS(initiationData);

    if (transaction.getClass() == AcceptItem.class) {
      InitiationHeader initiationHeader = ((AcceptItemInitiationData)initiationData).getInitiationHeader();
      FromSystemId fromSystemId = new FromSystemId(WMS_SCHEME_SYSTEMID, WMS_SYSTEM_ID);
      initiationHeader.setFromSystemId(fromSystemId);


    } else if (transaction.getClass() == CheckinItem.class) {

    } else if (transaction.getClass() == CheckoutItem.class) {

    } else if (transaction.getClass() == LookupUser.class) {

    }

    return modifiedInitiationData;
	}
  
}
