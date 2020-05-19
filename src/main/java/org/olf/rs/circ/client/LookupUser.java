package org.olf.rs.circ.client;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserResponseData;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.StructuredAddress;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.UserAddressInformation;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserPrivilege;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;



public class LookupUser extends NCIPService implements NCIPCircTransaction {
	
	private static final Logger logger = Logger.getLogger(LookupUser.class);
	protected String toAgency;
	protected String fromAgency;
	private String useridString;
	List<String> userElementTypes = new ArrayList<>();
	private String applicationProfileTypeString;


	public LookupUser() {
		
	}
	
	public LookupUser setApplicationProfileType(String profileType) {
		applicationProfileTypeString = profileType;
		return this;
	}
	
	public LookupUser setUserId(String userId) {
		useridString = userId;
		return this;
	}

	public LookupUser addUserElement(String userElement) {
		userElementTypes.add(userElement);
		return this;
	}
	
	public LookupUser setToAgency(String toAgency) {
		this.toAgency = toAgency;
		return this;
	}

	public LookupUser setFromAgency(String fromAgency) {
		this.fromAgency = fromAgency;
		return this;
	}
	
	//Convenience methods
	public LookupUser includeNameInformation() {
		this.addUserElement(Constants.NAME_INFORMATION);
		return this;
	}
	
	
	public LookupUser includeUserPrivilege() {
		this.addUserElement(Constants.USER_PRIVILEGE);
		return this;
	}
	
	
	public LookupUser includeUserAddressInformation() {
		this.addUserElement(Constants.USER_ADDRESS_INFORMATION);
		return this;
	}
	
	public LookupUser includeUserLanguageInformation() {
		this.addUserElement(Constants.USER_LANGUAGE_INFORMATION);
		return this;
	}
	
	public LookupUser includeUserId() {
		this.addUserElement(Constants.USERID);
		return this;
	}
	
	public JSONObject validateRequest() {
		return null;
	}
	
	/*
	 * This method generates the NCIP2 Request XML
	 */
	public NCIPInitiationData generateNCIP2Object() {
		LookupUserInitiationData lookupUserInitationData = new LookupUserInitiationData();
		InitiationHeader initiationHeader = new InitiationHeader();
		ApplicationProfileType applicationProfileType = new ApplicationProfileType(null,applicationProfileTypeString);
		initiationHeader.setApplicationProfileType(applicationProfileType);
		ToAgencyId toAgencyId = new ToAgencyId();
		toAgencyId.setAgencyId(new AgencyId(toAgency));
		FromAgencyId fromAgencyId = new FromAgencyId();
		fromAgencyId.setAgencyId(new AgencyId(fromAgency));
		initiationHeader.setToAgencyId(toAgencyId);
		initiationHeader.setFromAgencyId(fromAgencyId);
		for (Iterator<String> iter = userElementTypes.iterator(); iter.hasNext(); ) {
			switch (iter.next()) {
			case Constants.NAME_INFORMATION: lookupUserInitationData.setNameInformationDesired(true); break;
			case Constants.USER_ADDRESS_INFORMATION : lookupUserInitationData.setUserAddressInformationDesired(true); break;
			case Constants.USER_PRIVILEGE : lookupUserInitationData.setUserPrivilegeDesired(true); break;
			case Constants.USERID : lookupUserInitationData.setUserIdDesired(true); break;
			case Constants.USER_FISCAL_INFORMATION : lookupUserInitationData.setUserFiscalAccountDesired(true); break;
			case Constants.LOANED_ITEMS: lookupUserInitationData.setLoanedItemsDesired(true); break;
			case Constants.REQUESTED_ITEMS : lookupUserInitationData.setRequestedItemsDesired(true); break;
			case Constants.USER_LANGUAGE_INFORMATION : lookupUserInitationData.setUserLanguageDesired(true); break;
			}
		}
		
		UserId userid = new UserId();
		userid.setAgencyId(new AgencyId(fromAgency));
		userid.setUserIdentifierValue(useridString);
		lookupUserInitationData.setUserId(userid);
		lookupUserInitationData.setInitiationHeader(initiationHeader);

		return lookupUserInitationData;
	}
	

