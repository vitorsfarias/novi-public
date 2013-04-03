package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.lang.Boolean;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#Policy")
public interface Policy extends ManagedEntity {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasAttached")
	Set<ManagedEntity> getHasAttached();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyObjectProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasAttached")
	void setHasAttached(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyObjectProperties")
Set<? extends ManagedEntity> hasAttached);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#isEnabled")
	Set<Boolean> getIsEnabled();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#isEnabled")
	void setIsEnabled(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#PolicyProperties")
Set<? extends Boolean> isEnabled);

}
