package eu.novi.im.core;

import java.lang.Float;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Memory")
public interface Memory extends NodeComponent {
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableMemorySize")
	Float getHasAvailableMemorySize();
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableMemorySize")
	void setHasAvailableMemorySize(Float hasAvailableMemorySize);

	@Iri("http://fp7-novi.eu/im.owl#hasMemorySize")
	Float getHasMemorySize();
	@Iri("http://fp7-novi.eu/im.owl#hasMemorySize")
	void setHasMemorySize(Float hasMemorySize);

}