	public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
		
		
		//DEAL W/PROBLEMS IN THE RESPONSE 
		if (responseData.getProblems().size() > 0) {
			return constructProblem(responseData);
		}
				
		LookupUserResponseData lookupUserResponse = (LookupUserResponseData)responseData;
		JSONObject returnJson = new JSONObject();

		returnJson = gatherName(lookupUserResponse,returnJson);
		returnJson.put("userId", getUserIdString(lookupUserResponse,returnJson));
		returnJson.put("privileges", getPrivileges(lookupUserResponse));
		returnJson.put("electronicAddresses", gatherElectronicAddress(lookupUserResponse));
		returnJson.put("physicalAddresses", gatherPhysicalAddress(lookupUserResponse));

		return returnJson;
	}
	
	
	
	private JSONObject gatherName(LookupUserResponseData lookupUserResponse,JSONObject returnJson) {
		
		String firstName = "";
		String lastName = "";
		try {
			firstName = lookupUserResponse.getUserOptionalFields().getNameInformation().getPersonalNameInformation().getStructuredPersonalUserName().getGivenName();
			lastName = lookupUserResponse.getUserOptionalFields().getNameInformation().getPersonalNameInformation().getStructuredPersonalUserName().getSurname();
		}
		catch(Exception e) {
			logger.info("Name not provided in response");
		}
		returnJson.put("firstName", firstName);
		returnJson.put("lastName", lastName);
		return returnJson;
		
	}
	
	
	private JSONArray gatherPhysicalAddress(LookupUserResponseData lookupUserResponse) {
		
		JSONArray jsonArray = new JSONArray();
		if (lookupUserResponse.getUserOptionalFields().getUserAddressInformations() == null) return jsonArray;
		Iterator<UserAddressInformation> iterator = lookupUserResponse.getUserOptionalFields().getUserAddressInformations().iterator();
		while (iterator.hasNext()) {
			UserAddressInformation address = (UserAddressInformation) iterator.next();
			try {
				JSONObject json = new JSONObject();
				if (address.getPhysicalAddress() == null) continue;
				String type =  address.getPhysicalAddress().getPhysicalAddressType().getValue();
				StructuredAddress structuredAddress =  address.getPhysicalAddress().getStructuredAddress();
				JSONObject addressAsJson = new JSONObject();
				addressAsJson.put("lineOne", structuredAddress.getLine1());
				addressAsJson.put("lineTwo", structuredAddress.getLine2());
				addressAsJson.put("locality", structuredAddress.getLocality());
				addressAsJson.put("region", structuredAddress.getRegion());
				addressAsJson.put("postalCode", structuredAddress.getPostalCode());
				json.put("key",type);
				json.put("value",addressAsJson);
				jsonArray.put(json);
			}
			catch(Exception e) {
				logger.error("Unable to parse physical address");
				logger.error(e.toString());
			}
		}
		return jsonArray;
	}

	
	private JSONArray gatherElectronicAddress(LookupUserResponseData lookupUserResponse) {
		
		JSONArray jsonArray = new JSONArray();
		if (lookupUserResponse.getUserOptionalFields().getUserAddressInformations() == null) return jsonArray;
		Iterator<UserAddressInformation> iterator = lookupUserResponse.getUserOptionalFields().getUserAddressInformations().iterator();
		while (iterator.hasNext()) {
			UserAddressInformation address = (UserAddressInformation) iterator.next();
			try {
				JSONObject json = new JSONObject();
				if (address.getElectronicAddress() == null) continue;
				String type =  address.getElectronicAddress().getElectronicAddressType().getValue();
				String value =  address.getElectronicAddress().getElectronicAddressData();
				//MAKING EMAIL 'KEY' CONSISTENT
				if (isEmailPattern(value)) type = "emailAddress";
				json.put("key", type);
				json.put("value", value);
				jsonArray.put(json);
			}
			catch(Exception e) {
				logger.error("Unable to parse electronic address");
				logger.error(e.toString());
			}
		}
		return jsonArray;
	}
	
	private boolean isEmailPattern(String email) {
        String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
        	return true;
        }
        return false;
	}
	
	
	private String getUserIdString(LookupUserResponseData lookupUserResponse,JSONObject returnJson) {
		if (lookupUserResponse.getUserId() != null) 
			return lookupUserResponse.getUserId().getUserIdentifierValue();
		return "";
	}
	
	
	private JSONArray getPrivileges(LookupUserResponseData lookupUserResponse) {
		JSONArray jsonArray = new JSONArray();
		if (lookupUserResponse.getUserOptionalFields() == null) return jsonArray;
		Iterator<UserPrivilege> iterator = lookupUserResponse.getUserOptionalFields().getUserPrivileges().iterator();
		while (iterator.hasNext()) {
			UserPrivilege priv = (UserPrivilege) iterator.next();
			try {
				JSONObject json = new JSONObject();
				String type = priv.getAgencyUserPrivilegeType().getValue();
				String value = priv.getUserPrivilegeStatus().getUserPrivilegeStatusType().getValue();
				//TRANSLATE STATUS FROM ACTIVE TO OK - TO BE CONSISTENT
				//SOME SERVERS (ALMA) RETURN ACTIVE, OTHERS (OLE) RETURN OK
				if (type.equalsIgnoreCase("status") && value.equalsIgnoreCase("active")) value = "OK";
				json.put("key", type);
				json.put("value", value);
				jsonArray.put(json);
			}
			catch(Exception e) {
				logger.error("Unable to parse user privilege");
				logger.error(e.toString());
			}
		}
		return jsonArray;

	}
	
	/**
	 * The method parses the NCIP1 LookUp User response
	 * and creates the return JSON Object
	 *
	 */
	@Override
	public JSONObject constructResponseNcip1Response(String responseData) {
		JSONObject returnJson = new JSONObject();
        try {
            Document document = Jsoup.parse(responseData,"",Parser.xmlParser());
            
            Elements problems = document.select("NCIPMessage > LookupUserResponse > Problem");
            if (problems != null && !problems.isEmpty()) {
            	return constructeNcipOneProblems(problems);
            }
            
            Element name = document.select("NCIPMessage > LookupUserResponse > UserOptionalFields > NameInformation > PersonalNameInformation > UnstructuredPersonalUserName").get(0);
            returnJson.put("electronicAddresses", gatherNcipOneElectronicAddress(document));
            returnJson.put("privileges", getNcipOnePrivileges(document));
            returnJson.put("unstructuredPhysicalAddress", gatherUnstructuredPhysicalAddress(document));
            String nameString = name.text();
            returnJson = gatherUnstructuredName(nameString, returnJson);
            returnJson.put("userId",  document.select("NCIPMessage > LookupUserResponse > UserOptionalFields > VisibleUserId > VisibleUserIdentifier").get(0).text());
        } catch(Exception e) {
        	logger.fatal("failed to parse the NCIP XML Response: " + responseData);
        	logger.fatal(e.getLocalizedMessage());
        }
		return returnJson;
	}
	
	/**
	 * The method generates the NCIP1 request XML
	 *
	 */
	@Override
	public String generateNCIP1Object() {
		// TODO Auto-generated method stub
		logger.info("generating NCIP 1 request XML");
		Handlebars handlebars = new Handlebars();
		try {
			Template template = handlebars.compile("/templates/lookupUser"); 
			Context context = Context.newBuilder(this).resolver(FieldValueResolver.INSTANCE).build();
		    String output =  template.apply(context);
		    //logger.info(output);
		    return output;
		}
		catch(Exception e) {
			logger.fatal("failed to generate the NCIP1 request xml");
			logger.fatal(e.getLocalizedMessage());
		}
		return null;
	}

	/**
	 * The method parses the NCIP1 LookUpUser response
	 * privilege elements
	 *
	 */
	private JSONArray getNcipOnePrivileges(Element xmlDocElement) {
		JSONArray jsonArray = new JSONArray();
		Elements privs = xmlDocElement.select("NCIPMessage > LookupUserResponse > UserOptionalFields > UserPrivilege");
		Iterator privIterator = privs.iterator();
		while (privIterator.hasNext()) {
			Element priv = (Element) privIterator.next();
			JSONObject json = new JSONObject();
			try {
				String type = priv.select("AgencyUserPrivilegeType > Value").text();
				String value = priv.select("UserPrivilegeStatus > UserPrivilegeStatusType > Value").text();
				json.put("key", type);
				json.put("value", value);
				jsonArray.put(json);
			}
			catch(Exception e) {
				logger.fatal("failed parsing the user privileges in the NCIP-1 response: " + xmlDocElement.toString());
				logger.fatal(e.getLocalizedMessage());
			}
		}
		return jsonArray;
	}
	
	/**
	 * The method parses the NCIP1 LookUpUser response
	 * email address elements
	 *
	 */
	private JSONArray gatherNcipOneElectronicAddress(Element xmlDocElement) {
		
		JSONArray jsonArray = new JSONArray();
		Elements addresses = xmlDocElement.select("NCIPMessage > LookupUserResponse > UserOptionalFields > UserAddressInformation > ElectronicAddress");
		Iterator addressIterator = addresses.iterator();
		while (addressIterator.hasNext()) {
			Element address = (Element) addressIterator.next();
			JSONObject json = new JSONObject();
			try {
				String type = address.select("ElectronicAddressType > Value").text();
				String value = address.select("ElectronicAddressData").text();
				//MAKING EMAIL 'KEY' CONSISTENT
				if (isEmailPattern(value)) type = "emailAddress";
				json.put("key", type);
				json.put("value", value);
				jsonArray.put(json);
			}
			catch(Exception e) {
				//OK NOT AN EMAIL ADDRESS
			}
		}
		return jsonArray;
	}
	
	
	private JSONArray gatherUnstructuredPhysicalAddress(Element xmlDocElement) {
		JSONArray jsonArray = new JSONArray();
		//String addressRoleType = xmlDocElement.select("NCIPMessage > LookupUserResponse > UserOptionalFields > UserAddressInformation > UserAddressRoleType > Value").text();
		Elements addresses = xmlDocElement.select("NCIPMessage > LookupUserResponse > UserOptionalFields > UserAddressInformation");
		Iterator addressIterator = addresses.iterator();
		while (addressIterator.hasNext()) {
			Element address = (Element) addressIterator.next();
			JSONObject json = new JSONObject();
			try {
				String type = address.select("UserAddressRoleType > Value").text();
				String value = address.select("PhysicalAddress > UnstructuredAddress > UnstructuredAddressData").text();
				if (value != null && !value.equalsIgnoreCase("")) {
					json.put("key", type);
					json.put("value", value);
					jsonArray.put(json);
				}
			}
			catch(Exception e) {
				//OK NOT AN UNSTRUCTURED ADDRESS
			}

		}
		return jsonArray;
	}
	
	/**
	 * The method parses the unstructured name from the NCIP1 LookupUser response
	 * into first name and last name
	 *
	 */
	private JSONObject gatherUnstructuredName(String name, JSONObject returnJson) {
		
		try {
			String[] values = name.split(",");
			returnJson.put("lastName", values[0]);
			returnJson.put("firstName", values[1]);
		}
		catch(Exception e) {
			logger.info("Unstructured name returned from NCIP could not be parsed. ");
			logger.info(name);
			logger.info("Using entire string in last name field");
			returnJson.put("lastName", name);
			returnJson.put("firstName", " ");
		}
		//else...put the entire string in the last name field
		return returnJson;
	}



}
	

