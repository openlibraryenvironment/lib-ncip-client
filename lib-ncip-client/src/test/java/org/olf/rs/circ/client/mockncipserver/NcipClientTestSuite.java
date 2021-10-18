package org.olf.rs.circ.client.mockncipserver;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This collection of tests require a running 'mock ncip server'.
 * The endpoint has to be set in the tests.
 * 
 *
 */


@RunWith(Suite.class)
@Suite.SuiteClasses({
  NcipOneWithSocket.class,
  NcipOne.class,
  NcipTwo.class,
  NcipWms.class
})
public class NcipClientTestSuite {
	


	  @BeforeClass
	  public static void before()  {

	  }
	  
	  @AfterClass
	  public static void after() {

	  }

}
