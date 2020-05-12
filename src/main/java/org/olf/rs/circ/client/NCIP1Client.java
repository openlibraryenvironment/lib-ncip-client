package org.olf.rs.circ.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


public class NCIP1Client implements CirculationClient {
	
	private static final Logger logger = Logger.getLogger(NCIP1Client.class);
	protected String endpoint;
	private String userid;
	private String password;
	private boolean useSockets = true;
	private int socketTimeout = 30*1000;

	public NCIP1Client() throws IOException {
		super();
	}
	
	public NCIP1Client(String endpoint) throws IOException {
		super();
		this.endpoint = endpoint;
	}
	
	public NCIP1Client(String endpoint,boolean useSockets) throws IOException {
		super();
		this.endpoint = endpoint;
		this.useSockets = useSockets;
	}
	
	public NCIP1Client(String endpoint,boolean useSockets,int timeout) throws IOException {
		super();
		this.endpoint = endpoint;
		this.useSockets = useSockets;
		this.socketTimeout = timeout;
	}
	
	public JSONObject sendWithSockets(NCIPCircTransaction transaction) throws Exception  {
		
		if (this.getEndpoint() == null) {
			logger.fatal("sendWithSockets called and endpoint is: " + this.getEndpoint());
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		
		
		JSONObject responseObject = new JSONObject();
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
		socket.setSoTimeout(this.getSocketTimeout());
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
		
		try {
	    
			    BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    String line;
			    while ((line = rd.readLine()) != null) {
			    	entireResponse.append(line);
			    	if (error == false && line.contains("HTTP") && (line.contains("40") || line.contains("50"))) {
			    		error = true;
			    		httpResponse = line;
			    	}
			    	//NCIP MESSAGES SEEMS TO BE ON ONE LINE
			    	if (line.contains("NCIPMessage")) {
			    		stringBuffer.append(line);
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
		
		if (error) {
			JSONObject r = constructException(httpResponse,entireResponse.toString(),"exception calling ncip server with socket");
			return r;
		}

		JSONObject r = transaction.constructResponseNcip1Response(stringBuffer.toString());
		return r;
		
	}
	
	
	public JSONObject constructException(String httpResponse,String entireResponse,String element) {
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

	public JSONObject send(NCIPCircTransaction transaction) throws Exception {
		
		if (this.getEndpoint() == null) {
			logger.fatal("send called and endpoint is: " + this.getEndpoint());
			JSONObject r = constructException("Missing Endpoint ", "NCIP Client endpoint is null","");
			return r;
		}
		
		if (this.useSockets) return sendWithSockets(transaction);
		
		JSONObject responseObject = new JSONObject();
		String requestBody = transaction.generateNCIP1Object();
		logger.info(requestBody);
		String responseString = null;
		
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
			 logger.info("NCIP1 request sent: ");
			 logger.info(requestBody);
			 logger.info("to: " + this.endpoint);
			 logger.info("NCIP1 response received: ");
			 logger.info(responseString);
			 int responseCode = response.getStatusLine().getStatusCode();
			 //TODO
			 if (responseCode > 399) {
				JSONObject r = constructException("HTTP Response " + responseCode, responseString,"exception calling ncip 1 server with http");
				return r;
			  }
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
		responseObject = transaction.constructResponseNcip1Response(responseString);
		logger.info(responseObject.toString());
		return responseObject;
	}



	public boolean isUseSockets() {
		return useSockets;
	}

	public void setUseSockets(boolean useSockets) {
		this.useSockets = useSockets;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public String getUserid() {
		return userid;
	}

	public String getPassword() {
		return password;
	}
	
	
	@Override
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	
	

}
