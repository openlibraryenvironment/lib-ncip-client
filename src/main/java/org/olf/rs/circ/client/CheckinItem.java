package org.olf.rs.circ.client;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.extensiblecatalog.ncip.v2.binding.BindingError;
import org.extensiblecatalog.ncip.v2.binding.BindingException;
import org.extensiblecatalog.ncip.v2.service.AcceptItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.AgencyId;
import org.extensiblecatalog.ncip.v2.service.ApplicationProfileType;
import org.extensiblecatalog.ncip.v2.service.BibliographicDescription;
import org.extensiblecatalog.ncip.v2.service.CheckInItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckInItemResponseData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.CheckOutItemResponseData;
import org.extensiblecatalog.ncip.v2.service.CreateItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.FromAgencyId;
import org.extensiblecatalog.ncip.v2.service.InitiationHeader;
import org.extensiblecatalog.ncip.v2.service.ItemDescription;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.ItemOptionalFields;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.PickupLocation;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.RequestId;
import org.extensiblecatalog.ncip.v2.service.RequestedActionType;
import org.extensiblecatalog.ncip.v2.service.ToAgencyId;
import org.extensiblecatalog.ncip.v2.service.UserId;
import org.json.JSONArray;
import org.json.JSONObject;

public class CheckinItem extends NCIP2Service implements NCIPCircTransaction {

	protected String toAgency;
	protected String fromAgency;
	private String applicationProfileTypeString;
	private String itemIdString;
	List<String> itemOptionalFields = new ArrayList<>();

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

	public NCIPInitiationData generateNCIP2Object() {
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
			case Constants.ITEM_USE_RESTRICTION:
				checkinItemInitiationData.setItemUseRestrictionTypeDesired(true);
			case Constants.CIRCULATION_STATUS:
				checkinItemInitiationData.setCirculationStatusDesired(true);
			case Constants.LOCATION:
				checkinItemInitiationData.setLocationDesired(true);
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

}
