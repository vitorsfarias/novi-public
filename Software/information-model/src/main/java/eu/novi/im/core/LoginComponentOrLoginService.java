package eu.novi.im.core;

import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.rdfs.subPropertyOf;
import eu.novi.im.owl.Thing;
import eu.novi.im.owl.unionOf;

import java.lang.Object;
import java.util.Set;
import org.openrdf.annotations.Iri;

@unionOf({LoginService.class, LoginComponent.class})
public interface LoginComponentOrLoginService extends Thing, ManagedEntity {
	@Iri("http://fp7-novi.eu/im.owl#hasLoginIPv4Address")
	String getHasLoginIPv4Address();
	@Iri("http://fp7-novi.eu/im.owl#hasLoginIPv4Address")
	void setHasLoginIPv4Address(String hasLoginIPv4Address);

	/** values are for example SSHv1, SSHv2, Telnet, etc. */
	@Iri("http://fp7-novi.eu/im.owl#hasLoginProtocol")
	String getHasLoginProtocol();
	/** values are for example SSHv1, SSHv2, Telnet, etc. */
	@Iri("http://fp7-novi.eu/im.owl#hasLoginProtocol")
	void setHasLoginProtocol(String hasLoginProtocol);

	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginPassword")
	String getHasLoginPassword();
	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginPassword")
	void setHasLoginPassword(String hasLoginPassword);

	/** initially will be always port 22 */
	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginPort")
	Integer getHasLoginPort();
	/** initially will be always port 22 */
	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginPort")
	void setHasLoginPort(	@subPropertyOf("http://www.w3.org/2002/07/owl#topDataProperty")
	Integer hasLoginPort);

	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginUsername")
	String getHasLoginUsername();
	@Iri("http://www.co-ode.org/ontologies/ont.owl#hasLoginUsername")
	void setHasLoginUsername(String hasLoginUsername);

}