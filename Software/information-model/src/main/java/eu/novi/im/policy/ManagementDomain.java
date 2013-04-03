package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#ManagementDomain")
public interface ManagementDomain extends ManagedEntity {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagementDomainProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#includes")
	Set<ManagedEntity> getIncludes();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagementDomainProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#includes")
	void setIncludes(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#ManagementDomainProperties")
Set<? extends ManagedEntity> includes);

}
