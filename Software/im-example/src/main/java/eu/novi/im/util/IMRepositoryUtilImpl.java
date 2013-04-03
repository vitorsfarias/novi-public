package eu.novi.im.util;

import info.aduna.io.FileUtil;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.result.Result;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.*;
import eu.novi.im.core.impl.GroupImpl;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.core.impl.LocationImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.ServiceImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.ManagedEntity;
import eu.novi.im.policy.ManagedEntityMethod;
import eu.novi.im.policy.ManagedEntityProperty;
import eu.novi.im.policy.impl.ManagedEntityImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;

public class IMRepositoryUtilImpl implements IMRepositoryUtil {
	private static final transient Logger log = LoggerFactory
			.getLogger(IMRepositoryUtilImpl.class);

	ObjectRepositoryFactory objectRepositoryFactory;

	static ObjectRepository objectRepository = null;
	ObjectConnection objectConnection;
	String owlFile;

	public IMRepositoryUtilImpl() {

		try {
			Repository repository = new SailRepository(new MemoryStore());
			repository.initialize();

			objectRepositoryFactory = new ObjectRepositoryFactory();

			objectRepository = objectRepositoryFactory.createRepository(
					IMUtil.getRepositoryConfig(), repository);
			objectConnection = objectRepository.getConnection();

			log.info("Successfully initialize repository");

		} catch (RepositoryConfigException e) {

			e.printStackTrace();
		} catch (RepositoryException e) {
			log.error("This is not good, failed to initialize repository");
		}
	}

	
	
	public void destroyMethod() {
		try {
			// Useless variable, but if this file util from aduna is not here,
			// it seems that shutdown process failed (or maybe I am just
			// confused)
			FileUtil justToBeSureItSHere = new FileUtil();
			if (objectConnection != null)
				if (objectConnection.isOpen())
					objectConnection.close();
			if (objectRepository != null)
				objectRepository.shutDown();

		} catch (RepositoryException e) {
			log.error("Failed to shutdown repository");
		}
		log.info("Successfully shut down repository");
	}

	public ObjectConnection getObjectConnection() {
		return objectConnection;
	}

	/////////////////////////////////////////////////////////////////////////////
	//CODED - DECODED LIBRARY
	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String exportIMObjectToString(Object toExport) {
		
		synchronized(objectRepository) {
		
			//Check if object is not one of the root concepts in IM, then no transformation 
			if(!(toExport instanceof ResourceImpl || toExport instanceof GroupImpl || 
				 toExport instanceof LocationImpl || toExport instanceof LifetimeImpl ||
				 toExport instanceof ServiceImpl || toExport instanceof NOVIUserImpl) )
			{
				log.warn("exportIMObjectToString: the given object {}, is not a NOVI IM object",
						toExport);
				return null;
			}
			
			ObjectConnection connection =null;
			StringWriter stringWriter = new StringWriter();
			RDFXMLWriter rdfwriter = new RDFXMLWriter(stringWriter);
			String answer = null;
			try {
				connection = objectRepository.getConnection();
				connection.clear();
				connection.addObject(toExport);
				/////
				/*RepositoryResult<Statement> stats = connection.getStatements(null, null, null);
				for (Statement st: stats.asList())
				{
					log.debug(st.getSubject() + ", " + st.getPredicate() + ", " + st.getObject());
				}*/
				/////
				connection.export(rdfwriter);
	
				answer = stringWriter.getBuffer().toString();
				
			} catch(RepositoryException e) {
				log.warn("Problem in exportIMObjectToString :" + e.getMessage());
				//e.printStackTrace();
			} catch (RDFHandlerException e) {
				log.warn("Problem in exportIMObjectToString :" + e.getMessage());
				//e.printStackTrace();
			} finally {
				 
				try {
					connection.close();
				} catch (RepositoryException e) {
					log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
					//e.printStackTrace();
				}
			}
			
			
			return answer;
		
		}
	}
	
