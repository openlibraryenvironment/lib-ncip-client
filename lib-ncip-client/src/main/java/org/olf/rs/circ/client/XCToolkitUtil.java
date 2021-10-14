package org.olf.rs.circ.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.common.ServiceValidatorFactory;
import org.extensiblecatalog.ncip.v2.common.Translator;
import org.extensiblecatalog.ncip.v2.common.TranslatorFactory;
import org.extensiblecatalog.ncip.v2.service.ServiceContext;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;


public class XCToolkitUtil {
	
	private static final Logger logger = Logger.getLogger(XCToolkitUtil.class);
	private static volatile  XCToolkitUtil xcToolkitUtilInstance;
	public Translator translator;
	public ServiceContext serviceContext;
	
	private XCToolkitUtil() {
		if (xcToolkitUtilInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
		
	};
	
	public static XCToolkitUtil getInstance() throws IOException, ToolkitException {
		if (xcToolkitUtilInstance == null) {
			
			synchronized(XCToolkitUtil.class) {
				if (xcToolkitUtilInstance == null) xcToolkitUtilInstance = new XCToolkitUtil();
				
				InputStream inputStream = XCToolkitUtil.class.getClassLoader().getResourceAsStream(Constants.TOOLKIT_PROP_FILE); 
				logger.info("initializing the XC NCIP Toolkit Property File...");
				Properties properties = new Properties();
				properties.load(inputStream);
				
				if (properties.isEmpty()) {
					logger.fatal("Unable to initialize the default toolkit properties.");
					throw new RuntimeException("Unable to initialize the XC NCIP Toolkit property file.");
				}
				
				xcToolkitUtilInstance.serviceContext = ServiceValidatorFactory.buildServiceValidator(properties).getInitialServiceContext();
				xcToolkitUtilInstance.translator = TranslatorFactory.buildTranslator(null,properties);
				return xcToolkitUtilInstance;
			}
			
		}
		return xcToolkitUtilInstance;
	}

}
