
# lib-ncip-client
Client for the NISO Circulation Interchange Protocol (NCIP) 

## building the project
mvn package
## deploy the project to the repository
mvn clean deploy -Pdeploy-ncip-client

## Usage
This initial version of the 'NCIP Client' supports four NCIP services:
* LookupUser
* AcceptItem
* CheckInItem
* CheckOutItem

The client supports both NCIP1 and NCIP2. When sending requests to an NCIP1 server, you have the option to use the java.net.Socket class instead of http to support NCIP1 servers that return responses which the http response classes cannot parse.
<br><br>
Each NCIP Client constructor needs an endpoint parameter (String) and a Map parameter.  The map will contain values needed by each of the clients.
<br><br>
### Potential values to be used in the Map parameter
* apiKey     - Required by WMS NCIP only
* apiSecret  - Required by WMS NCIP only
* oAuthEndpointOverride   - Used by WMS NCIP only to change the default oAuth endpoint (if it is something different than https://oauth.oclc.org/token?grant_type=client_credentials&scope=)
* useSocket - Used by NCIP1 to indicate use socket instead of http (set to true for Aleph).  Defaults to false.
* useNamespace - Used by NCIP2.  Set this to false when you do not want a namespace in the NCIP request XML (Koha).  Defaults to true;  NCIP1 requests do not use a namespace.
* protocol - Use this when instantiating the NCIPClientWrapper to indicate which NCIPClient you want to use.  Valid values are:
	* NCIP1
	* NCIP2
	* NCIP1_SOCKET
	* WMS
* socketTimeout  - Use if you want to change the default timeout for NCIP1 when using NCIP1_SOCKET. The default is 30 seconds. Set to a number (not string)
* lookupPatronEndpoint - Required by WMS NCIP for the LookupUser service

Examples below using each client type:
### NCIP2
* NCIP2 does not require any values in the inputParms Map.  You should send through an empty Map.
```java
Map<String, Object> inputParms = new HashMap<String,Object>();
NCIP2Client ncipTwoClient = new NCIP2Client(endpoint,inputParms);
```

### NCIP1 USING THE SOCKET CLASS
```java
Map<String, Object> inputParms = new HashMap<String,Object>();
inputParms.put("useSocket", true);
NCIP1Client ncipOneClient = new NCIP1Client(endpoint,inputParms);
LookupUser lookupUser = new LookupUser()
```

### NCIP1
```java
Map<String, Object> inputParms = new HashMap<String,Object>(); 
inputParms.put("useSocket", false); //NOT REQUIRED - WILL DEFAULT TO FALSE
NCIP1Client ncipOneClient = new NCIP1Client(endpoint,inputParms);
```

### WMS NCIP
* The AcceptItem service is not supported
* The LookupUser service uses a different endpoint than the CheckIn and CheckOut services
* Include a registryID value in the object the represents the service you are calling e.g. checkoutItem.setRegistryId("128807")
```java
Map<String,Object> inputParms = new HashMap<String,Object>();
inputParms.put("apiKey", "yourapikey");
inputParms.put("apiSecret", "yourapisecret");
NCIP2WMSClient ncipWmsClient = new NCIP2WMSClient(endpoint,inputParms);
```

### NCIPClientWrapper
When using the NCIPClientWrapper class (which can be used for any of the NCIPClients above), add "protocol" to the input parameters to indicate which version of the client you want to use:
```java
inputParms.put("apiKey", "yourapikey"); //only required for WMS
inputParms.put("apiSecret", "yourapisecret"); //only required for WMS
inputParms.put("protocol", NCIPClientWrapper.WMS);
NCIPClientWrapper wrapper = new NCIPClientWrapper(endpoint, inputParms);
```

```java
inputParms.put("protocol", NCIPClientWrapper.NCIP1_SOCKET);
NCIPClientWrapper wrapper = new NCIPClientWrapper(endpoint, inputParms);
```

The NCIPClientWrapper instantiates the indicated client and when send is called, it returns a Map response (instead of a JSONObject)


You then instantiate the class that represents the service you are calling and call the send method on the client.  The response is a java.util.Map (when using NCIPClientWrapper) which includes a boolean (success) to indicate whether the call was successful (a JSONObject is returned for the other client classes):

### LookupUser
* NCIP WMS - will use a different endpoint for LookupUser (vs CheckIn and CheckOut)
```java
LookupUser lookupUser = new LookupUser()
			  .setToAgency("TST")
			  .setFromAgency("RSH")
			  .setUserId("5551212")
			  .includeNameInformation()
			  .includeUserAddressInformation()
			  .setRegistryId("128807")
			  .includeUserPrivilege();
Map<String, Object> map = wrapper.send(lookupUser);
```

Response examples:
```javascript
{
	firstName = JANE, lastName = DOE, 
	 privileges = {
		STATUS = OK,
		PROFILE = faculty,
		LIBRARY =
	 }, 
	 success = true, 
	 electronicAddresses = {
		emailAddress = janedoe@notreal.edu,
		TEL = 6105551212
	}, 
	physicalAddresses = {}, 
	userId = 871129834
}


{success=false, problems=[{detail=User does not exist, type=, value=8765791559, element=USER}]}
```

### AcceptItem
* WMS NCIP does not support AcceptItem
```java
AcceptItem acceptItem = new AcceptItem()
			  .setItemId("LEH-20200305633")
			  .setRequestId("LEH-20200305633")
			  .setUserId("5551212")
			  .setAuthor("Jane Doe") 
			  .setTitle("One Fish Two Fish")
			  .setIsbn("983847293847")
			  .setCallNumber("505.c")
			  .setPickupLocation("FAIRCHILD")
			  .setToAgency("Relais")
			  .setFromAgency("Relais")
			  .setRequestedActionTypeString("Hold For Pickup")
			  .setApplicationProfileType("EZBORROW");
Map<String, Object> map = wrapper.send(acceptItem);
```
Note: The NCIP1 Client sends pickupLocation through the initiationHeader 'toAgency' ID.
<br><br>
Response examples
```
{itemId=LEH-20200305633, requestId=LEH-20200305633,success=true}
{success=false, problems=[{detail=Item Barcode Already Exist, type=, value=LEH-20200526430, element=Item}]}
```

### CheckoutItem
```java
CheckoutItem checkoutItem = new CheckoutItem()
			  .setUserId("5551212")
			  .setItemId("LEH-20200305217")
			  .setRequestId("LEH-20200305217")
			  .setToAgency("TST")
			  .setFromAgency("RSH")
			  .setRegistryId("128807") //WMS ONLY
			  .setApplicationProfileType("EZBORROW")
Map<String, Object> map = wrapper.send(checkoutItem);
		  
```

Response example
```
{itemId=LEH-20200305217, success=true, dueDate=2021-05-26 04:00:00, userId=5551212}

```

### CheckinItem
```java
CheckinItem checkinItem = new CheckinItem()
			  .setItemId("LEH-20200301608")
			  .setToAgency("TST")
			  .setFromAgency("RSH")
			  .includeBibliographicDescription()
			  .setRegistryId("128807") //WMS ONLY
			  .setApplicationProfileType("EZBORROW");
Map<String, Object> map = wrapper.send(checkinItem);
```

Response example:
```
{itemId=LEH-20200301608, success=true}
```

### Printing
Each of the transaction objects (e.g. CheckoutItem) has a toString() method that will return a string to show each variable and value in the instance.
<br>
Each of the client objects (e.g. NCIP2WMSClient) has a printRequest method that accepts a transaction as an input parameter and returns the payload that will be generated and sent to the NCIP service.  (example:   ncip2WmsClient.printRequest(acceptItem))
		

