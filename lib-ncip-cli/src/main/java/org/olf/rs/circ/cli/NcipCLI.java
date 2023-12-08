package org.olf.rs.circ.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

import java.io.IOException;
import org.apache.log4j.BasicConfigurator;

import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.RequestItem;

import org.olf.rs.circ.client.NCIP2WMSClient;
import org.olf.rs.circ.client.NCIPClientWrapper;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;



public class NcipCLI {

	public static void main(String[] args) throws Exception {


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

		String fromAgency = null;
		String toAgency = null;

		String apiKey = null;
		String apiSecret = null;

		String registryId = null;

		System.out.println("**Testing NCIP at endpoint " + endpoint + "**");
		
		//System.out.println("Service? 'L' - lookupUser, 'A' - acceptItem, 'O' - checkoutItem, 'I' - checkinItem");
		//String service = in.nextLine();
		String service = stringOrDie("service", inputLine);
		Map<String, Object> inputParms = new HashMap<String,Object>();
		String ncipProtocol = inputLine.getOptionValue("ncip-protocol");
		if(ncipProtocol.equals("1")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP1);
		} else if(ncipProtocol.equals("2")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP2);
		} else if(ncipProtocol.equals("SOCKET")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP1_SOCKET);
		} else if(ncipProtocol.equals("STRICTSOCKET")) {
			inputParms.put("protocol", NCIPClientWrapper.NCIP1_STRICT_SOCKET);
		} else if(ncipProtocol.equals("WMS")) {
			inputParms.put("protocol", NCIPClientWrapper.WMS);
		} else if(ncipProtocol.equals("WMS2")) {
			inputParms.put("protocol", NCIPClientWrapper.WMS2);
		} else {
			die(ncipProtocol + " is not a valid NCIP protocol");
			return;
		}

		String namespace = inputLine.getOptionValue("use-namespace");
		if("false".equals(namespace)) {
			inputParms.put("useNamespace", false);
		}
		String password = inputLine.getOptionValue("password");
		if(password != null) {
			inputParms.put("fromAgencyAuthentication", password);
		}
		System.out.println("Creating NCIPClientWrapper");
		if(ncipProtocol.equals("WMS") || ncipProtocol.equals("WMS2")) {
			apiKey = stringOrDie("api-key", inputLine);
			apiSecret = stringOrDie("api-secret", inputLine);
			registryId = stringOrDie("registry-id", inputLine);
			inputParms.put("apiSecret", apiSecret);
			inputParms.put("apiKey", apiKey);
			inputParms.put("registryId", registryId);
			if("L".equals(service)) {
				inputParms.put("lookupPatronEndpoint",
						stringOrDie("wms-patron-lookup", inputLine));
			}

		}
		NCIPClientWrapper wrapper = new NCIPClientWrapper(endpoint, inputParms);
		if (service.equalsIgnoreCase("L")) {
			fromAgency = stringOrDie("from-agency", inputLine);
		    toAgency = stringOrDie("to-agency", inputLine);
			String uid = inputLine.getOptionValue("patron-id");
			String username = inputLine.getOptionValue("username");
			if(uid == null && username == null) {
				die("You must define either patron-id or username");
			}
			if(ncipProtocol.equals("1") || ncipProtocol.equals("SOCKET") || ncipProtocol.equals("STRICTSOCKET")) {
				if(uid == null) {
					die("Only NCIP version 2 supports username lookup in the client");
				}
			}
			if(uid != null) {
				System.out.println("Lookup User by id: " + uid);
			} else {
				System.out.println("Lookup User by username: " + username);
			}
			LookupUser lookupUser = new LookupUser();
			lookupUser.setFromAgency(fromAgency);
			lookupUser.setToAgency(toAgency);
			lookupUser.setUserId(uid);
			lookupUser.setUserName(username);
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
			fromAgency = stringOrDie("from-agency", inputLine);
		    toAgency = stringOrDie("to-agency", inputLine);
			String uid = stringOrDie("patron-id", inputLine);
			String itemId = stringOrDie("item-id", inputLine);
			String requestId = inputLine.getOptionValue("request-id");
			if (requestId==null || requestId.equalsIgnoreCase("")) requestId = itemId;
			String title = stringOrDie("title", inputLine);
			String author = stringOrDie("author", inputLine);
			String pickup = stringOrDie("pickup-location", inputLine);
			AcceptItem acceptItem = new AcceptItem()
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					.setUserId(uid) 
					.setTitle(title)
					.setAuthor(author)
					.setPickupLocation(pickup)
					.setRequestActionType("Hold For Pickup")
					.setRequestId(requestId)
					.setItemId(itemId)
					.setRegistryId(registryId);
			
			String templatePrefix = inputLine.getOptionValue("template-prefix");
			if(templatePrefix != null) {
				acceptItem.setTemplatePrefix(templatePrefix);
			}		
			
			Map<String, Object> map = wrapper.send(acceptItem);
			System.out.println("RESPONSE:  " + map.toString());
			System.out.println("");
		}
		else if (service.equalsIgnoreCase("O")) {
			Map<String, Object> inputParameters = new HashMap<String,Object>();
			fromAgency = stringOrDie("from-agency", inputLine);
		    toAgency = stringOrDie("to-agency", inputLine);
			String uid = stringOrDie("patron-id", inputLine);
			String itemId = stringOrDie("item-id", inputLine);
			String requestId = stringOrDie("request-id", inputLine);
			CheckoutItem checkoutItem = new CheckoutItem()
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					.setRequestId(requestId)
					.setItemId(itemId)
					.setUserId(uid);
			Map<String, Object> map = wrapper.send(checkoutItem);
			System.out.println("RESPONSE: " + map.toString());
			System.out.println("");
		}
		else if (service.equalsIgnoreCase("I")) {
			fromAgency = stringOrDie("from-agency", inputLine);
		    toAgency = stringOrDie("to-agency", inputLine);
			String itemId = stringOrDie("item-id", inputLine);			
			CheckinItem checkinItem = new CheckinItem()
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					.setItemId(itemId);
			Map<String, Object> map = wrapper.send(checkinItem);
			System.out.println("RESPONSE: " + map.toString());
			System.out.println("");

		}
		else if(service.equalsIgnoreCase("R")) {
			fromAgency = stringOrDie("from-agency", inputLine);
			toAgency = stringOrDie("to-agency", inputLine);
			String uid = stringOrDie("patron-id", inputLine);
			String requestId = stringOrDie("request-id", inputLine);
			String bibliographicId = stringOrDie("bib-id", inputLine);
			String bibliographicIdCode = stringOrDie("bib-id-code", inputLine);
			RequestItem requestItem = new RequestItem()
					.setToAgency(toAgency)
					.setFromAgency(fromAgency)
					.setUserId(uid)
					.setRequestId(requestId)
					.setBibliographicRecordId(bibliographicId)
					.setBibliographicRecordIdCode(bibliographicIdCode);
			Map<String, Object> map = wrapper.send(requestItem);
			System.out.println("RESPONSE: " + map.toString());
			System.out.println("");
		}
		else if(service.equalsIgnoreCase("T")) {
			if(!ncipProtocol.equals("WMS")) {
				die("Must use WMS protocol");
			}
			NCIP2WMSClient WMSClient = new NCIP2WMSClient(endpoint, inputParms);
			String token = WMSClient.getToken();
			System.out.println("TOKEN: " + token);
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

	private static String stringOrDie(String valueName, CommandLine inputLine) {
		String result = inputLine.getOptionValue(valueName);
		if(result == null) {
			die("Required parameter: " + valueName);
		}
		return result;
	}

	static CommandLine handleOptions(String[] args) 
		throws IOException, ParseException {
	  Options options = new Options();

		Option fromAgency = Option.builder("f")
				.hasArg()
				.required(false)
				.desc("From Agency value")
				.longOpt("from-agency")
				.build();
		
		Option toAgency = Option.builder("t")
				.hasArg()
				.required(false)
				.desc("To Agency value")
				.longOpt("to-agency")
				.build();
		
		Option service = Option.builder("s")
				.hasArg()
				.required(false)
				.desc("The service to test: (L, A, O, I, T: WMS Token Only)")
				.longOpt("service")
				.build();
		
		Option patronId = Option.builder("p")
				.hasArg()
				.required(false)
				.desc("The patron ID for lookup")
				.longOpt("patron-id")
				.build();

		Option userName = Option.builder("n")
				.hasArgs()
				.required(false)
				.desc("The username for lookup (if used instead of id)")
				.longOpt("username")
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

		Option ncipProtocol = Option.builder("w")
				.hasArg()
				.required(false)
				.desc("The protocol of NCIP to use (1, 2, WMS, WMS2, SOCKET, STRICTSOCKET)")
				.longOpt("ncip-protocol")
				.build();

		Option appProfile = Option.builder("A")
				.hasArg()
				.required(false)
				.desc("The name of the application profile")
				.longOpt("app-profile")
				.build();

		Option apiSecret = Option.builder("S")
				.hasArg()
				.required(false)
				.desc("API Secret (WMS required)")
				.longOpt("api-secret")
				.build();

		Option apiKey = Option.builder("K")
				.hasArg()
				.required(false)
				.desc("API Key (WMS required)")
				.longOpt("api-key")
				.build();

		Option registryId = Option.builder("R")
				.hasArg()
				.required(false)
				.desc("Registry ID (WMS required)")
				.longOpt("registry-id")
				.build();

		Option prefix = Option.builder("x")
			.hasArg()
			.required(false)
			.desc("Template Prefix (currently only for NCIP1 AcceptItem)")
			.longOpt("template-prefix")
			.build();

		Option namespace = Option.builder("N")
			.hasArg()
			.required(false)
			.desc("Use namespaces in v2 XML output (default true)")
			.longOpt("use-namespace")
			.build();

		Option password = Option.builder("z")
			.hasArg()
			.required(false)
			.desc("If provided, maps to fromAgencyAuthentication")
			.longOpt("password")
			.build();

		Option wmsLookupPatronEndpoint = Option.builder("W")
			.hasArg()
			.required(false)
			.desc("The endpoint for looking up patrons used by WMS clients")
			.longOpt("wms-patron-lookup")
			.build();

		Option bibliographicId = Option.builder("b")
			.hasArg()
			.required(false)
			.desc("The Bibligraphic Record Id")
			.longOpt("bib-id")
			.build();

		Option bibliographicIdCode = Option.builder("c")
			.hasArg()
			.required(false)
			.desc("The Bibligraphic Record Id code")
			.longOpt("bib-id-code")
			.build();


		Option help = new Option("help", "print this message");

		options.addOption(fromAgency);
		options.addOption(toAgency);
		options.addOption(service);
		options.addOption(itemId);
		options.addOption(patronId);
		options.addOption(userName);
		options.addOption(requestId);
		options.addOption(title);
		options.addOption(author);
		options.addOption(pickupLocation);
		options.addOption(ncipProtocol);
		options.addOption(appProfile);
		options.addOption(apiKey);
		options.addOption(apiSecret);
		options.addOption(registryId);
		options.addOption(help);
		options.addOption(prefix);
		options.addOption(namespace);
		options.addOption(password);
		options.addOption(wmsLookupPatronEndpoint);
		options.addOption(bibliographicId);
		options.addOption(bibliographicIdCode);

		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		
		if(line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("[options] <path to NCIP endpoint>", options);
			System.exit(0);
		}

		
		return line;
				
	}

}
