package org.olf.rs.circ.cli;

//import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONObject;
import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIP1Client;
import org.olf.rs.circ.client.NCIP2Client;
import org.olf.rs.circ.client.NCIPClientWrapper;

//import jdk.internal.jshell.tool.resources.l10n;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;



public class NcipCLI {

	public static void main(String[] args) throws Exception {

		//final String endpoint = "https://35.171.72.85:443/iii/nciprelais/Restful";
	  //final String fromAgency = "reshare";
		
		//final String toAgency = "duquesne";

	  final Logger logger = Logger.getLogger(NcipCLI.class.getName());
	
		BasicConfigurator.configure();
		
		//Scanner in = new Scanner(System.in);
		CommandLine inputLine = handleOptions(args);
		List<String> cliArgs = inputLine.getArgList();

		String endpoint;

		try {
			endpoint = cliArgs.get(0);
		} catch(Exception e) {
			System.out.println("Unable to read value for NCIP endpoint");
			System.exit(1);
			return;
		}

		if(endpoint == null || endpoint.isEmpty()) {
			System.out.println("An NCIP endpoint must be provided");
			System.exit(1);
		}
		
		String fromAgency = inputLine.getOptionValue("from-agency");
		String toAgency = inputLine.getOptionValue("to-agency");

		System.out.println("**Testing NCIP at endpoint " + endpoint + "**");
		
		//System.out.println("Service? 'L' - lookupUser, 'A' - acceptItem, 'O' - checkoutItem, 'I' - checkinItem");
		//String service = in.nextLine();
		String service = inputLine.getOptionValue("service");
		Map<String, Object> inputParms = new HashMap<String,Object>();
		String ncipVersion = inputLine.getOptionValue("ncip-version");
		if(ncipVersion.equals("1")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		} else if(ncipVersion.equals("2")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP2);
		} else {
			die(ncipVersion + " is not a valid NCIP version");
			return;
		}
		//inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		//inputParms.put("protocol", NCIPClientWrapper.NCIP2);
		inputParms.put("useNamespace", false);
		System.out.println("Creating NCIPClientWrapper");
		//NCIPClientWrapper wrapper = new NCIPClientWrapper("https://eastern.tlcdelivers.com:8467/ncipServlet/NCIPResponder", inputParms);
		NCIPClientWrapper wrapper = new NCIPClientWrapper(endpoint, inputParms);
		if (service.equalsIgnoreCase("L")) {
			//System.out.println("Patron ID?");
			//String uid = in.nextLine();
			String uid = inputLine.getOptionValue("patron-id");
			if(uid == null) {
				die("Required parameter: patron-id (p)");
				return;
			}
			System.out.println("Lookup User: " + uid);
			LookupUser lookupUser = new LookupUser();
			//lookupUser.setFromAgency("Relias");
			lookupUser.setFromAgency(fromAgency);
			//lookupUser.setToAgency("EUL");
			lookupUser.setToAgency(toAgency);
			lookupUser.setUserId(uid);
			lookupUser.includeUserAddressInformation();
			lookupUser.includeNameInformation();
			lookupUser.includeUserPrivilege();
			String appProfile = inputLine.getOptionValue("app-profile");
			if(appProfile != null) {
				lookupUser.setApplicationProfileType(appProfile);
			}

			System.out.println("Sending lookup");
			Map<String, Object> map = wrapper.send(lookupUser);
			System.out.println("RESPONSE:  " + map.toString());
			System.out.println("");	
		}
		else if (service.equalsIgnoreCase("A")) {
			//System.out.println("Patron ID?");
			//String uid = in.nextLine();
			String uid = inputLine.getOptionValue("patron-id");
			if(uid == null) {
				die("Required parameter: patron-id (p)");
			}
			//System.out.println("Item ID?");
			//String itemId = in.nextLine();
			String itemId = inputLine.getOptionValue("item-id");
			if(itemId == null) {
				die("Required parameter: item-id (i)");
			}
			//System.out.println("Request ID? (press enter to use: " + itemId + ")");
			//String requestId = in.nextLine();
			String requestId = inputLine.getOptionValue("request-id");
			if (requestId==null || requestId.equalsIgnoreCase("")) requestId = itemId;
			//System.out.println("Title?");
			//String title = in.nextLine();
			String title = inputLine.getOptionValue("title");
			if(title == null) {
				die("Required parameter: title (T)");
			}
			//System.out.println("author?");
			//String author = in.nextLine();
			String author = inputLine.getOptionValue("author");
			if(author == null) {
				die("Required parameter: author (a)");
			}
			//System.out.println("Pickup Location?");
			//String pickup = in.nextLine();
			String pickup = inputLine.getOptionValue("pickup-location");
			if(pickup == null) {
				die("Required parameter: pickup-location (P)");
			}
			AcceptItem acceptItem = new AcceptItem()
					//.setToAgency("EUL")
					.setToAgency(toAgency)
					//.setFromAgency("Relais")
					.setFromAgency(fromAgency)
					.setUserId(uid) 
					.setTitle(title)
					.setAuthor(author)
					.setPickupLocation(pickup)
					.setRequestActionType("Hold For Pickup")
					.setRequestId(requestId)
					.setItemId(itemId);
			Map<String, Object> map = wrapper.send(acceptItem);
			System.out.println("RESPONSE:  " + map.toString());
			System.out.println("");
		}
		else if (service.equalsIgnoreCase("O")) {
			Map<String, Object> inputParameters = new HashMap<String,Object>();
			inputParameters.put("useNamespace", false);
			//NCIP1Client client = new NCIP1Client(" https://eastern.tlcdelivers.com:8467/ncipServlet/NCIPResponder",ip);
			NCIP2Client client = new NCIP2Client(endpoint, inputParameters);
			//System.out.println("Patron ID?");
			//String uid = in.nextLine();
			String uid = inputLine.getOptionValue("patron-id");
			if(uid == null) {
				die("Required parameter: patron-id (p)");
			}
			//System.out.println("Item ID?");
			//String itemId = in.nextLine();
			String itemId = inputLine.getOptionValue("item-id");
			if(itemId == null) {
				die("Required parameter: item-id (i)");
			}
			//System.out.println("Request ID?");
			//String requestId = in.nextLine();
			String requestId = inputLine.getOptionValue("request-id");
			if(requestId == null) {
				die("Required parameter: request-id (r)");
			}
			CheckoutItem checkoutItem = new CheckoutItem()
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					//.setToAgency("EUL")
					//.setFromAgency("Relais")
					.setRequestId(requestId)
					.setItemId(itemId)
					.setUserId(uid);
			JSONObject map = client.send(checkoutItem);
			System.out.println("RESPONSE: " + map.toString());
			System.out.println("");
		}
		else if (service.equalsIgnoreCase("I")) {
			//System.out.println("Item ID?");
			//String itemId = in.nextLine();
			String itemId = inputLine.getOptionValue("item-id");
			if(itemId == null) {
				die("Required parameter: item-id (i)");
			}
			CheckinItem checkinItem = new CheckinItem()
					//.setToAgency("EUL")
					//.setFromAgency("Relais")
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					.setItemId(itemId);
			Map<String, Object> map = wrapper.send(checkinItem);
			System.out.println("RESPONSE: " + map.toString());
			System.out.println("");

		}
		else {
			die("Finished - not a valid selection");
		}
		
