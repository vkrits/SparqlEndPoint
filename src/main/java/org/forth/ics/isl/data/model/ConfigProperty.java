package org.forth.ics.isl.data.model;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS the pagination and Spring
 * 
 * @author Vangelis Kritsotakis
 */
public class ConfigProperty {
	private String rdfBase;
	private String rdfAfterbase;
	
	public String getRdfBase() {
		return rdfBase;
	}
	public void setRdfBase(String rdfBase) {
		this.rdfBase = rdfBase;
	}
	
	public String getRdfAfterbase() {
		return rdfAfterbase;
	}
	public void setRdfAfterbase(String rdfAfterbase) {
		this.rdfAfterbase = rdfAfterbase;
	}
	
}
