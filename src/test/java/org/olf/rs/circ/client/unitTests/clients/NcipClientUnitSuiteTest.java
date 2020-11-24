package org.olf.rs.circ.client.unitTests.clients;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;




@RunWith(Suite.class)
@Suite.SuiteClasses({
  NCIP1ClientTests.class,
  NCIP2ClientTests.class,
  NCIP2WMSClientTests.class,
  NCIPClientWrapperTests.class,
})
public class NcipClientUnitSuiteTest {
	


	  @BeforeClass
	  public static void before()  {

	  }
	  
	  @AfterClass
	  public static void after() {

	  }

}
