package org.olf.rs.circ.client.mockncipserver;




import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;




@RunWith(Suite.class)
@Suite.SuiteClasses({
  NcipOneWithSocket.class,
  NcipOne.class,
})
public class NcipClientTestSuite {
	


	  @BeforeClass
	  public static void before()  {

	  }
	  
	  @AfterClass
	  public static void after() {

	  }

}
