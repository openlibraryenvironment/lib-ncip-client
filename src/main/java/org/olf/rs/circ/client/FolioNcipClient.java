package org.olf.rs.circ.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.ValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

public class FolioNcipClient implements CirculationClient {
	
	private static final Logger logger = Logger.getLogger(FolioNcipClient.class);
	private String endpoint;
	private String userid;
	private String password;
	private String tenant;
	private HashMap<String, String> httpHeader = new HashMap<String, String>();
	private XCToolkitUtil xcToolkitUtil;
	//TODO ADD TIMEOUT PREFERENCE ?
	//TODO ADD RETRY ATTEMPT PREFERENCE ?
	
	public FolioNcipClient() throws IOException, ToolkitException {
		xcToolkitUtil = XCToolkitUtil.getInstance();
	}
	
	public FolioNcipClient(String endpoint, String userid, String password, String tenant) throws IOException, ToolkitException {
		xcToolkitUtil = XCToolkitUtil.getInstance();
		this.endpoint = endpoint;
		this.userid = userid;
		this.password = password;
		this.tenant = tenant;
	}

	
	public JSONObject send(NCIPCircTransaction transaction) throws ServiceException, ValidationException, IOException {
		//generates XC NCIP Objects:
		NCIPInitiationData  initiationData = transaction.generateNCIP2Object();
		//transforms the object into NCIP XML:
		InputStream requestMessageStream =  xcToolkitUtil.translator.createInitiationMessageStream(xcToolkitUtil.serviceContext, initiationData);
		
		JSONObject authObject = new JSONObject();
		authObject.put("tenant", tenant);
		authObject.put("username", userid);
		authObject.put("password", password);
		
		String requestBody = IOUtils.toString(requestMessageStream, StandardCharsets.UTF_8);
		logger.info(requestBody);
		String responseString = null;
		JSONObject responseObject = new JSONObject();
		//call to NCIP server
		try {
			CloseableHttpClient client = HttpClients.custom().build();
			
			HttpUriRequest authRequest = RequestBuilder.post()
					.setUri(endpoint + "/authn/login")
					.setEntity(new StringEntity(authObject.toString()))
					.setHeader("Content-Type", "application/json")
					.setHeader("Accept","application/json")
					.setHeader("x-okapi-tenant",tenant)
					.build();
			HttpResponse authResponse = client.execute(authRequest);
			HttpEntity authEntity = authResponse.getEntity();
			responseString = EntityUtils.toString(authEntity, "UTF-8");
			//TODO FOLIO passes back password
			//remove logging response?
			//FOLIO Client will not typically be used
			//Will ususally use NCIP2Client to FOLIO through the edge mod
			logger.info("---> responseString: " + responseString);
			if (authResponse.getStatusLine().getStatusCode() > 399) throw new Exception("UNABLE TO AUTHENTICATE: " + responseString);
			 
			String token = authResponse.getFirstHeader("x-okapi-token").getValue();
			
			HttpUriRequest request = RequestBuilder.post()
					.setUri(this.endpoint + "/ncip")
					.setEntity(new StringEntity(requestBody,"UTF-8"))
					.setHeader("Content-Type", "application/xml")
					.setHeader("x-okapi-token",token)
					.setHeader("x-okapi-tenant",tenant)
					.build();
			
			 HttpResponse response = client.execute(request);
			 HttpEntity entity = response.getEntity();
			 responseString = EntityUtils.toString(entity, "UTF-8");
			 logger.info("NCIP2 request sent: ");
			 logger.info(requestBody);
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
			problem.put("detail","NCIP2 Client failed to call NCIP server or parse returned results");
			problem.put("element",e.getCause());
			problem.put("value", e.getLocalizedMessage());
			array.put(problem);
			responseObject.put("problems", array);			
		}
		return responseObject;
	}
	
	

	public void addHttpHeader(String key, String value) {
		 this.httpHeader.put(key, value);
	}
	
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}



}
