package org.olf.rs.circ.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.Thread;

/**
 * This class sends a POST request to NCIP v1 servers.
 * If useSocket is set to true, it will use java.net.Socket (not http)
 * to call the services.
 * @author mis306
 *
 */
public class NCIP1Client implements CirculationClient {
	
	private static final Logger logger = Logger.getLogger(NCIP1Client.class);
	protected String endpoint;
	private boolean useSocket = false;
	private boolean strictSocket = false;
	private int socketTimeout = 30*1000;


	public NCIP1Client(String endpoint, Map<String,Object> inputParms) throws NCIPClientException {
		super();
		CaseInsensitiveMap<String,Object> inputMap = new CaseInsensitiveMap<String,Object>();
		inputMap.putAll(inputParms);
		this.endpoint = endpoint;
		try {
			if (inputMap.containsKey("useSocket")) {
				this.useSocket = (boolean) inputMap.get("useSocket");
			}
			if (inputMap.containsKey("socketTimeout")) {
				this.socketTimeout = (int) inputMap.get("socketTimeout");
			}
			if (inputMap.containsKey("strictSocket")) {
				this.strictSocket = (boolean) inputMap.get("strictSocket");
			}
		}
		catch(Exception e) {
			throw new NCIPClientException(e.getLocalizedMessage());
		}
	}
	
	
	private JSONObject strictSocket(NCIPCircTransaction transaction) throws Exception  {
		if (this.endpoint == null) {
			logger.fatal("sendWithSockets (strict) called and endpoint is: " + this.endpoint);
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		
		JSONObject errors = transaction.validateRequest();
		if (errors != null) return errors;
		
		String requestBody = transaction.generateNCIP1Object();
		//SPLIT UP THE ENDPONT 
		URI uri = new URI (this.endpoint);
		int port = uri.getPort();
		if (port == 0) port = 80;
		String baseUri = uri.getHost();
		String path = uri.getPath();
		logger.info("baseUrl: " + baseUri);
		logger.info("path: " + path);
		logger.info("port: " + port);

		Socket socket = new Socket(baseUri,port);
		//socket.setSoTimeout(5000);
		//socket.setKeepAlive(true);
		
		DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(
			socket.getInputStream()));
		toServer.writeBytes(requestBody + "\n");
		String line = null;
		StringBuffer buffer = new StringBuffer();
		final int sleepLength = 500;
		final int maxSleeps = 20;
		int sleeps = 0;
		try {
			while(true) {
				if(fromServer.ready()) {
					sleeps = 0; //reset counter
					line = fromServer.readLine();
					logger.info("Read line: '" + line + "'");
					if(line == null) {
						break;
					} else {
						buffer.append(line);
					}
				} else {					
					if( sleeps >= maxSleeps) {
						logger.info("Max time exceeded for waiting on server");
						break;
					} else {
						//logger.info("Server is not ready to read");
						Thread.sleep(sleepLength);
						sleeps = sleeps + 1;
					}
				}
			}
		} catch(Exception e) {
			logger.info("Error reading from socket: " + e.getLocalizedMessage());	
		}

		try {
			logger.info("Attempting to close socket");
			socket.close();
		} catch(Exception e) {
			logger.error("Error closing socket: " + e.getLocalizedMessage());
		}
		
		if (buffer.toString().trim().isEmpty()) {
			JSONObject r = constructException("response is empty",buffer.toString(),"exception calling ncip server with strict socket");
			return r;
		}
		
		try {
			logger.info("full response:");
			logger.info(buffer.toString());
			JSONObject r = transaction.constructResponseNcip1Response(buffer.toString());
			return r;
		}
		catch(Exception e) {
			JSONObject r = constructException(e.getLocalizedMessage(),buffer.toString(),"exception calling ncip server with ");
			return r;
		}
		
	}
	
