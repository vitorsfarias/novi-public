package eu.novi.im.policy;

import eu.novi.im.rdfs.subPropertyOf;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterface")
public interface MissionInterface extends ManagedEntity {
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#acceptsEvent")
	Set<PolicyEvent> getAcceptsEvent();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#acceptsEvent")
	void setAcceptsEvent(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
Set<? extends PolicyEvent> acceptsEvent);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#providesEvent")
	Set<PolicyEvent> getProvidesEvent();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#providesEvent")
	void setProvidesEvent(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
Set<? extends PolicyEvent> providesEvent);

	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#raiseEvent")
	Set<PolicyEvent> getRaiseEvent();
	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
	@Iri("http://fp7-novi.eu/NOVIPolicyService.owl#raiseEvent")
	void setRaiseEvent(	@subPropertyOf("http://fp7-novi.eu/NOVIPolicyService.owl#MissionInterfaceProperties")
Set<? extends PolicyEvent> raiseEvent);

}
