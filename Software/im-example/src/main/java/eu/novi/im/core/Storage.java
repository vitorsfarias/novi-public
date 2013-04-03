package eu.novi.im.core;

import java.lang.Float;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/im.owl#Storage")
public interface Storage extends NodeComponent {
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableStorageSize")
	Float getHasAvailableStorageSize();
	@Iri("http://fp7-novi.eu/im.owl#hasAvailableStorageSize")
	void setHasAvailableStorageSize(Float hasAvailableStorageSize);

	@Iri("http://fp7-novi.eu/im.owl#hasStorageSize")
	Float getHasStorageSize();
	@Iri("http://fp7-novi.eu/im.owl#hasStorageSize")
	void setHasStorageSize(Float hasStorageSize);

}
