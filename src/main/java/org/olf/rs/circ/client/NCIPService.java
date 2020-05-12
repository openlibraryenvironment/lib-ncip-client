package org.olf.rs.circ.client;

import java.util.Iterator;

import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class NCIPService {
	
	public JSONObject constructProblem(NCIPResponseData responseData) {
		
		JSONObject returnJson = new JSONObject();
		JSONArray array = new JSONArray();
		Iterator<Problem> i = responseData.getProblems().iterator();
		while (i.hasNext()) {
			Problem ncipProblem = (Problem)i.next();
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
			 //String value = xmlElement.select("ProcessingErrorElement > ProcessingErrorElement > ElementName").text();
			 problem.put("type",type);
			 problem.put("detail",detail);
			 problem.put("element",element);
			 //problem.put("value", value);
			 array.put(problem);
		 }
		returnJson.put("problems", array);
		return returnJson;
	}
	


}
