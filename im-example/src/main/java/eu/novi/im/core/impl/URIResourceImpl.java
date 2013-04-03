package eu.novi.im.core.impl;

public class URIResourceImpl extends org.openrdf.model.impl.URIImpl {

	public URIResourceImpl(String uri) {
		super(uri);
	}
	
	
	public URIResourceImpl()  {
		super();
	}
	
	public void setURIString(String string) {
		super.setURIString(string);
	}
	
	public String getURIString() {
		return super.toString();
	}
	
	String localName;
	public void setLocalName(String localName){
			this.localName = localName;
	}
	
	String namespace;
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
