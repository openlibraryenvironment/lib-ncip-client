package org.olf.rs.circ.client;

import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.json.JSONObject;

public interface NCIPCircTransaction {

	NCIPInitiationData generateNCIP2Object();
	JSONObject constructResponseNcip2Response(NCIPResponseData responseData);
	String generateNCIP1Object();
	JSONObject constructResponseNcip1Response(String responseData);
	JSONObject validateRequest();
	

	
	
}
