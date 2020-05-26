package org.olf.rs.circ.client.manualTesting;

import java.io.IOException;
import java.net.URISyntaxException;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONObject;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.FolioNcipClient;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;

public class CheckoutItemTest {

	public static void main(String[] args) throws Exception {

		NCIP1Client client = new NCIP1Client("http://placeholder.ncip.edu:5994/ncip");
		CheckoutItem checkoutItem = new CheckoutItem()
											.setUserId("21207101074550")
											.setItemId("100556447-10")
											.setRequestId("NYU-202005041220")
											.setToAgency("TNS")
											.setFromAgency("RSH");
											//.setApplicationProfileType("EZBORROW");
		JSONObject response = client.send(checkoutItem);
		System.out.println(response);
		
		
		
		
	}

}
