package eu.novi.im.core;

import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Link")
public interface Link extends NetworkElement, LinkOrPath {
	@Iri("http://fp7-novi.eu/im.owl#provisionedBy")
	Set<LinkOrPath> getProvisionedBy();
	@Iri("http://fp7-novi.eu/im.owl#provisionedBy")
	void setProvisionedBy(Set<? extends LinkOrPath> provisionedBy);

}
