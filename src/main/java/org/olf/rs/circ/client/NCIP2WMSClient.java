package org.olf.rs.circ.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.NCIPInitiationData;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;


public class NCIP2WMSClient implements CirculationClient {

	private static final Logger logger = Logger.getLogger(NCIP2WMSClient.class);
	private String endpoint;
	private XCToolkitUtil xcToolkitUtil;
	private String apiKey;
	private String apiSecret;
	private String oAuthEndpointOverride;
	private String lookupPatronEndpoint;


	public NCIP2WMSClient(String endpoint, Map<String, Object> inputParms) throws NCIPClientException {
		try {
			xcToolkitUtil = XCToolkitUtil.getInstance();
			CaseInsensitiveMap<String,Object> inputMap = new CaseInsensitiveMap<String,Object>();
			inputMap.putAll(inputParms);
			this.endpoint = endpoint;
			this.apiSecret = (String) inputMap.get("apiSecret");
			this.apiKey = (String) inputMap.get("apiKey");
			this.oAuthEndpointOverride = (String) inputMap.get("oAuthEndpointOverride");
			this.lookupPatronEndpoint  = (String) inputMap.get("lookupPatronEndpoint");
		}
		catch(Exception e) {
			throw new NCIPClientException(e.getLocalizedMessage());
		}
	}