	/**
	 * Send NCIP request using java.net.Socket
	 *
	 */
	private JSONObject sendWithSockets(NCIPCircTransaction transaction) throws Exception  {
		
		if (this.endpoint == null) {
			logger.fatal("sendWithSockets called and endpoint is: " + this.endpoint);
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		
		JSONObject errors = transaction.validateRequest();
		if (errors != null) return errors;
		
		String requestBody = transaction.generateNCIP1Object();
		//SPLIT UP THE ENDPONT 
		URI uri = new URI (this.endpoint);
		int port = uri.getPort();
		if (port == 0) port = 80;
		String baseUri = uri.getHost();
		String path = uri.getPath();
		logger.info("baseUrl: " + baseUri);
		logger.info("path: " + path);
		logger.info("port: " + port);

		Socket socket = new Socket(baseUri,port);
		socket.setSoTimeout(socketTimeout);
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
	    wr.write("POST " + path + " HTTP/1.0\r\n");
	    wr.write("Content-Length: " + requestBody.length() + "\r\n");
	    wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
	    wr.write("\r\n");
	    wr.write(requestBody);
	    wr.flush();
	    
		logger.info("NCIP1 SOCKET request sent: ");
	    logger.info(requestBody);
		logger.info("to: " + this.endpoint);
		
	    StringBuffer stringBuffer = new StringBuffer();
	    StringBuffer entireResponse = new StringBuffer();
	    String httpResponse = "";
	    boolean error = false;
			final String xmlRegex = "<\\?xml.+?\\?>";
			final String docRegex = "<!DOCTYPE.+?>";
			final String bigRegex = "<\\?xml.+<NCIPMessage.+</NCIPMessage>"; //In case everything NCIP is on one line
		
		try {
	    
			    BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    String line;
					boolean isNCIPMessage = false;
			    while ((line = rd.readLine()) != null) {
					logger.info("READING RESPONSE LINES");
					logger.info(line);
			    	entireResponse.append(line);
			    	if (error == false && line.contains("HTTP") && (line.contains("40") || line.contains("50"))) {
			    		error = true;
			    		httpResponse = line;
			    	}

						if( line.matches(bigRegex)) {
							stringBuffer.append(line);
							continue;
						}
						
						if( line.matches(xmlRegex)) {
							stringBuffer.append(line);
							continue;
						}

						if( line.matches(docRegex)) {
							stringBuffer.append(line);
							continue;
						}
						/* 
			    	//NCIP MESSAGES SEEMS TO BE ON ONE LINE
			    	if (line.contains("NCIPMessage")) {
			    		stringBuffer.append(line);
			    	}
						*/
						if (line.contains("<NCIPMessage")) { //Open tag
							isNCIPMessage = true;
						}
						if( isNCIPMessage) {
							stringBuffer.append(line);
						}

						if (line.contains("</NCIPMessage>")) { //Close tag
							isNCIPMessage = false;
						}
			    }
			    wr.close();
			    rd.close();
			    socket.close();
		}
		catch(SocketTimeoutException socketTimeoutException) {
			JSONObject r = constructException("Socket call timeout","no response","exception calling ncip server with socket");
			return r;
		}
		catch(Exception e) {
			JSONObject r = constructException("Exception during socket call" , e.getLocalizedMessage(),"exception calling ncip server with socket");
			return r;
		}
		
		logger.info("NCIP1 SOCKET response received: ");
		logger.info(entireResponse.toString()); 
		
		if (error || entireResponse.toString() == null || entireResponse.toString().isEmpty()) {
			JSONObject r = constructException(httpResponse,entireResponse.toString(),"exception calling ncip server with socket");
			return r;
		}

		try {
			logger.info("Constructing NCIP response with string: " + stringBuffer.toString());
			JSONObject r = transaction.constructResponseNcip1Response(stringBuffer.toString());
			return r;
		}
		catch(Exception e) {
			JSONObject r = constructException(httpResponse,entireResponse.toString(),"exception calling ncip server with socket");
			return r;
		}
		
	}
	
	
	private JSONObject constructException(String httpResponse, String entireResponse,String element) {
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

	/**
	 * If useSocket is set to false, send NCIP request using http,
	 * otherwise call sendWithSockets
	 */
	public JSONObject send(NCIPCircTransaction transaction)  {
		
		if (this.endpoint == null) {
			logger.fatal("send called and endpoint is: " + this.endpoint);
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		JSONObject errors = transaction.validateRequest();
		if (errors != null) return errors;
		
		try {
			if (this.useSocket && this.strictSocket) {
				logger.info("Sending with strict sockets");
				return strictSocket(transaction);
			}
			if (this.useSocket) {
				logger.info("Sending with sockets");
				return sendWithSockets(transaction);
			}
		}
		catch(Exception e) {
			JSONObject responseObject = new JSONObject();
			JSONArray array = new JSONArray();
			JSONObject problem = new JSONObject();
			problem.put("type","NCIP1 with socket Client failed to call NCIP server or parse returned results");
			problem.put("element",e.getCause());
			problem.put("detail", e.getLocalizedMessage());
			problem.put("exception", e.getClass().getCanonicalName());
			array.put(problem);
			responseObject.put("problems", array);			
			logger.info(responseObject.toString());
			return responseObject;
		}
				
		JSONObject responseObject = new JSONObject();
		String requestBody = transaction.generateNCIP1Object();
		logger.info(requestBody);
		String responseString = null;
		
		try {
			
			//added sslConnectionSocketFactory for Sirsi NCIP 03-2021
			SSLContext sslContext = SSLContexts.custom()
			        .loadTrustMaterial((chain, authType) -> true).build();

			SSLConnectionSocketFactory sslConnectionSocketFactory =
			        new SSLConnectionSocketFactory(sslContext, new String[]
			        //{"SSLv2Hello", "SSLv3", "TLSv1","TLSv1.1", "TLSv1.2" }, null,
							{"SSLv3", "TLSv1","TLSv1.1", "TLSv1.2" }, null,
			        NoopHostnameVerifier.INSTANCE);
			
			CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
			//CloseableHttpClient client = HttpClients.custom().build();
			HttpUriRequest request = RequestBuilder.post()
					.setUri(this.endpoint)
					.setEntity(new StringEntity(requestBody,"UTF-8"))
					.setHeader("Content-Type", "application/xml")
					.build();
			
			 HttpResponse response = client.execute(request);
			 HttpEntity entity = response.getEntity();
			 responseString = EntityUtils.toString(entity, "UTF-8");
			 logger.info("NCIP1 request sent: ");
			 logger.info(requestBody);
			 logger.info("to: " + this.endpoint);
			 logger.info("NCIP1 response received: ");
			 logger.info(responseString);
			 int responseCode = response.getStatusLine().getStatusCode();
			 if (responseCode > 399) {
				JSONObject r = constructException("HTTP Response " + responseCode, responseString,"exception calling NCIP 1 server with http");
				return r;
			  }
		}
		catch(Exception e) {
			JSONArray array = new JSONArray();
			JSONObject problem = new JSONObject();
			problem.put("type","NCIP2 Client failed to call NCIP server or parse returned results");
			problem.put("element",e.getCause());
			problem.put("detail", e.getLocalizedMessage());
			problem.put("exception", e.getClass().getCanonicalName());
			array.put(problem);
			responseObject.put("problems", array);			
			return responseObject;
		}
		try {
			responseObject = transaction.constructResponseNcip1Response(responseString);
			logger.info(responseObject.toString());
			return responseObject;
		}
		catch(Exception e) {
			JSONObject r = constructException("","unable to parse response from NCIP1 return",e.getLocalizedMessage());
			return r;
		}
	}

	@Override
	public String printRequest(NCIPCircTransaction transaction) throws NCIPClientException {
		return transaction.generateNCIP1Object();
	}

}
