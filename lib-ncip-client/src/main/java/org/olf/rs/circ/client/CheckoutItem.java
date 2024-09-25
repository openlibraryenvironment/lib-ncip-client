package org.olf.rs.circ.client;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class CheckoutItem extends NCIPService implements NCIPCircTransaction {
	
	private static final Logger logger = Logger.getLogger(CheckoutItem.class);
	protected String registryId; //WMS ONLY
	protected String toAgency;
	protected String fromAgency;
	private String useridString;
	private String applicationProfileTypeString;
	private String itemIdString;
	private String requestIdString;
	private String desiredDueDate;


	public CheckoutItem() {
		
	}
	
	public CheckoutItem setApplicationProfileType(String profileType) {
		applicationProfileTypeString = profileType;
		return this;
	}
	/*
	 * Sets desired due date.  Desired due date is NOT always supported by NCIP 
	 * responders.
	 */
	public CheckoutItem setDesiredDueDate(String dueDate) {
		desiredDueDate = dueDate;
		return this;
	}
	
	public CheckoutItem setRequestId(String requestId) {
		requestIdString = requestId;
		return this;
	}
	
	public CheckoutItem setItemId(String itemId) {
		itemIdString = itemId;
		return this;
	}

	public CheckoutItem setUserId(String userId) {
		useridString = userId;
		return this;
	}

	public CheckoutItem setToAgency(String toAgency) {
		this.toAgency = toAgency;
		return this;
	}

	public CheckoutItem setFromAgency(String fromAgency) {
		this.fromAgency = fromAgency;
		return this;
	}
	
	/**
	 * setRegistryId
	 * @param string - registry ID.  Will be set as the 'AgencyId' on the 'ItemId' element
	 * @return Instance object
	 */
	public CheckoutItem setRegistryId(String registryId) {
		this.registryId = registryId;
		return this;
	}
	
	public JSONObject validateRequest() {
		if (this.itemIdString == null) return constructMissingElementProblem("Item ID");
		return null;
	}

	/**
	 * This method generates the NCIP2 Request XML
	 */
	public NCIPInitiationData generateNCIP2Object() {
		CheckOutItemInitiationData checkoutItemInitiationData = new CheckOutItemInitiationData();
		InitiationHeader initiationHeader = new InitiationHeader();
		ApplicationProfileType applicationProfileType = new ApplicationProfileType(null,applicationProfileTypeString);
		initiationHeader.setApplicationProfileType(applicationProfileType);
		ToAgencyId toAgencyId = new ToAgencyId();
		toAgencyId.setAgencyId(new AgencyId(toAgency));
		FromAgencyId fromAgencyId = new FromAgencyId();
		fromAgencyId.setAgencyId(new AgencyId(fromAgency));
		initiationHeader.setToAgencyId(toAgencyId);
		initiationHeader.setFromAgencyId(fromAgencyId);

		UserId userid = new UserId();
		userid.setAgencyId(new AgencyId(fromAgency));
		userid.setUserIdentifierValue(useridString);
		
		ItemId itemId = new ItemId();
		itemId.setAgencyId(new AgencyId(fromAgency));
		itemId.setItemIdentifierValue(itemIdString);
		
		RequestId requestId = new RequestId();
		requestId.setAgencyId(new AgencyId(fromAgency));
		requestId.setRequestIdentifierValue(requestIdString);
		
		if (desiredDueDate != null && !desiredDueDate.equalsIgnoreCase("")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = df.parse(desiredDueDate);
			} catch (ParseException e) {
				logger.error("Unable to parse desired due date:");
				logger.error(desiredDueDate);
				logger.error(e.getLocalizedMessage());
			}
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			checkoutItemInitiationData.setDesiredDateDue((GregorianCalendar) cal);
		}

		checkoutItemInitiationData.setItemId(itemId);
		checkoutItemInitiationData.setUserId(userid);
		checkoutItemInitiationData.setRequestId(requestId);
		checkoutItemInitiationData.setInitiationHeader(initiationHeader);
		return checkoutItemInitiationData;
	}
	
	/**
	 * This method generates a JSONObject using the NCIPResponsData object for CheckoutItem
	 */
	public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
		CheckOutItemResponseData checkoutItemResponse;
		try {
			checkoutItemResponse = (CheckOutItemResponseData)responseData;
			//DEAL W/PROBLEMS IN THE RESPONSE
			if (checkoutItemResponse.getProblems() != null && checkoutItemResponse.getProblems().size() > 0) {
				return constructProblem(responseData);
			}
		}
		// WMS RETURNS A PROBLEM ELEMENT (INSTEAD OF NCIPResponseData)
		catch(ClassCastException e) {
			return constructProblem(responseData);
		}
		JSONObject returnJson = new JSONObject();

		String dueDateString = "";
		if (checkoutItemResponse.getDateDue() != null) {
			//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			TimeZone tz = TimeZone.getTimeZone("UTC");
			//formatter.setCalendar(checkoutItemResponse.getDateDue());
			formatter.setTimeZone(tz);
			dueDateString = formatter.format(checkoutItemResponse.getDateDue().getTime());
		}

		if (checkoutItemResponse.getLoanUuid() != null) {
			returnJson.put("loanUuid", checkoutItemResponse.getLoanUuid().getItemIdentifierValue());
		}

		if (checkoutItemResponse.getItemOptionalFields() != null &&
				checkoutItemResponse.getItemOptionalFields().getItemDescription() != null &&
				checkoutItemResponse.getItemOptionalFields().getItemDescription().getCallNumber() != null) {
			returnJson.put("callNumber", checkoutItemResponse.getItemOptionalFields().getItemDescription().getCallNumber());
		}
		
		returnJson.put("dueDate", dueDateString);
		returnJson.put("itemId", checkoutItemResponse.getItemId().getItemIdentifierValue());
		returnJson.put("userId", checkoutItemResponse.getUserId().getUserIdentifierValue());
		return  returnJson;
	}

	
	public NCIPInitiationData modifyForWMS(NCIPInitiationData initData) {
		((CheckOutItemInitiationData)initData).getItemId().setAgencyId(new AgencyId(registryId));
		((CheckOutItemInitiationData)initData).getUserId().setAgencyId(new AgencyId(registryId));
	   return initData;
	}
	
	/**
	 * Call to generate NCIP1 request XML using specific template file
	 *
	 */
	@Override
	public String generateNCIP1Object() {
		return generateNCIP1Object("/templates/checkoutItem");
	}
	
	/*
	 * This method generates the NCIP1 response data in the format of a JSONObject
	 */
	@Override
	public JSONObject constructResponseNcip1Response(String responseData) {
		JSONObject returnJson = new JSONObject();
        try {
            Document document = Jsoup.parse(responseData,"",Parser.xmlParser());
            
            Elements problems = document.select("NCIPMessage > CheckOutItemResponse > Problem");
            if (problems != null && !problems.isEmpty()) {
            	return constructeNcipOneProblems(problems);
            }
            
            String  itemId = document.select("NCIPMessage > CheckOutItemResponse > UniqueItemId > ItemIdentifierValue").text();
            String  userId = document.select("NCIPMessage > CheckOutItemResponse > UniqueUserId > UserIdentifierValue").text();
            String dueDate = document.select("NCIPMessage > CheckOutItemResponse > DateDue").text();
            returnJson.put("itemId", itemId);
            returnJson.put("dueDate", dueDate);
            returnJson.put("userId", userId);
        } catch(Exception e) {
        	logger.fatal("failed to parse the NCIP XML Response: " + responseData);
        	logger.fatal(e.getLocalizedMessage());
        	throw e;
        }
		return returnJson;
	}

	/**
	 * Not implemented because the WMS response for CheckoutItem
	 * is constructed with the constructResponseNcip2Response method 
	 */
	@Override
	public JSONObject constructWMSResponse(JSONObject responseJson) {
		throw new NotImplementedException();
	}
	
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
