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
		String endpoint = cliArgs.get(0);

		if(endpoint == null || endpoint.isEmpty()) {
			throw new Exception("An NCIP endpoint must be provided");
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
			throw new Exception(ncipVersion + " is not a valid version");
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
				throw new Exception("Required parameter: patron-id");
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
				throw new Exception("Required parameter: patron-id");
			}
			//System.out.println("Item ID?");
			//String itemId = in.nextLine();
			String itemId = inputLine.getOptionValue("item-id");
			if(itemId == null) {
				throw new Exception("Required parameter: item-id");
			}
			//System.out.println("Request ID? (press enter to use: " + itemId + ")");
			//String requestId = in.nextLine();
			String requestId = inputLine.getOptionValue("request-id");
			if (requestId==null || requestId.equalsIgnoreCase("")) requestId = itemId;
			//System.out.println("Title?");
			//String title = in.nextLine();
			String title = inputLine.getOptionValue("title");
			if(title == null) {
				throw new Exception("Required parameter: title");
			}
			//System.out.println("author?");
			//String author = in.nextLine();
			String author = inputLine.getOptionValue("author");
			if(author == null) {
				throw new Exception("Required parameter: author");
			}
			//System.out.println("Pickup Location?");
			//String pickup = in.nextLine();
			String pickup = inputLine.getOptionValue("pickup-location");
			if(pickup == null) {
				throw new Exception("Required parameter: pickup-location");
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
				throw new Exception("Required parameter: patron-id");
			}
			//System.out.println("Item ID?");
			//String itemId = in.nextLine();
			String itemId = inputLine.getOptionValue("item-id");
			if(itemId == null) {
				throw new Exception("Required parameter: item-id");
			}
			//System.out.println("Request ID?");
			//String requestId = in.nextLine();
			String requestId = inputLine.getOptionValue("request-id");
			if(requestId == null) {
				throw new Exception("Required parameter: request-id");
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
				throw new Exception("Required parameter: item-id");
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
			System.out.println("Finished - not a valid selection");
		}
		
		//in.close();
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

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		

		
		return line;
				
	}

}