	@Override
	public String exportIMObjectToStringWithFilter(Object toExport, String ... urisToExclude) {
		
		synchronized(objectRepository) {
		
			ObjectConnection connection =null;
			StringWriter stringWriter = new StringWriter();
			RDFXMLWriter rdfwriter = new RDFXMLWriter(stringWriter);
			String answer = null;
				try {
				connection = objectRepository.getConnection();
				connection.clear();
				connection.addObject(toExport);
			
				for(String uriToRemove : urisToExclude){
					//Statement allStatementWithURIAsType  = new StatementImpl(null, null, new URIImpl(uriToRemove));
					connection.remove(null, null, new URIImpl(uriToRemove), null);
				}
				
				/////
				connection.export(rdfwriter);
				answer = stringWriter.getBuffer().toString();
				
			} catch(RepositoryException e) {
				log.warn("Problem in exportIMObjectToString :" + e.getMessage());
				//e.printStackTrace();
			} catch (RDFHandlerException e) {
				log.warn("Problem in exportIMObjectToString :" + e.getMessage());
				//e.printStackTrace();
			} finally {
				 
				try {
					connection.close();
				} catch (RepositoryException e) {
					log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
					//e.printStackTrace();
				}
			}
			
			
			return answer;
		}
	}
	@Override
	public <T> Set<T> getIMObjectsFromString(String xmlRdfString, Class<T> type) {
		synchronized(objectRepository) {
			ObjectConnection connection =null;
			if (xmlRdfString == null)
			{
				log.warn("getIMObjectsFromString: the input string is null");
				return null;
			}
			StringReader owlStringReader = new StringReader(xmlRdfString);
			Set<T> result = new HashSet<T>();
			IMCopy copier = new IMCopy();
			//copier.enableLogs();
			try {
				connection = objectRepository.getConnection();
				connection.clear();
				connection.add(owlStringReader,"", RDFFormat.RDFXML);
				Result<T> results = connection.getObjects(type);
				while(results.hasNext()){
					T currentResult = (T) results.next();
					T implCopy = (T) copier.copy(currentResult, -1);
					result.add(implCopy);
				}
				
			} catch(RepositoryException e){
				log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
				e.printStackTrace();
			} catch (RDFParseException e) {
				log.warn(e.getMessage());
				log.warn("Problem in getIMObjectsFromString. The string file is not valid:" + xmlRdfString);
				//e.printStackTrace();
			} catch (IOException e) {
				log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
				e.printStackTrace();
			} finally {
			 
				try {
					connection.close();
				} catch (RepositoryException e) {
					log.warn("Problem in getIMObjectsFromString :" + e.getMessage());
					//e.printStackTrace();
				}
			}
			
			return result;
		}
	}
	

	@Override
	public <T> T getIMObjectFromString(String xmlRdfString, Class<T> type) {
		Set<T> objects = getIMObjectsFromString(xmlRdfString, type);
		if (objects == null || objects.size() == 0)
		{
			return null;
		}
		else
		{
			return objects.iterator().next();
		}
	}
	
	
	@Override
	public <T> T getIMObjectFromString(String xmlRdfString, Class<T> type,
			String uri) {

		Set<T> objects = getIMObjectsFromString(xmlRdfString, type);
		if (objects == null || objects.size() == 0)
		{
			return null;
		}
		else
		{
			for (T obj : objects)
			{
				if (obj.toString().equals(uri))
				{
					return obj;
				}
			}
			return null;
		}
	}
	
	
	
	
	////////////////////////////////////////////
	//END OF CODED - DECODED LIBRARY
	////////////////////////////////////////////
	
