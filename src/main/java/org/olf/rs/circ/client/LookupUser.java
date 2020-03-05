package org.olf.rs.circ.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserResponseData;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.UserAddressInformation;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.extensiblecatalog.ncip.v2.service.UserPrivilege;
import org.json.JSONArray;
import org.json.JSONObject;

public class LookupUser extends NCIP2Service implements NCIPCircTransaction {
	
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
	
	//TODO
	//DOES RESHARE NEED THIS INFO RETURNED?
	//DURING TESTING IT DIDN'T SEEM LIKE ALL NCIP
	//SERVERS SUPPORT RETURNING THESE ELEMENTS
	//EVEN THOUGH IT IS PART OF THE PROTOCOL
	//FISCAL INFORMATION IS ALSO SUPPORTED
	/*public LookupUser includeRequestedItems() {
		this.addUserElement(Constants.REQUESTED_ITEMS);
		return this;
	}
	
	public LookupUser includeLoanedItems() {
		this.addUserElement(Constants.LOANED_ITEMS);
		return this;
	} */
	

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
			case Constants.NAME_INFORMATION: lookupUserInitationData.setNameInformationDesired(true);
			case Constants.USER_ADDRESS_INFORMATION : lookupUserInitationData.setUserAddressInformationDesired(true);
			case Constants.USER_PRIVILEGE : lookupUserInitationData.setUserPrivilegeDesired(true);
			case Constants.USERID : lookupUserInitationData.setUserIdDesired(true);
			case Constants.USER_FISCAL_INFORMATION : lookupUserInitationData.setUserFiscalAccountDesired(true);
			case Constants.LOANED_ITEMS: lookupUserInitationData.setLoanedItemsDesired(true);
			case Constants.REQUESTED_ITEMS : lookupUserInitationData.setRequestedItemsDesired(true);
			case Constants.USER_LANGUAGE_INFORMATION : lookupUserInitationData.setUserLanguageDesired(true);
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
		LookupUserResponseData lookupUserResponse = (LookupUserResponseData)responseData;
		JSONObject returnJson = new JSONObject();
		
		//DEAL W/PROBLEMS IN THE RESPONSE
		if (lookupUserResponse.getProblems().size() > 0) {
			return constructProblem(responseData);
		}
		
		returnJson = gatherName(lookupUserResponse,returnJson);
		returnJson.put("userId", getUserIdString(lookupUserResponse,returnJson));
		returnJson.put("privileges", getPrivileges(lookupUserResponse));
		returnJson.put("electronicAddresses", gatherElectronicAddress(lookupUserResponse));

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
				json.put("key", type);
				json.put("value", value);
				jsonArray.put(json);
			}
			catch(Exception e) {
				logger.error("Unable to parse electronic a");
				logger.error(e.toString());
			}
		}
		return jsonArray;
		
		
		
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


}
	

