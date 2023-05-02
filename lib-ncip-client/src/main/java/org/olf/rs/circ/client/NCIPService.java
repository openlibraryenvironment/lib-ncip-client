package org.olf.rs.circ.client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;

public class NCIPService {

	private static final Logger logger = Logger.getLogger(NCIPService.class);

	// If the templatePrefix is set, then append this to the filename of the prefix that we use
	protected String templatePrefix = null;

	public void setTemplatePrefix(String prefix) {
		templatePrefix = prefix;
	}

	public JSONObject constructMissingElementProblem(String missingElement) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject problem = new JSONObject();
		problem.put("type","Missing element");
		problem.put("detail", "Missing element in request: " + missingElement);
		problem.put("element",missingElement);
		problem.put("value", "");
		array.put(problem);
		returnJson.put("problems", array);
		return returnJson;
	}

	public JSONObject constructProblem(NCIPResponseData responseData) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		Iterator<Problem> i = responseData.getProblems().iterator();
		while (i.hasNext()) {
			Problem ncipProblem = i.next();
			JSONObject problem = new JSONObject();
			ncipProblem.getProblemDetail();
			problem.put("type",ncipProblem.getProblemType().getValue());
			problem.put("detail",ncipProblem.getProblemDetail());
			problem.put("element",ncipProblem.getProblemElement());
			problem.put("value", ncipProblem.getProblemValue());
			array.put(problem);
		}
		returnJson.put("problems", array);
		return returnJson;
	}

	public JSONObject constructProblem(Problem ncipProblem) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject problem = new JSONObject();
		problem.put("type",ncipProblem.getProblemType().getValue());
		problem.put("detail",ncipProblem.getProblemDetail());
		problem.put("element",ncipProblem.getProblemElement());
		problem.put("value", ncipProblem.getProblemValue());
		array.put(problem);
		returnJson.put("problems", array);
		return returnJson;
	}

	public JSONObject constructeNcipOneProblems(Elements document) {
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		 Elements problems = document.select("ProcessingError");
		 Iterator iterator = problems.iterator();
		 while (iterator.hasNext()) {
			 JSONObject problem = new JSONObject();
			 Element xmlElement = (Element) iterator.next();
			 String type = xmlElement.select("ProcessingErrorType > value").text();
			 String element = xmlElement.select("ProcessingErrorElement > ElementName").text();
			 String detail = xmlElement.select("ProcessingErrorElement > ProcessingErrorElement > ProcessingErrorValue").text();
			 //SIRSI - SLIGHTLY DIFFERENT ERROR STRUCTURE
			 if (detail.isEmpty()) {
				 detail = xmlElement.select("ProcessingErrorElement > ProcessingErrorValue").text();
			 }
			 problem.put("type",type);
			 problem.put("detail",detail);
			 problem.put("element",element);
			 array.put(problem);
		 }
		returnJson.put("problems", array);
		return returnJson;
	}


	/**
	 * The method generates the NCIP1 request XML
	 * @param templateFileName path including file name to template file
	 */
	public String generateNCIP1Object(String templateFileName) {
		logger.info("generating NCIP 1 request XML");
		Handlebars handlebars = new Handlebars();

		if(templatePrefix != null) {
			Path templatePath = Paths.get(templateFileName);
			String fileName = templatePath.getFileName().toString();
			fileName = templatePrefix + "_" + fileName;
			Path newTemplatePath = Paths.get(templatePath.getParent().toString(), fileName);
			if(newTemplatePath != null) {
				templateFileName = newTemplatePath.toString();
			}
		}

		try {
			Template template = handlebars.compile(templateFileName);
			Context context = Context.newBuilder(this).resolver(MethodValueResolver.INSTANCE,FieldValueResolver.INSTANCE).build();
		    String output =  template.apply(context);
		    //logger.info(output);
		    return output;
		}
		catch(Exception e) {
			logger.fatal("failed to generate the NCIP1 request xml");
			logger.fatal(e.getLocalizedMessage());
		}
		return null;
	}

	public void addProtocolInformation(
			JSONObject responseObject,
			String endPoint,
			String requestBody,
			String responseStatus,
			String responseBody) {
		// Create our json object that we will add the debug information to
		JSONObject protocolInformation = new JSONObject();
		responseObject.put("protocolInformation", protocolInformation);

		// Lets add the request data
		JSONObject requestDetails = new JSONObject();
		protocolInformation.put("request", requestDetails);
		requestDetails.put("endPoint", endPoint);
		requestDetails.put("requestbody", requestBody);

		// Lets add the response data
		JSONObject responseDetails = new JSONObject();
		protocolInformation.put("response", responseDetails);
		responseDetails.put("responseStatus", responseStatus);
		responseDetails.put("responseBody", responseBody);
	}
}