	/**
	 * 
	 */
	public Topology getTopologyFromFile(String owlFile) {
		StringReader owlStringReader = new StringReader(owlFile);

		Result<Topology> topologies;
		Topology result = null;
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader, "", RDFFormat.RDFXML);
			topologies = getObjectConnection().getObjects(Topology.class);
			while (topologies.hasNext()) {
				Object current = topologies.next();
				// Since these objects are obtained using getObjects(Topology.class) this object is for sure a topology.
				result = (Topology) current;
					// Without break here means we are getting the last topology
				
			}
		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		} catch (RDFParseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		IMCopy copier = new IMCopy();
		TopologyImpl topologyImpl = (TopologyImpl) copier.copy(result, -1);
		
		return topologyImpl;
	}

	public Reservation getReservationFromFile(String owlFile) {
		StringReader owlStringReader = new StringReader(owlFile);

		Result<Reservation> reservations;
		Reservation result = null;
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader, "", RDFFormat.RDFXML);
			reservations = getObjectConnection().getObjects(Reservation.class);
			while (reservations.hasNext()) {
				Object current = reservations.next();
				if (current instanceof Reservation) {
					result = (Reservation) current;
					// Without break here means we are getting the last topology
				}
			}
		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		} catch (RDFParseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		IMCopy copier = new IMCopy();
		ReservationImpl reservationImpl = (ReservationImpl) copier.copy(result, -1);
		
		return reservationImpl;
	}

	public Resource getResourceFromFile(String owlFile) {
		StringReader owlStringReader = new StringReader(owlFile);

		Result<Resource> resources;
		Resource result = null;
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader, "", RDFFormat.RDFXML);
			resources = getObjectConnection().getObjects(Resource.class);
			while (resources.hasNext()) {
				Object current = resources.next();
				if (current instanceof Resource) {
					result = (Resource) current;
					// Without break here means we are getting the last topology
				}
			}
		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		} catch (RDFParseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return result;
	}

	public Resource getKnownResourceFromFile(String owlFile, String name) {
		StringReader owlStringReader = new StringReader(owlFile);

		Result<Resource> resources;
		Resource result = null;
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader, "", RDFFormat.RDFXML);
			resources = getObjectConnection().getObjects(Resource.class);
			while (resources.hasNext()) {
				Object current = resources.next();
				if ((current instanceof Resource)
						&& current.toString().equals(name)) {
					result = (Resource) current;
					break;
				}
			}
		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		} catch (RDFParseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		IMCopy copier = new IMCopy();
		ResourceImpl resImpl = (ResourceImpl) copier.copy(result, -1);
		
		return resImpl;
	}

	public ObjectConnection getConnection() {
		return objectConnection;
	}

	@Override
	public Topology createTopology(String topologyName) {
		ObjectFactory factory = getConnection().getObjectFactory();

		Topology myTopology = factory.createObject("http://fp7-novi.eu/im.owl#"
				+ topologyName, Topology.class);

		return myTopology;
	}

	public void dumpRepository() throws RepositoryException {
		RepositoryResult<Statement> statements = getObjectConnection()
				.getStatements(null, null, null, null);
		for (final Statement s : statements.asList()) {
			System.out.println(s.getSubject().stringValue() + " "
					+ s.getPredicate().stringValue() + " "
					+ s.getObject().stringValue());
		}
	}

	@Override
	public <T> T createObject(String id, Class<T> type) {
		ObjectFactory factory = getConnection().getObjectFactory();
		return factory.createObject("http://fp7-novi.eu/im.owl#" + id, type);
	}

	/**
	 * This method will take an object created using Alibaba object factory and
	 * instantiate an implemented object.
	 * 
	 * Still work in progress, not yet fully tested.
	 * 
	 * @param sourceAlibaba
	 *            is the object created from Alibaba object factory
	 * @param targetImpl
	 *            is the object implementation target.
	 */
	@Override
	public void cloneInterfaceToImplementation(
			Object sourceAlibaba,
			Object targetImpl) {
			
			Class<?> abClass = sourceAlibaba.getClass();
			Class<?> imClass = targetImpl.getClass();

			System.out.println(abClass.getName()+ "  "+imClass.getName());
			
			// Methods from the alibaba interfaces
			Method[] abMethods = abClass.getMethods();
			
			// Methods from the implementations
			Method[] imMethods = imClass.getMethods();

			// For convinience, string to method maps
			HashMap<String, Method> abMethodsMap = new HashMap<String, Method>();
			HashMap<String, Method> imMethodsMap = new HashMap<String, Method>();
			
			// Populating method maps
			for(Method m : abMethods)	abMethodsMap.put(m.getName(), m);
			for(Method m : imMethods)	imMethodsMap.put(m.getName(), m);
							
			/** For each setters from the target implementation, 
			 *  get the corresponding property values from Alibaba interface 
			 *  Set the value accordingly, if necessary perform recursion 
			 */
			for(Method targetSetterMethod : imMethods){
				String targetMethodName = targetSetterMethod.getName();
				if(targetMethodName.startsWith("set")){
					Method sourceGetterMethod = abMethodsMap.get(targetMethodName.replace("set","get"));
					// This is where the magic happens
					try {
						targetSetterMethod.invoke(targetImpl, sourceGetterMethod.invoke(sourceAlibaba, null));
					} catch(Exception e){
						
					}
				}
			}
	
			//for(String r : notImplementedSet) System.out.print(r+ " ");
	}

	/**
	 * Getting URI from Alibaba object interface by calling toString method
	 * 
	 * @return
	 */
	private String getURIFromAlibabaObject(Object sourceAlibaba) {
		Method toStringMethod =null;
		String result = "";
		try {
			toStringMethod = sourceAlibaba.getClass().getMethod("toString",null);
			result = (String) toStringMethod.invoke(sourceAlibaba, null);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return result;
	}
	
	public Set<Group> getGroupImplFromFile(String owlFile) {
		StringReader owlStringReader = new StringReader(owlFile);
		
		Result<Group> groups;
		Set<Group> result = new HashSet<Group>();
		
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader,"", RDFFormat.RDFXML);
			groups = getObjectConnection().getObjects(Group.class);
			IMCopy copier = new IMCopy();
			while(groups.hasNext()){
				Object current = groups.next();
				// This is ugly but just group is not enough, start from lower subclasses, topology, Platform or reservation first.
				if(current instanceof Topology) {
					Topology topology = (Topology) copier.copy(current, -1);
					result.add(topology);
				} else
				if(current instanceof Platform) {
					Platform platform = (Platform) copier.copy(current, -1);
					result.add(platform);

				} else
				if(current instanceof Reservation) {
					Reservation reservation = (Reservation) copier.copy(current, -1);
					result.add(reservation);
				} else
				if(current instanceof Group) {
					Group group = (Group) copier.copy(current, -1);
					result.add(group);
				}
			}
		} catch (RepositoryException e) {
			
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			
			e.printStackTrace();
		} catch (RDFParseException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		 
		return result;
	}
	public String getId(String string) {
		String[] components = string.toString().split("\\#");
		return components[components.length-1];
	}
	
	@Override
	public Set<Group> getGroupsFromFile(String owlFile) {
		StringReader owlStringReader = new StringReader(owlFile);
		
		Result<Group> groups;
		Set<Group> result = new HashSet<Group>();
		
		try {
			getObjectConnection().clear();
			getObjectConnection().add(owlStringReader,"", RDFFormat.RDFXML);
			groups = getObjectConnection().getObjects(Group.class);
			while(groups.hasNext()){
				Object current = groups.next();
				result.add((Group) current);
				
			}
		} catch (RepositoryException e) {
			
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			
			e.printStackTrace();
		} catch (RDFParseException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		 
		return result;
	}




	@Override
	public String exportGroupImplSetToString(Set<Group> groupsToExport) {
		ObjectConnection connection =null;
		StringWriter stringWriter = new StringWriter();
		RDFXMLWriter rdfwriter = new RDFXMLWriter(stringWriter);
		try {
			connection = objectRepository.getConnection();
			connection.clear();
			for(Group toExport : groupsToExport){
				connection.addObject(toExport);
			}
			connection.export(rdfwriter);
		} catch(RepositoryException e){
			
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return stringWriter.getBuffer().toString();
	}


}
