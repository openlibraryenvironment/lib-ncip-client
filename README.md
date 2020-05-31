
# lib-ncip-client
Client for the NISO Circulation Interchange Protocol (NCIP) 

## building the project
mvn package


## Usage
This initial version of the 'NCIP Client' supports four NCIP services:
* LookupUser
* AcceptItem
* CheckInItem
* CheckOutItem

The client supports both NCIP1 and NCIP2. When sending requests to an NCIP1 server, you have the option to use the java.net.Socket class instead of http to support NCIP1 servers that return responses which the http response classes cannot parse.

The latest version of this library includes a NCIPClientWrapper class which you can use to send requests to any protocol version.  You indicate the version when you instantiate the class.  For example

```java
NCIPClientWrapper wrapper = new NCIPClientWrapper("https://ncip.server.endpoint.edu/ncip", NCIPClientWrapper.NCIP2);
NCIPClientWrapper wrapper = new NCIPClientWrapper("https://ncip.server.endpoint.edu/ncip", NCIPClientWrapper.NCIP1);
NCIPClientWrapper wrapper = new NCIPClientWrapper("https://ncip.server.endpoint.edu/ncip", NCIPClientWrapper.NCIP1_SOCKET);
```

You then instantiate the class that represents the service you are calling and call the send method on the client.  The response is a java.util.Map which includes a boolean (success) to indicate whether the call was successful:

### LookupUser
```java
LookupUser lookupUser = new LookupUser()
			  .setToAgency("TST")
			  .setFromAgency("RSH")
			  .setUserId("5551212")
			  .includeNameInformation()
			  .includeUserAddressInformation()
			  .includeUserPrivilege();
Map<String, Object> map = wrapper.send(lookupUser);
```

Response examples:
```
{
		success=true,
		firstName = Jane, 
		lastName = Doe, 
		privileges = {
			Courtesy Notice = true,
			Paging = true,
			Delivery = false,
			Profile = STAFF,
			status = OK
	}, electronicAddresses = {
		emailAddress = indigit @lehigh.edu,
		TEL = 6105551212
	}, physicalAddresses = {
		CAMPUS = {
			"lineTwo": "",
			"postalCode": "",
			"locality": "",
			"lineOne": "30 - Test Library",
			"region": ""
		}
	}, userId = 5551212
}


{success=false, problems=[{detail=User does not exist, type=, value=8765791559, element=USER}]}
```

### AcceptItem
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
			  .setApplicationProfileType("EZBORROW");
Map<String, Object> map = wrapper.send(checkinItem);
```

Response example:
```
{itemId=LEH-20200301608, success=true}
```

You can also use the NCIP1Client or NCIP2Client directly (without the wrapper class).  The examples below all use NCIP2.  To send NCIP1 or NCIP1 w/socket requests use the NCIP1 Client:
```java
NCIP1Client ncip1Client = new NCIP1Client("https://test.ncip.lehigh.edu/ncip");
```

or to use sockets:

```java
NCIP1Client ncip1Client = new NCIP1Client("https://test.ncip.lehigh.edu/ncip",true);
```

Everything else can remain the same.

## 

		
```
### LookupUser
```java
NCIP2Client ncip2Client = new NCIP2Client("https://test.ncip.lehigh.edu/ncip");
LookupUser lookupUser = new LookupUser()
                  .setUserId("876579559")
                  .includeUserAddressInformation()
                  .includeUserPrivilege()
                  .includeNameInformation()
                  .setToAgency("Relais")
                  .setFromAgency("Relais")
                  .setApplicationProfileType("EZBORROW");
JSONObject response = ncip2Client.send(lookupUser);
System.out.println(response);
```
Response examples:
```json
{
	"firstName": "Jane",
	"lastName": "Doe",
	"privileges": [{
			"value": "true",
			"key": "Courtesy Notice"
		}, {
			"value": "false",
			"key": "Delivery"
		},
		{
			"value": "true",
			"key": "Paging"
		}, {
			"value": "STAFF",
			"key": "Profile"
		}, {
			"value": "OK",
			"key": "status"
		}
	],
	"electronicAddresses": [{
		"value": "notreal@lehigh.edu",
		"key": "electronic mail address"
	}, {
		"value": "6105551212",
		"key": "TEL"
	}],
	"userId": "876579559"
}

Response example when there is a problem:
{"problems":[{"detail":"User does not exist","type":"","value":"85551212","element":"USER"}]}

```
### AcceptItem
```java
NCIP2Client ncip2Client = new NCIP2Client("https://test.ncip.lehigh.edu/ncip");
AcceptItem acceptItem = new AcceptItem()
                  .setItemId("LEH-20200305633")
                  .setRequestId("LEH-20200305633")
                  .setUserId("876579559")
                  .setAuthor("Jane Doe") 
                  .setTitle("One Fish Two Fish")
                  .setIsbn("983847293847")
                  .setCallNumber("505.c")
                  .setPickupLocation("FAIRCHILD")
                  .setToAgency("Relais")
                  .setFromAgency("Relais")
                  .setRequestedActionTypeString("Hold For Pickup")
                  .setApplicationProfileType("EZBORROW");
JSONObject response = ncip2Client.send(acceptItem);
System.out.println(response);
```
Response examples:
```json
{"itemId":"LEH-20200305699","requestId":"25388"}

Response with a problem:
{"problems":[{"detail":"Item Barcode Already Exist","type":"","value":"LEH-20200305699","element":"Item"}]}
```

### CheckoutItem
```java
NCIP2Client ncip2Client = new NCIP2Client("https://test.ncip.lehigh.edu/ncip");
CheckoutItem checkoutItem = new CheckoutItem()
                  .setUserId("905808497")
                  .setItemId("LEH-20200305217")
                  .setRequestId("LEH-20200305217")
                  .setToAgency("01TULI_INST")
                  .setFromAgency("01TULI_INST")
                  .setApplicationProfileType("EZBORROW")
                  .setDesiredDueDate("2020-03-18");
JSONObject response = ncip2Client.send(checkoutItem);
System.out.println(response);
```

Response examples:
```json
{"itemId":"LEH-20200305700","dueDate":"2020-06-13 04:00:00","userId":"876579559"}

Response with problem:
{"problems":[{"detail":"Invalid item barcode : LEH-2020030570a",
"type":"","value":"CheckOut Failed","element":""}]}
```

### CheckinItem
```java
NCIP2Client ncip2Client = new NCIP2Client("https://test.ncip.lehigh.edu/ncip");
CheckinItem checkinItem = new CheckinItem()
                  .setItemId("LEH-20200301608")
                  .setToAgency("01TULI_INST")
                  .setFromAgency("01TULI_INST")
                  .includeBibliographicDescription()
                  .setApplicationProfileType("EZBORROW");
JSONObject response = ncip2Client.send(checkinItem);
System.out.println(response);

```
Response examples:
```json
{"itemId":"LEH-20200305700"}

Response with a problem:
{"problems":[{"detail":"Failed to find incoming or outgoing by external item barcode 
LEH-20200305699","type":"Unknown Item"}]}
```

