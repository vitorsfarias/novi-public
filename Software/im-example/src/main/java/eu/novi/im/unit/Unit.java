package eu.novi.im.unit;

import eu.novi.im.rdfs.subPropertyOf;
import java.lang.Double;
import java.lang.String;
import java.util.Set;
import org.openrdf.annotations.Iri;

@Iri("http://fp7-novi.eu/unit.owl#Unit")
public interface Unit extends PrefixOrUnit {
	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
	@Iri("http://fp7-novi.eu/unit.owl#forwardExpression")
	String getForwardExpression();
	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
	@Iri("http://fp7-novi.eu/unit.owl#forwardExpression")
	void setForwardExpression(	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
String forwardExpression);

	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
	@Iri("http://fp7-novi.eu/unit.owl#inverseExpression")
	String getInverseExpression();
	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
	@Iri("http://fp7-novi.eu/unit.owl#inverseExpression")
	void setInverseExpression(	@subPropertyOf("http://fp7-novi.eu/unit.owl#regexp")
String inverseExpression);

	@Iri("http://fp7-novi.eu/unit.owl#possiblePrefix")
	Set<Prefix> getPossiblePrefix();
	@Iri("http://fp7-novi.eu/unit.owl#possiblePrefix")
	void setPossiblePrefix(Set<? extends Prefix> possiblePrefix);

	@Iri("http://fp7-novi.eu/unit.owl#power")
	Double getPower();
	@Iri("http://fp7-novi.eu/unit.owl#power")
	void setPower(Double power);

	@Iri("http://fp7-novi.eu/unit.owl#regexp")
	Set<String> getRegexp();
	@Iri("http://fp7-novi.eu/unit.owl#regexp")
	void setRegexp(Set<? extends String> regexp);

}
