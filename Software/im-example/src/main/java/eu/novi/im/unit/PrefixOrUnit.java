package eu.novi.im.unit;

import eu.novi.im.owl.unionOf;
import java.lang.Double;
import java.lang.String;
import org.openrdf.annotations.Iri;

@unionOf({Unit.class, Prefix.class})
public interface PrefixOrUnit {
	@Iri("http://fp7-novi.eu/unit.owl#scale")
	Double getScale();
	@Iri("http://fp7-novi.eu/unit.owl#scale")
	void setScale(Double scale);

	@Iri("http://fp7-novi.eu/unit.owl#symbol")
	String getSymbol();
	@Iri("http://fp7-novi.eu/unit.owl#symbol")
	void setSymbol(String symbol);

	@Iri("http://fp7-novi.eu/unit.owl#value")
	Double getValue();
	@Iri("http://fp7-novi.eu/unit.owl#value")
	void setValue(Double value);

}