	private JSONObject authenticate(String apiKey, String apiSecret, String scope) {
		String token = null;
		JSONObject responseObject = new JSONObject();
		try {
			CloseableHttpClient client = HttpClients.custom().build();
			String authString = apiKey + ":" + apiSecret;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			
			String authEndpoint = (oAuthEndpointOverride == null) ? Constants.OCLC_OAUTH  : oAuthEndpointOverride;

			HttpUriRequest request = RequestBuilder.post()
					.setUri(authEndpoint + scope)
					.addHeader("Authorization", "Basic " + authStringEnc)
					.build();
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode > 399) {
				throw new Exception("Call to " + authEndpoint + " returned response code " + responseCode + ".  Response body: " + responseString);
			}
			JSONObject authResponse = new JSONObject(responseString);
			token = authResponse.getString("access_token");
		}
		catch(Exception e) {
			JSONArray array = new JSONArray();
			JSONObject problem = new JSONObject();
			problem.put("detail","NCIP2 WMS Client failed to call authenticate");
			problem.put("element",e.getCause());
			problem.put("value", e.getLocalizedMessage());
			array.put(problem);
			responseObject.put("problems", array);	
		}
		responseObject.put("token",token);
		return responseObject;
	}

	/**
	 * Sends the NCIP request
	 *
	 * @return JSONObject results of NCIP call
	 */

	public JSONObject send(NCIPCircTransaction transaction) throws NCIPClientException {
		
		//MAKE SURE THIS IS NOT AN ACCEPT ITEM TRANSACTION
		//WMS DOES NOT SUPPORT ACCEPT ITEM AS THIS TIME
		if (transaction.getClass() == AcceptItem.class) {
			return this.constructException("transaction type not supported", "NCIP2WMS does not support the AcceptItem message", transaction.getClass().toString());
		}

		if (endpoint == null) {
			logger.fatal("NCIPWMSClient send called but endpoint is missing");
			JSONObject r = constructException("Missing endpoint ", "NCIP Client endpoint is null","");
			return r;
		}

		if (apiKey == null || apiSecret == null) {
			logger.fatal("NCIP2WMSClient send called but apiKey or apiSecret is missing");
			JSONObject r = constructException("Missing credentials ", "NCIP Client apikey or apiSecret is null","");
			return r;
		}

		//WHEN WMS HAS A LOOKPUP USER SERVICE
		//THAT RETURNS THE NEEDED INFORMATION,
		//THIS 'IF' CAN BE REMOVED
		//callNcipService(transaction) WILL BE THE ONLY
		//CALL NEEDED
		try {
			JSONObject responseObject = new JSONObject();
			if  (transaction.getClass() != LookupUser.class) {
				responseObject = callNcipService(transaction);
				return responseObject;
			}
			else {
				responseObject = lookupUser(transaction);
				return responseObject;
			}
		}
		catch(Exception e) {
			throw new NCIPClientException(e.getLocalizedMessage());
		}

		
	}
	
	public String printRequest(NCIPCircTransaction transaction) throws NCIPClientException {
		try {
			if  (transaction.getClass() != LookupUser.class) {
				NCIPInitiationData  initiationData = transaction.generateNCIP2Object();
				transaction.modifyForWMS(initiationData);
				InputStream requestMessageStream = null;
				//transforms the object into NCIP XML:
				try {
					requestMessageStream =  xcToolkitUtil.translator.createInitiationMessageStream(xcToolkitUtil.serviceContext, initiationData);
				}
				catch(Exception e) {
					logger.fatal("NCIP2Client printRequest call failed building requestMessageStream");
					JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2WMSClient printRequest call failed building requestMessageStream");
					return r.toString();
				}
				String requestBody = null;
				try {
					requestBody = IOUtils.toString(requestMessageStream, StandardCharsets.UTF_8);
				}
				catch(Exception e) {
					logger.fatal("NCIP2Client printRequest call failed building requestMessageStream");
					JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(), "NCIP2WMSClient printRequest call failed building XML");
					return r.toString();
				}
				return requestBody;
			}
			else {
				JSONObject jsonRequest = new JSONObject();
				JSONArray schemasArray = new JSONArray();
				schemasArray.put("urn:ietf:params:scim:api:messages:2.0:SearchRequest");
				jsonRequest.put("filter",  "External_ID eq \"" + ((LookupUser)transaction).getUserid()  + "\"");
				jsonRequest.put("schemas",schemasArray);
				return jsonRequest.toString();
			}
		}
		catch(Exception e) {
			throw new NCIPClientException(e.getLocalizedMessage());
		}
	}

	private JSONObject constructException(String httpResponse,String entireResponse,String element) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject problem = new JSONObject();
		problem.put("type",httpResponse);
		problem.put("detail", entireResponse);
		problem.put("element", element);
		array.put(problem);
		returnJson.put("problems", array);
		return returnJson;
	}

	private JSONObject callNcipService(NCIPCircTransaction transaction) throws ServiceException, ValidationException, IOException {
		String token = null;
		JSONObject responseObject = new JSONObject();
		try {
			JSONObject jsonObject = authenticate(apiKey,apiSecret,Constants.NCIP_SCOPE);
			if (jsonObject.optString("token",null) == null) return jsonObject;
			token = jsonObject.optString("token");
		}
		catch(Exception e) {
			throw e;
		}

		JSONObject errors = transaction.validateRequest();
		if (errors != null) return errors;

		//generates XC NCIP Objects:
		NCIPInitiationData  initiationData = transaction.generateNCIP2Object();
		transaction.modifyForWMS(initiationData);
		//transforms the object into NCIP XML:
		InputStream requestMessageStream =  xcToolkitUtil.translator.createInitiationMessageStream(xcToolkitUtil.serviceContext, initiationData);

		String requestBody = IOUtils.toString(requestMessageStream, StandardCharsets.UTF_8);
		logger.info(requestBody);
		String responseString = null;
		//call to NCIP server
		try {
			CloseableHttpClient client = HttpClients.custom().build();

			HttpUriRequest request = RequestBuilder.post()
					.setUri(this.endpoint)
					.setEntity(new StringEntity(requestBody,"UTF-8"))
					.setHeader("Content-Type", "application/xml")
					.addHeader("Authorization", "Bearer " + token)
					.build();

			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
			logger.info("NCIP2 (WMS) request sent: ");
			logger.info(requestBody);
			logger.info("to: " + this.endpoint);
			logger.info("NCIP2 (WMS) response received: ");
			logger.info(responseString);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode > 399) {
				throw new Exception("Http call to " + this.endpoint + " returned response code " + responseCode + ".  Response body: " + responseString);
			}
			//transforms the NCIP xml response into XC NCIP Objects
			InputStream stream = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));
			NCIPResponseData responseData = xcToolkitUtil.translator.createResponseData(xcToolkitUtil.serviceContext, stream);
			//transforms the XC NCIP Objects into a JSON response object
			responseObject = transaction.constructResponseNcip2Response(responseData);
			logger.info(responseObject.toString());
		}
		catch(Exception e) {
			JSONArray array = new JSONArray();
			JSONObject problem = new JSONObject();
			problem.put("detail","NCIP2 Client failed to call NCIP server or parse returned results");
			problem.put("element",e.getCause());
			problem.put("value", e.getLocalizedMessage());
			array.put(problem);
			responseObject.put("problems", array);			
		}
		return responseObject;
	}
	
	
	private JSONObject lookupUser(NCIPCircTransaction transaction) {
		
		if (lookupPatronEndpoint == null || lookupPatronEndpoint.isEmpty()) {
			logger.fatal("NCIPWMSClient send called but lookup patron endpoint is missing");
			JSONObject r = constructException("Missing lookupPatronEndpoint ", "NCIP Client lookupPatronEndpoint is null","");
			return r;
		}
		
		
		String token = null;
		JSONObject responseObject = new JSONObject();
		try {
			JSONObject jsonObject = authenticate(apiKey,apiSecret,Constants.IDM_SCOPE);
			if (jsonObject.optString("token",null) == null) return jsonObject;
			token = jsonObject.optString("token");
		}
		catch(Exception e) {
			throw e;
		}

		JSONObject jsonRequest = new JSONObject();
		JSONArray schemasArray = new JSONArray();
		schemasArray.put("urn:ietf:params:scim:api:messages:2.0:SearchRequest");
		jsonRequest.put("filter",  "External_ID eq \"" + ((LookupUser)transaction).getUserid()  + "\"");
		jsonRequest.put("schemas",schemasArray);
		String responseString = null;
		responseObject = new JSONObject();
		try {
			CloseableHttpClient client = HttpClients.custom().build();
			HttpUriRequest request = RequestBuilder.post()
					.setUri(this.lookupPatronEndpoint)
					.setEntity(new StringEntity(jsonRequest.toString()))
					.addHeader("Content-Type", "application/scim+json")
					.addHeader("Accept", "application/scim+json")
					.addHeader("Authorization", "Bearer " + token)
					.build();
			logger.info("OCLC lookup user request sent:");
			logger.info(jsonRequest.toString());
			logger.info("to: "  + this.endpoint);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
			logger.info("OCLC lookup user response received:");
			logger.info(responseString);
			int responseCode = response.getStatusLine().getStatusCode();
			logger.info(responseCode);
			if (responseCode > 399) {
				throw new Exception("Http call to " + this.endpoint + " returned response code " + responseCode + ".  Response body: " + responseString);
			}
			JSONObject responseAsJson = new JSONObject(responseString);
			responseObject = transaction.constructWMSResponse(responseAsJson);

		}
		catch(Exception e) {
			JSONArray array = new JSONArray();
			JSONObject problem = new JSONObject();
			problem.put("type","NCIP2WMS Client failed to call Lookup User or parse returned results");
			problem.put("element",e.getCause());
			problem.put("detail", e.getLocalizedMessage());
			array.put(problem);
			responseObject.put("problems", array);		
		}
		return responseObject;
	}
	
}
