package eu.novi.im.core;

import eu.novi.im.owl.unionOf;

import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({Group.class, Path.class})
public interface GroupOrPath {
	@Iri("http://fp7-novi.eu/im.owl#contains")
	Set<Resource> getContains();
	@Iri("http://fp7-novi.eu/im.owl#contains")
	void setContains(Set<? extends Resource> contains);

}
