package org.olf.rs.circ.client.manualTesting;

import java.util.Map;

import org.olf.rs.circ.client.AcceptItem;
import org.olf.rs.circ.client.CheckinItem;
import org.olf.rs.circ.client.CheckoutItem;
import org.olf.rs.circ.client.LookupUser;
import org.olf.rs.circ.client.NCIPClientWrapper;

public class Wrapper {

	public static void main(String[] args) throws Exception {
		NCIPClientWrapper wrapper = new NCIPClientWrapper("http://example.edu:5994/ncip", NCIPClientWrapper.NCIP1_SOCKET);
		LookupUser lookupUser = new LookupUser()
									  .setToAgency("TST")
									  .setFromAgency("RSH")
									  .setUserId("ABCEZMVS1")
									  //.setApplicationProfileType("E-ZBorrow")
									  .includeNameInformation()
									  .includeUserAddressInformation()
									  .includeUserPrivilege();
		Map<String, Object> map = wrapper.send(lookupUser);
		System.out.println(map.toString());
		
		AcceptItem acceptItem = new AcceptItem()
								.setToAgency("TST")
								.setFromAgency("RSH")
								.setPickupLocation("TNSGI")
								.setAuthor("Jane Doe")
								.setUserId("ABCEZMVS1")
								.setTitle("Test Title")
								.setRequestActionType("Hold For Pickup")
								.setRequestId("TST-20200526429")
								.setItemId("TST-20200526429");
		
		map = wrapper.send(acceptItem);
		System.out.println(map.toString());

		CheckoutItem checkoutItem = new CheckoutItem()
								  .setToAgency("TST")
								  .setFromAgency("RSH")
								  .setItemId("355512139746403")
								  .setRequestId("TST-20200526430")
								  .setUserId("ABCEZMVS1");
		
		map = wrapper.send(checkoutItem);
		System.out.println(map.toString());
		
		
		CheckinItem checkinItem = new CheckinItem()
								  .setToAgency("TST")
								  .setFromAgency("RSH")
								  .setItemId("355512139746403");
		
		map = wrapper.send(checkinItem);
		System.out.println(map.toString());
	}

}
