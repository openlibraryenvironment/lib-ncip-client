package org.olf.rs.circ.client;

import java.util.Iterator;

import org.extensiblecatalog.ncip.v2.service.NCIPResponseData;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.json.JSONArray;
import org.json.JSONObject;

public class NCIP2Service {
	
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

}
