package eu.novi.im.core;

import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Group")
public interface Group extends GroupOrPath, GroupOrResource {
	@Iri("http://fp7-novi.eu/im.owl#autoUpdateOnfailure")
	Boolean getAutoUpdateOnfailure();
	@Iri("http://fp7-novi.eu/im.owl#autoUpdateOnfailure")
	void setAutoUpdateOnfailure(Boolean autoUpdateOnfailure);
}
