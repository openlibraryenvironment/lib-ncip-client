package org.olf.rs.circ.client.unitTests.clients;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;




@RunWith(Suite.class)
@Suite.SuiteClasses({
  NCIP1ClientTests.class,
  NCIP2ClientTests.class,
  NCIPClientWrapperTests.class,
})
public class NcipClientUnitTestSuite {
	


	  @BeforeClass
	  public static void before()  {

	  }
	  
	  @AfterClass
	  public static void after() {

	  }

}
