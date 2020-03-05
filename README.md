
# lib-ncip-client
Client for the NISO Circulation Interchange Protocol (NCIP) 

## building the project
mvn package

## to do
* [ ] Feedback?
* [ ] Write tests
* [ ] More testing

## usage
This initial version of the 'NCIP Client' supports four NCIP2 services:
* LookupUser
* AcceptItem
* CheckInItem
* CheckOutItem



### LookupUser
```java
NCIP2Client ncip2Client = new NCIP2Client();
ncip2Client.setEndpoint("https://test.ncip.lehigh.edu/ncip");
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
### AcceptItem
```java
NCIP2Client ncip2Client = new NCIP2Client();
ncip2Client.setEndpoint("https://test.ncip.lehigh.edu/ncip");
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

### CheckoutItem
```java
NCIP2Client ncip2Client = new NCIP2Client();
ncip2Client.setEndpoint("https://test.ncip.lehigh.edu/ncip");
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

### CheckinItem
```java
NCIP2Client ncip2Client = new NCIP2Client();
ncip2Client.setEndpoint("https://test.ncip.lehigh.edu/ncip");
CheckinItem checkinItem = new CheckinItem()
                  .setItemId("LEH-20200301608")
                  .setToAgency("01TULI_INST")
                  .setFromAgency("01TULI_INST")
                  .includeBibliographicDescription()
                  .setApplicationProfileType("EZBORROW");
JSONObject response = ncip2Client.send(checkinItem);
System.out.println(response);

```

## design & future expansion
There are four objects representing each of the four services.  For the most part, the values in these objects will be needed across other variations of this client like NCIP1 or something else custom that may have to be written.

The NCIP2Client object understands how to ask each object for the NCIP2 xml and how to send the request to an NCIP2 server and how to ask for the NCIP Response object based on the XML returned.  I see this library expanding in the future by creating other Clients (e.g. NCIP1Client) and methods on the objects to support those clients.

The developer using this library can always create one of the four objects and will just need to vary the client object as others are added.
