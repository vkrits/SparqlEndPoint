package org.forth.ics.isl.controller;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.springframework.context.annotation.ScopedProxyMode;

import org.forth.ics.isl.data.model.ConfigProperty;
import org.forth.ics.isl.data.model.EndPointDataPage;
import org.forth.ics.isl.data.model.EndPointForm;
import org.forth.ics.isl.data.model.IncomingOutgoingURIs;
import org.forth.ics.isl.enums.QueryResultFormat;
import org.forth.ics.isl.service.BlazegraphRepRestful;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;
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

@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
@Controller
public class MainController {
		
	@Value("${triplestore.url}")
	private String service; // = environment.getProperty("triplestore.url");
	@Value("${triplestore.namespace}")
	private String namespace;
	@Value("${rdf.base}")
	private String rdfBase;
	@Value("${rdf.afterbase}")
	private String rdfAfterbase;
	private BlazegraphRepRestful blaze;
	private JsonNode currQueryResult;
	private JsonNode outgoingUrisResult;
	private JsonNode incomingUrisResult;
	
    @PostConstruct
    public void init() {
    	blaze = new BlazegraphRepRestful(service);
        currQueryResult = new ObjectNode(JsonNodeFactory.instance);
        outgoingUrisResult = new ObjectNode(JsonNodeFactory.instance);
        incomingUrisResult = new ObjectNode(JsonNodeFactory.instance);
    }
    
    @RequestMapping(value="/",method = RequestMethod.GET)
    public String homepage() {
        return "index";
    }
    
    
    @RequestMapping(value = "/angularjs-http-service-ajax-post-json-data-code-example", method = RequestMethod.GET)
    public ModelAndView httpServicePostJSONDataExample( ModelMap model ) {
    	return new ModelAndView("httpservice_post_json");
    }
    
    @RequestMapping(value = "/prefix/{afterfix}/**", method = RequestMethod.GET)
    public @ResponseBody IncomingOutgoingURIs find(@PathVariable String afterfix, WebRequest request, HttpServletRequest httpServletRequest) {
    	
    	String url = httpServletRequest.getRequestURI();
		System.out.println("url: " + url);
        String mvcPath = (String) request.getAttribute(
        		HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    	
        //System.out.println("mvcPrefix: " + mvcPrefix);
        System.out.println("mvcPath: " + mvcPath);
    	    	
        String uri = rdfBase + mvcPath;
        //String uri = rdfBase + "/prefix/" + afterfix + "/";
        
    	int itemsPerPage = 20;
    	System.out.println("Retrieving info for URI: " + uri);
    	
    	// Used in order to hold everything and then be returned
    	IncomingOutgoingURIs incomingOutgoingURIs = new IncomingOutgoingURIs();
    	incomingOutgoingURIs.setUri(uri);
    	
    	// Retrieving Outgoing URIs
    	
    	// POJO EndPointForm instance, used for holding Outgoing URIs
    	EndPointForm outgoingEndPointForm = new EndPointForm();
    	outgoingEndPointForm.setItemsPerPage(itemsPerPage);
    	
    	// Setting query
    	String outgoingQueryStr = "select * where {<" + uri + "> ?p ?o}";
    	outgoingEndPointForm.setQuery(outgoingQueryStr);
    	
    	// Retrieving items based on query an holding them in EndPointForm POJO
    	outgoingEndPointForm = retrieveBasedOnQuery(outgoingEndPointForm);
    	
    	// Checking saved status
    	if (outgoingEndPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
    		outgoingUrisResult = outgoingEndPointForm.getResult();
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, outgoingEndPointForm.getItemsPerPage(), "outgoing");
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			outgoingEndPointForm.setResult(firstPageQueryResult);
			
			// Set to IncomingOutgoingURIs
			incomingOutgoingURIs.setOutgoingEndPointForm(outgoingEndPointForm);
			
    	}
    	    	
    	// Retrieving Incoming URIs
    	
    	// POJO EndPointForm instance used for holding the incoming URIs
    	EndPointForm incomingEndPointForm = new EndPointForm();
    	incomingEndPointForm.setItemsPerPage(itemsPerPage);
    	
    	// Setting query
    	String incomingQueryStr = "select * where { ?s ?p <" + incomingOutgoingURIs.getUri() + ">}";
    	incomingEndPointForm.setQuery(incomingQueryStr);
    	
    	// Retrieving items based on query an holding them in EndPointForm POJO
    	incomingEndPointForm = retrieveBasedOnQuery(incomingEndPointForm);
    	
    	// Checking saved status
    	if (incomingEndPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
    		incomingUrisResult = incomingEndPointForm.getResult();
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, incomingEndPointForm.getItemsPerPage(), "incoming");
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			incomingEndPointForm.setResult(firstPageQueryResult);
			
			// Set to IncomingOutgoingURIs
			incomingOutgoingURIs.setIncomingEndPointForm(incomingEndPointForm);
    	}

