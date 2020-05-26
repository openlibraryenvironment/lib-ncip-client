package manualTesting;

import java.io.IOException;
import java.net.URISyntaxException;

import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONObject;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.FolioNcipClient;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;

public class AcceptItemTest {

	public static void main(String[] args) throws Exception {


		NCIP1Client client = new NCIP1Client("http://placeholder.ncip.edu:5994/ncip");
		AcceptItem acceptItem = new AcceptItem()
											.setItemId("NYU-20200512106")
											.setRequestId("NYU-20200512106")
											.setUserId("21207101074550")
											.setAuthor("Jane Doe") 
											.setTitle("one fish, two fish")
											.setCallNumber("500.abc")
											.setToAgency("TNS")
											.setFromAgency("RSH");
											//.setApplicationProfileType("EZBORROW");
		JSONObject response = client.send(acceptItem);
		System.out.println(response);
		
	}
}


