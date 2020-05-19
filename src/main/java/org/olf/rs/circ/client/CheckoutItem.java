package org.olf.rs.circ.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemResponseData;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.UserId;
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

public class CheckoutItem extends NCIPService implements NCIPCircTransaction {
	
	private static final Logger logger = Logger.getLogger(CheckoutItem.class);
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
	 * Sets desired due date.  Desired due date is NOT always supported by the NCIP 
	 * responder.
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
	
	public JSONObject validateRequest() {
		if (this.itemIdString == null) return constructMissingElementProblem("Item ID");
		return null;
	}

	/*
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
	
	/*
	 * This method generates the NCIP2 response data in the format of a JSONObject
	 */
	public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
		CheckOutItemResponseData checkoutItemResponse = (CheckOutItemResponseData)responseData;
		JSONObject returnJson = new JSONObject();
		
		//DEAL W/PROBLEMS IN THE RESPONSE
		if (checkoutItemResponse.getProblems().size() > 0) {
			return constructProblem(responseData);
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		formatter.setCalendar(checkoutItemResponse.getDateDue());
		String dueDateString = formatter.format(checkoutItemResponse.getDateDue().getTime());

		
		returnJson.put("dueDate", dueDateString);
		returnJson.put("itemId", checkoutItemResponse.getItemId().getItemIdentifierValue());
		returnJson.put("userId", checkoutItemResponse.getUserId().getUserIdentifierValue());
		return  returnJson;
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
			Template template = handlebars.compile("/templates/checkoutItem"); 
			Context context = Context.newBuilder(this).resolver(FieldValueResolver.INSTANCE).build();
		    String output =  template.apply(context);
		    logger.info(output);
		    return output;
		}
		catch(Exception e) {
			logger.fatal("failed to generate the NCIP1 request xml");
			logger.fatal(e.getLocalizedMessage());
		}
		return null;
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
        }
		return returnJson;
	}
	

}
