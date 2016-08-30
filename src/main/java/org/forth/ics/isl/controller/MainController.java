package org.forth.ics.isl.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.forth.ics.isl.data.model.EndPointDataPage;
import org.forth.ics.isl.data.model.EndPointForm;
import org.forth.ics.isl.enums.QueryResultFormat;
import org.forth.ics.isl.service.BlazegraphRepRestful;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The main controller responsible for submitting queries, managing page content with a serverside pagination, etc
 * 
 * @author Vangelis Kritsotakis
 */

@Controller
public class MainController {
	
	String propFile = "/config/quads.properties";
    String service = "http://139.91.183.88:9999/blazegraph";
    BlazegraphRepRestful blaze = new BlazegraphRepRestful(service);
    JsonNode currQueryResult = new ObjectNode(JsonNodeFactory.instance);
	
    @RequestMapping(value="/",method = RequestMethod.GET)
    public String homepage(){
        return "index";
    }
    
    
    @RequestMapping(value = "/angularjs-http-service-ajax-post-json-data-code-example", method = RequestMethod.GET)
    public ModelAndView httpServicePostJSONDataExample( ModelMap model ) {
    	return new ModelAndView("httpservice_post_json");
    }
/*
    @RequestMapping(value = "/executequery_json", method = RequestMethod.POST)	
    public  @ResponseBody EndPointForm executequery_json( @RequestBody EndPointForm endPointForm )   {		
    	//
    	// Code processing the input parameters
    	//	
    	System.out.println("executequery_json");
    	System.out.println("Query: " + endPointForm.getQuery());
    	//return "JSON: Query: " + endPointForm.getQuery() + ", Output: " + endPointForm.getResult();
    	return endPointForm;
    }
    */
    
    
    //http://localhost:8080/index.html#/
    /*
    @RequestMapping(value = "/executequery_json", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody EndPointForm PostExecutequery(@RequestBody EndPointForm endPointForm) {
    	System.out.println("endPointForm.getQuery(): " + endPointForm.getQuery());
    	
    	try {
    		Response blazeResponce = blaze.executeSparqlQuery(endPointForm.getQuery(), "testnamespace", QueryResultFormat.JSON);
			//endPointForm.setResult(blazeResponce.readEntity(String.class));
			
    		System.out.println("blazeResponce.getStatus(): "  + blazeResponce.getStatusInfo());
    		
    		// Setting Response status to POJO
    		endPointForm.setStatusRequestCode(blazeResponce.getStatus());
			endPointForm.setStatusRequestInfo(blazeResponce.getStatusInfo().toString());
    		
			// In case of OK status handle the response
    		if (blazeResponce.getStatus() == 200) {
	    		// Serializing in pojo
	    		ObjectMapper mapper = new ObjectMapper();
	    		// Holding JSON in jsonNode
				JsonNode queryResult = mapper.readValue(blazeResponce.readEntity(String.class), JsonNode.class);
				endPointForm.setResult(queryResult);
    		}
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
		return endPointForm;
	}
    */
    
    @RequestMapping(value = "/executequery_json", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody EndPointForm PostExecutequery(@RequestBody EndPointForm endPointForm) {
    	System.out.println("endPointForm.getQuery(): " + endPointForm.getQuery());
    	
    	try {
    		Response blazeResponce = blaze.executeSparqlQuery(endPointForm.getQuery(), "testnamespace", QueryResultFormat.JSON);
			
    		System.out.println("blazeResponce.getStatus(): "  + blazeResponce.getStatusInfo());
    		
    		// Setting Response status to POJO
    		endPointForm.setStatusRequestCode(blazeResponce.getStatus());
			endPointForm.setStatusRequestInfo(blazeResponce.getStatusInfo().toString());
    		
			// In case of OK status handle the response
    		if (blazeResponce.getStatus() == 200) {
	    		// Serializing in pojo
	    		ObjectMapper mapper = new ObjectMapper();
	    		
	    		// Holding JSON in jsonNode globally
	    		currQueryResult = mapper.readValue(blazeResponce.readEntity(String.class), JsonNode.class);
	    		//System.out.println("currQueryResult: "  + currQueryResult);
	    		// Holding the first page in separate JsonNode
				JsonNode queryResult = getDataOfPageForCurrentEndPointForm(1, endPointForm.getItemsPerPage());
				
				// Setting results and total items for the response
				endPointForm.setResult(queryResult);
				endPointForm.setTotalItems(currQueryResult.get("results").get("bindings").size());
				
				//System.out.println(endPointForm.getResult());

    		}
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
		return endPointForm;
	}
    
    
    @RequestMapping(value = "/paginator_json", method = RequestMethod.GET, produces={"application/json"})
	public @ResponseBody EndPointDataPage loadDataForPage(@RequestParam Map<String,String> requestParams, Model model) {
    	
    	int page = new Integer(requestParams.get("page")).intValue();
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();
		
    	// The EndPointForm for the page
    	EndPointDataPage endPointDataPage = new EndPointDataPage();
    	endPointDataPage.setPage(page);
    	endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage));
    	
    	//System.out.println(endPointDataPage.getResult());
    	
		return endPointDataPage;
	}
    
    /**
     * Constructs an ObjectNode for one page only, based on the passed page and the whole data
     *
     * @param page The 		page number
     * @param itemsPerPage 	The number of items per page
     * @return 				the constructed ObjectNode
     */
    private ObjectNode getDataOfPageForCurrentEndPointForm(int page, int itemsPerPage) {
    	    	    	    	
    	//{"head":{"vars":["s","p","o"]},"results":{"bindings":[{"s":{... "p":{...
    	JsonNodeFactory factory = JsonNodeFactory.instance;
    	
    	// vars
		ObjectNode varsObjectNode = new ObjectNode(factory);
		varsObjectNode.set("vars", currQueryResult.get("head").get("vars"));
		
		// bindings
		ArrayNode bindingsArrayNode = new ArrayNode(factory);
		
    	for (int i=0; i<itemsPerPage; i++) {
    		if(currQueryResult.get("results").get("bindings").hasNonNull(i+(page-1)*itemsPerPage))
    		bindingsArrayNode.add(currQueryResult.get("results").get("bindings").get(i+(page-1)*itemsPerPage));
    	}
    	  	
    	// Final ResultObject
    	ObjectNode resultObjectNode = new ObjectNode(factory);
    	resultObjectNode.set("head", varsObjectNode);
    	resultObjectNode.set("results", bindingsArrayNode);
    	
    	return resultObjectNode;
    	
    }
    
    // Todo: use offset and limit and orderBy and groupBy at the end
    // select * where {?s ?p ?o} offset 100 limit 40 
    
}