		//in.close();
	}

	private static void die(String reason) {
		System.out.println(reason);
		System.exit(1);
	}

	static CommandLine handleOptions(String[] args) 
		throws IOException, ParseException {
	  Options options = new Options();

		Option fromAgency = Option.builder("f")
				.hasArg()
				.required(true)
				.desc("From Agency value")
				.longOpt("from-agency")
				.build();
		
		Option toAgency = Option.builder("t")
				.hasArg()
				.required(true)
				.desc("To Agency value")
				.longOpt("to-agency")
				.build();
		
		Option service = Option.builder("s")
				.hasArg()
				.required(true)
				.desc("The service to test")
				.longOpt("service")
				.build();
		
		Option patronId = Option.builder("p")
				.hasArg()
				.required(false)
				.desc("The patron ID for lookup")
				.longOpt("patron-id")
				.build();

		Option itemId = Option.builder("i")
				.hasArg()
				.required(false)
				.desc("The item ID for lookup")
				.longOpt("item-id")
				.build();
		
		Option requestId = Option.builder("r")
				.hasArg()
				.required(false)
				.desc("The request ID for lookup")
				.longOpt("request-id")
				.build();

		Option title = Option.builder("T")
				.hasArg()
				.required(false)
				.desc("The title of the item")
				.longOpt("title")
				.build();

		Option author = Option.builder("a")
				.hasArg()
				.required(false)
				.desc("The author of the item")
				.longOpt("author")
				.build();
		
		Option pickupLocation = Option.builder("P")
				.hasArg()
				.required(false)
				.desc("The pickup location for the item")
				.longOpt("pickup-location")
				.build();

		Option ncipVersion = Option.builder("v")
				.hasArg()
				.required(true)
				.desc("The version of NCIP to use")
				.longOpt("ncip-version")
				.build();

		Option appProfile = Option.builder("A")
				.hasArg()
				.required(false)
				.desc("The name of the application profile")
				.longOpt("app-profile")
				.build();

		options.addOption(fromAgency);
		options.addOption(toAgency);
		options.addOption(service);
		options.addOption(itemId);
		options.addOption(patronId);
		options.addOption(requestId);
		options.addOption(title);
		options.addOption(author);
		options.addOption(pickupLocation);
		options.addOption(ncipVersion);
		options.addOption(appProfile);

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		

		
		return line;
				
	}

}
