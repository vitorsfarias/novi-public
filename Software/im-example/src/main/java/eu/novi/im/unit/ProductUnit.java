package eu.novi.im.unit;

import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/unit.owl#ProductUnit")
public interface ProductUnit extends DerivedUnit {
	@Iri("http://fp7-novi.eu/unit.owl#productOf")
	Set<Unit> getProductOf();
	@Iri("http://fp7-novi.eu/unit.owl#productOf")
	void setProductOf(Set<? extends Unit> productOf);

}