    	return incomingOutgoingURIs;
	
    }
    
    
    
    
    /**
     * Method used many times that retrieves data based on an EndPointForm POJO (from which 
     * it mainly uses the query variable)
     *
     * @param endPointForm 	An EndPointForm object some of the variables of which are filled
     * 						in and the rest will be filled after the data retrieval is completed.
     * @return 				A processed EndPointForm object, all the fields of which are 
     * 						filled in. However the field "Result" will be reset soon.
     */
    private EndPointForm retrieveBasedOnQuery(EndPointForm endPointForm) {

    	try {
    		Response blazeResponce = blaze.executeSparqlQuery(endPointForm.getQuery(), namespace, QueryResultFormat.JSON);
    		System.out.println("blazeResponce.getStatus(): "  + blazeResponce.getStatusInfo());
    		
    		// Setting Response status to POJO
    		endPointForm.setStatusRequestCode(blazeResponce.getStatus());
			endPointForm.setStatusRequestInfo(blazeResponce.getStatusInfo().toString());
    		
			// In case of OK status handle the response
    		if (blazeResponce.getStatus() == 200) {
    			
	    		// Serializing in pojo
	    		ObjectMapper mapper = new ObjectMapper();
	    		// Holding JSON in jsonNode globally
	    		JsonNode queryResult = mapper.readValue(blazeResponce.readEntity(String.class), JsonNode.class);
				// Setting results for the response (for now we set them all and 
	    		// later we will replace them with those at the first page)
				endPointForm.setResult(queryResult);
				// Setting total items for the response
				endPointForm.setTotalItems(queryResult.get("results").get("bindings").size());
				
    		}
    		
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
		return endPointForm;
    }
    
    /**
     * Request post for submitting a query to the SPARQL end point and returning the data
     *
     * @param endPointForm 		An EndPointForm object some of the variables of which are 
     * filled in and the rest will be filled after the data retrieval is completed.
     * @return 					An EndPointForm object with all of its fields been filled in.
     */
    @RequestMapping(value = "/executequery_json", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody EndPointForm postExecuteQuery(@RequestBody EndPointForm endPointForm) {
    	System.out.println("endPointForm.getQuery(): " + endPointForm.getQuery());
    	
    	// Retrieving items based on query an holding them in EndPointForm pojo
    	endPointForm = retrieveBasedOnQuery(endPointForm);
    	
    	// Checking saved status
    	if (endPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
	    	currQueryResult = endPointForm.getResult();
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, endPointForm.getItemsPerPage(), "query");
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			endPointForm.setResult(firstPageQueryResult);
			
    	}
		return endPointForm;
	}
    
    /**
     * Request post for retrieving information based on a URI
     *
     * @param URI 			String representation of the URI
     * @return 				A JSON object that holds the outgoing and incoming URIs and literals
     */
    @RequestMapping(value = "/retrieve_uri_info_json", method = RequestMethod.POST, produces={"application/json"})
	public @ResponseBody IncomingOutgoingURIs getRetrieveUriInfo(@RequestParam Map<String,String> requestParams) {
    	
    	String uri = requestParams.get("uri");
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();
    	System.out.println("Retrieving info for URI: " + uri);
    	
    	// Used in order to hold everything and then be returned
    	IncomingOutgoingURIs incomingOutgoingURIs = new IncomingOutgoingURIs();
    	incomingOutgoingURIs.setUri(uri);
    	
    	// Retrieving Outgoing URIs
    	
    	// POJO EndPointForm instance, used for holding Outgoing URIs
    	EndPointForm outgoingEndPointForm = new EndPointForm();
    	outgoingEndPointForm.setItemsPerPage(itemsPerPage);
    	
    	// Setting query
    	String outgoingQueryStr = "select * where {<" + uri + "> ?p ?o}";
    	outgoingEndPointForm.setQuery(outgoingQueryStr);
    	
    	// Retrieving items based on query an holding them in EndPointForm POJO
    	outgoingEndPointForm = retrieveBasedOnQuery(outgoingEndPointForm);
    	
    	// Checking saved status
    	if (outgoingEndPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
    		outgoingUrisResult = outgoingEndPointForm.getResult();
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, outgoingEndPointForm.getItemsPerPage(), "outgoing");
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			outgoingEndPointForm.setResult(firstPageQueryResult);
			
			// Set to IncomingOutgoingURIs
			incomingOutgoingURIs.setOutgoingEndPointForm(outgoingEndPointForm);
			
    	}
    	    	
    	// Retrieving Incoming URIs
    	
    	// POJO EndPointForm instance used for holding the incoming URIs
    	EndPointForm incomingEndPointForm = new EndPointForm();
    	incomingEndPointForm.setItemsPerPage(itemsPerPage);
    	
    	// Setting query
    	String incomingQueryStr = "select * where { ?s ?p <" + incomingOutgoingURIs.getUri() + ">}";
    	incomingEndPointForm.setQuery(incomingQueryStr);
    	
    	// Retrieving items based on query an holding them in EndPointForm POJO
    	incomingEndPointForm = retrieveBasedOnQuery(incomingEndPointForm);
    	
    	// Checking saved status
    	if (incomingEndPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
    		incomingUrisResult = incomingEndPointForm.getResult();
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, incomingEndPointForm.getItemsPerPage(), "incoming");
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			incomingEndPointForm.setResult(firstPageQueryResult);
			
			// Set to IncomingOutgoingURIs
			incomingOutgoingURIs.setIncomingEndPointForm(incomingEndPointForm);
    	}

    	return incomingOutgoingURIs;
	
    }
        
    /**
     * Request get for retrieving a number of items that correspond to the passed page
     * and returning them in the form of EndPointDataPage object.
     *
     * @param requestParams 	A map that holds all the request parameters
     * @return 					An EndPointDataPage object that holds the items of the passed page.
     */
    @RequestMapping(value = "/paginator_json", method = RequestMethod.GET, produces={"application/json"})
	public @ResponseBody EndPointDataPage loadDataForPage(@RequestParam Map<String,String> requestParams) {//, Model model) {
    	
    	int page = new Integer(requestParams.get("page")).intValue();
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();
		String action = requestParams.get("action");

    	// The EndPointForm for the page
    	EndPointDataPage endPointDataPage = new EndPointDataPage();
    	endPointDataPage.setPage(page);
    	if(action.equals("query"))
    		endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
    	if(action.equals("outgoing"))
    		endPointDataPage.setTotalItems(outgoingUrisResult.get("results").get("bindings").size());
    	if(action.equals("incoming"))
    		endPointDataPage.setTotalItems(incomingUrisResult.get("results").get("bindings").size());
    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage, action));
    	    	
		return endPointDataPage;
	}
    
    /**
     * Constructs an ObjectNode for one page only, based on the passed page and the whole data
     *
     * @param page	 		The page number
     * @param itemsPerPage 	The number of items per page
     * @return 				the constructed ObjectNode
     */
    private ObjectNode getDataOfPageForCurrentEndPointForm(int page, int itemsPerPage, String action) {
    	    	    	    	
    	//{"head":{"vars":["s","p","o"]},"results":{"bindings":[{"s":{... "p":{...
    	JsonNodeFactory factory = JsonNodeFactory.instance;
    	
    	// vars
		ObjectNode varsObjectNode = new ObjectNode(factory);
		if(action.equals("query"))
			varsObjectNode.set("vars", currQueryResult.get("head").get("vars"));
		else if(action.equals("outgoing"))
			varsObjectNode.set("vars", outgoingUrisResult.get("head").get("vars"));
		else if(action.equals("incoming"))
			varsObjectNode.set("vars", incomingUrisResult.get("head").get("vars"));
		
		// bindings
		ArrayNode bindingsArrayNode = new ArrayNode(factory);
		
    	for (int i=0; i<itemsPerPage; i++) {
    		// Case query results
    		if(action.equals("query")) {
	    		if(currQueryResult.get("results").get("bindings").hasNonNull(i+(page-1)*itemsPerPage))
	    			bindingsArrayNode.add(currQueryResult.get("results").get("bindings").get(i+(page-1)*itemsPerPage));
    		}
    		// Case outgoing results
    		else if(action.equals("outgoing")) {
    			if(outgoingUrisResult.get("results").get("bindings").hasNonNull(i+(page-1)*itemsPerPage))
	    			bindingsArrayNode.add(outgoingUrisResult.get("results").get("bindings").get(i+(page-1)*itemsPerPage));
    		}
    		// Case incoming results
    		else if(action.equals("incoming")) {
    			if(incomingUrisResult.get("results").get("bindings").hasNonNull(i+(page-1)*itemsPerPage))
	    			bindingsArrayNode.add(incomingUrisResult.get("results").get("bindings").get(i+(page-1)*itemsPerPage));
    		}
    	}
    	  	
    	// Final ResultObject
    	ObjectNode resultObjectNode = new ObjectNode(factory);
    	resultObjectNode.set("head", varsObjectNode);
    	resultObjectNode.set("results", bindingsArrayNode);
    	
    	return resultObjectNode;
    	
    }
    
    /**
     * Request get for retrieving the rdf.base attribute retrieved from the config.properties file
     *
     * @return 	A String holding the rdf.base
     */
    @RequestMapping(value = "/config_properties", method = RequestMethod.GET, produces={"application/json"})
	public @ResponseBody ConfigProperty retrieveRdfBase() {
    	ConfigProperty configProperty = new ConfigProperty();
    	configProperty.setRdfBase(rdfBase);
    	configProperty.setRdfAfterbase(rdfAfterbase);
		return configProperty;
	}
        
}