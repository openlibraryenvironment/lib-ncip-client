package org.olf.rs.circ.client.manualTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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

public class LookupUserTest {

	public static void main(String[] args) throws Exception {

        
		NCIP1Client client = new NCIP1Client("http://placeholder.ncip.edu:5994/ncip");
		LookupUser lookupUser = new LookupUser()
											.setUserId("N0023423423454")
											.includeUserAddressInformation()
											.includeUserPrivilege()
											.includeNameInformation()
											.setToAgency("TNS")
											.setFromAgency("RSH");
											//.setApplicationProfileType("E-ZBorrow");  not needed for new school
		JSONObject response = client.send(lookupUser);
		System.out.println(response);
		
	}

}
