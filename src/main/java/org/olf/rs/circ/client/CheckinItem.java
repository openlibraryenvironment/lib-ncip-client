package org.olf.rs.circ.client;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemResponseData;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;

public class CheckinItem extends NCIPService implements NCIPCircTransaction {

	protected String toAgency;
	protected String fromAgency;
	private String applicationProfileTypeString;
	private String itemIdString;
	List<String> itemOptionalFields = new ArrayList<>();
	private static final Logger logger = Logger.getLogger(CheckinItem.class);

	public CheckinItem() {

	}

	public CheckinItem addItemElement(String itemElement) {
		itemOptionalFields.add(itemElement);
		return this;
	}

	public CheckinItem setApplicationProfileType(String profileType) {
		applicationProfileTypeString = profileType;
		return this;
	}

	public CheckinItem setItemId(String itemId) {
		itemIdString = itemId;
		return this;
	}

	public CheckinItem setToAgency(String toAgency) {
		this.toAgency = toAgency;
		return this;
	}

	public CheckinItem setFromAgency(String fromAgency) {
		this.fromAgency = fromAgency;
		return this;
	}
	
	//convenience methods
	public CheckinItem includeBibliographicDescription() {
		itemOptionalFields.add(Constants.BIBLIOGRAPHIC_DESCRIPTION);
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
		// TODO Auto-generated method stub
		CheckInItemInitiationData checkinItemInitiationData = new CheckInItemInitiationData();
		InitiationHeader initiationHeader = new InitiationHeader();
		ApplicationProfileType applicationProfileType = new ApplicationProfileType(null, applicationProfileTypeString);
		initiationHeader.setApplicationProfileType(applicationProfileType);
		ToAgencyId toAgencyId = new ToAgencyId();
		toAgencyId.setAgencyId(new AgencyId(toAgency));
		FromAgencyId fromAgencyId = new FromAgencyId();
		fromAgencyId.setAgencyId(new AgencyId(fromAgency));
		initiationHeader.setToAgencyId(toAgencyId);
		initiationHeader.setFromAgencyId(fromAgencyId);

		// TODO
		// I'M NOT SURE NCIP SERVERS SUPPORT THIS PART OF THE PROTOCOL
		// OLE DOESN'T SEEM TO
		// MAYBE REMOVE?
		for (Iterator<String> iter = itemOptionalFields.iterator(); iter.hasNext();) {
			switch (iter.next()) {
			case Constants.BIBLIOGRAPHIC_DESCRIPTION:
				checkinItemInitiationData.setBibliographicDescriptionDesired(true);
				break;
			case Constants.ITEM_USE_RESTRICTION:
				checkinItemInitiationData.setItemUseRestrictionTypeDesired(true);
				break;
			case Constants.CIRCULATION_STATUS:
				checkinItemInitiationData.setCirculationStatusDesired(true);
				break;
			case Constants.LOCATION:
				checkinItemInitiationData.setLocationDesired(true);
				break;
			}
		}

		ItemId itemId = new ItemId();
		itemId.setAgencyId(new AgencyId(fromAgency));
		itemId.setItemIdentifierValue(itemIdString);

		checkinItemInitiationData.setItemId(itemId);
		checkinItemInitiationData.setInitiationHeader(initiationHeader);

		return checkinItemInitiationData;
	}


	public JSONObject constructResponseNcip2Response(NCIPResponseData responseData) {
		CheckInItemResponseData checkinItemResponse = (CheckInItemResponseData) responseData;
		JSONObject returnJson = new JSONObject();

		// DEAL W/PROBLEMS IN THE RESPONSE
		if (checkinItemResponse.getProblems().size() > 0) {
			return constructProblem(responseData);
		}

		String itemId = checkinItemResponse.getItemId().getItemIdentifierValue();
		returnJson.put(Constants.ITEM_ID, itemId);
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
			Template template = handlebars.compile("/templates/checkinItem"); 
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
	



	@Override
	public JSONObject constructResponseNcip1Response(String responseData) {
		JSONObject returnJson = new JSONObject();
        try {
            Document document = Jsoup.parse(responseData,"",Parser.xmlParser());

            Elements problems = document.select("NCIPMessage > CheckInItemResponse > Problem");
            if (problems != null && !problems.isEmpty()) {
            	return constructeNcipOneProblems(problems);
            }
            
            String  itemId = document.select("NCIPMessage > CheckInItem > UniqueItemId > ItemIdentifierValue").text();
            returnJson.put("itemId", itemId);
        } catch(Exception e) {
        	logger.fatal("failed to parse the NCIP XML Response: " + responseData);
        	logger.fatal(e.getLocalizedMessage());
        }
		return returnJson;
	}

}
