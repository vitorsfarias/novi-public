package eu.novi.im.policy;

import eu.novi.im.owl.Thing;
import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntity")
public interface ManagedEntity extends Thing {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasMethods")
	Set<ManagedEntityMethod> getHasMethods();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasMethods")
	void setHasMethods(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
Set<? extends ManagedEntityMethod> hasMethods);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasProperties")
	Set<ManagedEntityProperty> getHasProperties();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasProperties")
	void setHasProperties(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagedEntitiesProperties")
Set<? extends ManagedEntityProperty> hasProperties);

}
