package eu.novi.resources.discovery.database.communic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NOT USED
 * construct a monitoring query in an OWL format.
 *@author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ContructMonQuery {
	private static final transient Logger log = 
			LoggerFactory.getLogger(ContructMonQuery.class);
	
	private final static String	PREFIX = 
			"<?xml version=\"1.0\"?> " +
			"<!DOCTYPE rdf:RDF [ \n" +
			"<!ENTITY unit \"http://fp7-novi.eu/unit.owl#\" >\n" +
			"<!ENTITY owl \"http://www.w3.org/2002/07/owl#/\" >\n" +
			"<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n" +
			"<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" +
			"<!ENTITY monitoring_query \"http://fp7-novi.eu/monitoring_query.owl#\" >\n" +
			"<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" +
			"<!ENTITY monitoring_features \"http://fp7-novi.eu/monitoring_features.owl#\" >\n" +
			"<!ENTITY monitoring_parameter \"http://fp7-novi.eu/monitoring_parameter.owl#\" >\n" +
			"]> \n" +
			"<rdf:RDF xmlns=\"http://fp7-novi.eu/monitoringQuery_example.owl#\"\n" +
			"xml:base=\"http://fp7-novi.eu/monitoringQuery_example.owl\"\n" +
			"xmlns:unit=\"http://fp7-novi.eu/unit.owl#\"\n" +
			"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
			"xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
			"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
			"xmlns:monitoring_features=\"http://fp7-novi.eu/monitoring_features.owl#\"\n" +
			"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
			"xmlns:monitoring_query=\"http://fp7-novi.eu/monitoring_query.owl#\"\n" +
			"xmlns:monitoring_parameter=\"http://fp7-novi.eu/monitoring_parameter.owl#\">\n" +
			"<owl:Ontology rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl\">\n" +
			"<owl:imports rdf:resource=\"http://fp7-novi.eu/monitoring_query.owl\"/>\n" +
			"</owl:Ontology>\n";
	
	
	public static String getTestQuery()
	{
		return PREFIX +
				"<owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#PQ_Host_novilab.elte.hu\">\n" +
				"<rdf:type rdf:resource=\"&monitoring_query;QueryParameter\"/>\n" +
				"<monitoring_parameter:paramValue>157.181.175.243</monitoring_parameter:paramValue>\n" +
				"<monitoring_parameter:paramName>SourceAddress</monitoring_parameter:paramName>\n" +
				"<rdfs:comment>this parameter in the future should not be stated explicitely, " +
				"since it is bound to the Resource (to be imported from the core ontology)   </rdfs:comment>\n" +
				"<monitoring_parameter:hasType rdf:resource=\"&monitoring_parameter;String\"/>\n" +
				"<monitoring_parameter:hasDimension rdf:resource=\"&unit;ipaddress\"/>\n" +
				"</owl:NamedIndividual>\n\n" +

    			"<owl:NamedIndividual rdf:about=\"http://fp7-novi.eu/monitoringQuery_example.owl#measureMemoryInformation\">\n" +
    			"<rdf:type rdf:resource=\"&monitoring_query;BundleQuery\"/>\n" +
    			"<monitoring_query:hasParameter rdf:resource=\"http://fp7-novi.eu/monitoringQuery_example.owl#PQ_Host_novilab.elte.hu\"/>\n" +
    			"<monitoring_query:hasFeature rdf:resource=\"&monitoring_features;FreeMemory\"/>\n" +
    			"</owl:NamedIndividual>\n" +
    			"</rdf:RDF>";
	}

}
