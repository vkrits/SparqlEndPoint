package org.forth.ics.isl.data.model;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS Spring for the resolver
 * 
 * @author Vangelis Kritsotakis
 */

public class IncomingOutgoingURIs {
	private String uri;
	private EndPointForm outgoingEndPointForm;
	private EndPointForm incomingEndPointForm;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public EndPointForm getOutgoingEndPointForm() {
		return outgoingEndPointForm;
	}
	public void setOutgoingEndPointForm(EndPointForm outgoingEndPointForm) {
		this.outgoingEndPointForm = outgoingEndPointForm;
	}
	public EndPointForm getIncomingEndPointForm() {
		return incomingEndPointForm;
	}
	public void setIncomingEndPointForm(EndPointForm incomingEndPointForm) {
		this.incomingEndPointForm = incomingEndPointForm;
	}
	
}
