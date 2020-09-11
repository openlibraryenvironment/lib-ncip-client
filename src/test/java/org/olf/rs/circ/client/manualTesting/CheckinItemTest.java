package org.olf.rs.circ.client.manualTesting;

import java.io.IOException;
import java.net.URISyntaxException;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONObject;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.FolioNcipClient;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;

public class CheckinItemTest {

	public static void main(String[] args) throws Exception {
		
		NCIP1Client client = new NCIP1Client("http://placeholder.ncip.edu:5994/ncip");
		//NCIP2Client ncip2Client = new NCIP2Client("http://localhost:8080/ncip");
		CheckinItem checkinItem = new CheckinItem()
						.setItemId("31207063743771")
						.setToAgency("TNS")
						.setFromAgency("RSH")
						.includeBibliographicDescription();
						//.setApplicationProfileType("EZBORROW");
		JSONObject response = client.send(checkinItem);
		System.out.println(response);
		
	}

}
