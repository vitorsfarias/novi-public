package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.owl.Thing;
import eu.novi.im.owl.unionOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({Interface.class, Node.class})
public interface InterfaceOrNode extends Thing, ManagedEntity, Resource {
	@Iri("http://fp7-novi.eu/im.owl#connectedTo")
	Set<InterfaceOrNode> getConnectedTo();
	@Iri("http://fp7-novi.eu/im.owl#connectedTo")
	void setConnectedTo(Set<? extends InterfaceOrNode> connectedTo);

	/*@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	Set<InterfaceOrNode> getImplementedBy();
	
	*//**the reverse property implements are set automatically
	 * @param implementedBy
	 *//*
	@Iri("http://fp7-novi.eu/im.owl#implementedBy")
	void setImplementedBy(Set<? extends InterfaceOrNode> implementedBy);

	@Iri("http://fp7-novi.eu/im.owl#implements")
	Set<InterfaceOrNode> getImplements();
	@Iri("http://fp7-novi.eu/im.owl#implements")
	void setImplements(Set<? extends InterfaceOrNode> _implements);*/

}
