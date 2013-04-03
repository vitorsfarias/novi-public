package eu.novi.im.util;


import java.util.Set;

import eu.novi.im.core.Group;
import eu.novi.im.core.Topology;


public interface IMRepositoryUtil {
	public Topology getTopologyFromFile(String owlFile);

	public Set<Group> getGroupsFromFile(String owlFile);
	
	public Topology createTopology(String string);
	
	public <T> T createObject(String id, Class <T> type);
	
	
	public void cloneInterfaceToImplementation(Object interfaceObjectFromRepository, Object emptyImplementationTarget);
	
	public Set<Group> getGroupImplFromFile(String owlFile);
	
	/**
	 * This is the counter part of the above getGroupImpl from file, 
	 * it will export and generate rdf/xml string.
	 * @param groupsToExport
	 * @return
	 */
	public String exportGroupImplSetToString(Set<Group> groupsToExport);

	/**
	 * This is exporting existing IMObject to string
	 * @param toExport this is the object to be exported, should be an implemented object/instance. e.g TopologyImpl or GroupImpl
	 * @return this will return the xml/rdf string implementation, or null if an error occur
	 */
	public String exportIMObjectToString(Object toExport);

	/**
	 * This is exporting existing IMObject to string
	 * @param toExport this is the object to be exported, should be an implemented object/instance. e.g TopologyImpl or GroupImpl
	 * @return this will return the xml/rdf string implementation, or null if an error occur
	 * @param uriTypesToExclude set of strings which will be filtered from output, whenever they are part of object in a statement.
	 * Some unnecessary types are filtered from the statements
	 */
	public String exportIMObjectToStringWithFilter(Object toExport, String... uriTypesToExclude);

	
	/**
	 * Functions to get IM Objects from rdf/xml string representation
	 * 
	 * @param xmlRdfString the rdf/xml string
	 * @param type this should be the IM interfaces e.g : Topology.class not the TopologyImpl.class
	 * @return a set of objects of type T that is contained in the string, which is already transformed 
	 * to an implemented instance so this set is of type Topology.class but within each instance it 
	 * is already TopologyImpl (using IMCopy).
	 * If none object T is contained in the string (or an error happen) then it will return an empty Set.
	 * 
	 */
	public <T> Set <T> getIMObjectsFromString(String xmlRdfString,  Class <T> type);
	
	/**
	 * Functions to get an IM Object from rdf/xml string representation.
	 * 
	 * 
	 * @param xmlRdfString the rdf/xml string
	 * @param type this should be the IM interfaces e.g : Topology.class not the TopologyImpl.class
	 * @return an objects of type T that is contained in the string. The answer is already transformed to an implemented instance
	 * 		   so this is type of Topology.class but within each instance it is already TopologyImpl (using IMCopy).
	 * If not object T is contained in the string then it will return null.
	 * if in the string is contained more that one object of type T 
	 * then it returns only one of that
	 */
	public <T> T getIMObjectFromString(String xmlRdfString,  Class <T> type);
	
	/**
	 * Functions to get a specific IM Object from rdf/xml string representation.
	 * 
	 * 
	 * @param xmlRdfString the rdf/xml string
	 * @param type The type of the object.
	 * This should be the IM interfaces e.g : Topology.class not the TopologyImpl.class
	 * @param uri the URI of the requested object
	 * @return an objects of type T that is contained in the string and have the given URI.
	 *  The answer is already transformed to an implemented instance so this is type of
	 *  Topology.class but within each instance it is already TopologyImpl (using IMCopy).
	 * If the specific object is not contained in the string then it will return null.
	 */
	public <T> T getIMObjectFromString(String xmlRdfString,  Class <T> type, String uri);
}
