package org.olf.rs.circ.client.unitTests.transactions;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;




@RunWith(Suite.class)
@Suite.SuiteClasses({
  AcceptItemTests.class,
  CheckinItemTests.class,
  CheckoutItemTests.class,
  LookupUserTests.class,
  RequestItemTests.class
})
public class NcipTransactionsUnitSuiteTest {
	


	  @BeforeClass
	  public static void before()  {

	  }
	  
	  @AfterClass
	  public static void after() {

	  }

}
