package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#MissionConroller")
public interface MissionConroller extends ManagedEntity {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionControllerProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasLoaded")
	Set<MissionPolicy> getHasLoaded();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionControllerProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#hasLoaded")
	void setHasLoaded(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionControllerProperties")
Set<? extends MissionPolicy> hasLoaded);

}
