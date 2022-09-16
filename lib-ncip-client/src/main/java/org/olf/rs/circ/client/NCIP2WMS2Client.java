package org.olf.rs.circ.client;

import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.apache.log4j.Logger;

import java.util.Map;

public class NCIP2WMS2Client extends NCIP2WMSClient {

  private static final Logger logger = Logger.getLogger(NCIP2WMS2Client.class);
  
  public NCIP2WMS2Client(String endpoint, Map<String, Object> inputParams) throws NCIPClientException {
    super(endpoint, inputParams);
  }

  @Override
  protected NCIPInitiationData modifyTransactionForWMS(NCIPCircTransaction transaction, NCIPInitiationData initiationData) {
		NCIPInitiationData modifiedInitiationData = transaction.modifyForWMS(initiationData);

    if (transaction.getClass() == AcceptItem.class) {

    } else if (transaction.getClass() == CheckinItem.class) {

    } else if (transaction.getClass() == CheckoutItem.class) {

    } else if (transaction.getClass() == LookupUser.class) {

    }

    return modifiedInitiationData;
	}
  
}
