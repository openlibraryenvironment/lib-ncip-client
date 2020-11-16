package org.olf.rs.circ.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
import org.json.JSONArray;
import org.json.JSONObject;

public class NCIP2Client implements CirculationClient {
	
	private static final Logger logger = Logger.getLogger(NCIP2Client.class);
	protected String endpoint;
	private XCToolkitUtil xcToolkitUtil;
	//TODO ADD TIMEOUT PREFERENCE ?
	//TODO ADD RETRY ATTEMPT PREFERENCE ?

	public NCIP2Client(String endpoint, Map<String, Object> inputParms) throws NCIPClientException {
		try {
			CaseInsensitiveMap<String,Object> inputMap = new CaseInsensitiveMap<String,Object>();
			inputMap.putAll(inputParms);
			xcToolkitUtil = XCToolkitUtil.getInstance();
			this.endpoint = endpoint;
		}
		catch(Exception e) {
			throw new NCIPClientException(e.getLocalizedMessage());
		}
	}


	public JSONObject send(NCIPCircTransaction transaction)  {
		
		if (this.endpoint == null) {
			logger.fatal("NCIP2Client send calls but endpoint is missing");
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		
		JSONObject errors = transaction.validateRequest();
		if (errors != null) return errors;
		
		//generates XC NCIP Objects:
		NCIPInitiationData  initiationData = transaction.generateNCIP2Object();
		InputStream requestMessageStream = null;
		//transforms the object into NCIP XML:
		try {
			requestMessageStream =  xcToolkitUtil.translator.createInitiationMessageStream(xcToolkitUtil.serviceContext, initiationData);
		}
		catch(Exception e) {
			logger.fatal("NCIP2Client send call failed building requestMessageStream");
			JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2Client send call failed building requestMessageStream");
			return r;
		}
		String requestBody = null;
		try {
			requestBody = IOUtils.toString(requestMessageStream, StandardCharsets.UTF_8);
		}
		catch(Exception e) {
			logger.fatal("NCIP2Client send call failed building requestMessageStream");
			JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2Client send call failed building XML");
			return r;
		}
		logger.info(requestBody);
		String responseString = null;
		JSONObject responseObject = new JSONObject();
		//call to NCIP server
		try {
			CloseableHttpClient client = HttpClients.custom().build();
			HttpUriRequest request = RequestBuilder.post()
					.setUri(this.endpoint)
					.setEntity(new StringEntity(requestBody,"UTF-8"))
					.setHeader("Content-Type", "application/xml")
					.build();
			
			 HttpResponse response = client.execute(request);
			 HttpEntity entity = response.getEntity();
			 responseString = EntityUtils.toString(entity, "UTF-8");
			 logger.info("NCIP2 request sent: ");
			 logger.info(requestBody);
			 logger.info("to: " + this.endpoint);
			 logger.info("NCIP2 response received: ");
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
			problem.put("type","NCIP2 Client failed to call NCIP server or parse returned results");
			problem.put("element",e.getCause());
			problem.put("detail", e.getLocalizedMessage());
			array.put(problem);
			responseObject.put("problems", array);			
		}
		return responseObject;
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


	@Override
	public String printRequest(NCIPCircTransaction transaction) throws NCIPClientException {
		NCIPInitiationData  initiationData = transaction.generateNCIP2Object();
		InputStream requestMessageStream = null;
		//transforms the object into NCIP XML:
		try {
			requestMessageStream =  xcToolkitUtil.translator.createInitiationMessageStream(xcToolkitUtil.serviceContext, initiationData);
		}
		catch(Exception e) {
			logger.fatal("NCIP2Client printRequest call failed building requestMessageStream");
			JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2Client printRequest call failed building requestMessageStream");
			return r.toString();
		}
		String requestBody = null;
		try {
			requestBody = IOUtils.toString(requestMessageStream, StandardCharsets.UTF_8);
		}
		catch(Exception e) {
			logger.fatal("NCIP2Client printRequest call failed building requestMessageStream");
			JSONObject r = constructException("Toolkit Exception ", e.getLocalizedMessage(),"NCIP2Client printRequest call failed building XML");
			return r.toString();
		}
		return requestBody;
	}


}
