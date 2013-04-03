package eu.novi.im.ris.test;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.result.Result;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

import eu.novi.im.core.*;

import org.openrdf.annotations.*;

public class AlibabaRetrieveCreateTest
{
	ObjectRepositoryFactory objectRepositoryFactory;

	public ObjectRepositoryFactory getObjectRepositoryFactory()
	{
		return objectRepositoryFactory;
	}

	public void setObjectRepositoryFactory(
			ObjectRepositoryFactory objectRepositoryFactory)
	{
		this.objectRepositoryFactory = objectRepositoryFactory;
	}

	public ObjectRepository getObjectRepository()
	{
		return objectRepository;
	}

	public void setObjectRepository(ObjectRepository objectRepository)
	{
		this.objectRepository = objectRepository;
	}

	public Repository getRdfRepository()
	{
		return rdfRepository;
	}

	public void setRdfRepository(Repository rdfRepository)
	{
		this.rdfRepository = rdfRepository;
	}

	public ObjectConnection getConnection()
	{
		return connection;
	}

	public void setConnection(ObjectConnection connection)
	{
		this.connection = connection;
	}

	ObjectRepository objectRepository;
	Repository	   rdfRepository;
	ObjectConnection connection;

	@Before
	public synchronized void setUp()
	{
		/*
		 * Setup and initialization code, this will be hidden within RIS
		 * component once it is ready. This part of code basically create a
		 * temporary local repository, under directory testRepository that we
		 * will use.
		 */
		//NativeStore nativeStore = new NativeStore(new File(REPOSITORY_PATH));
		MemoryStore memoryStore = new MemoryStore();
		rdfRepository = new SailRepository(memoryStore);
		System.out.println("Setting up");
		try {
			getRdfRepository().initialize();
			setObjectRepositoryFactory(new ObjectRepositoryFactory());
			setObjectRepository(objectRepositoryFactory
					.createRepository(rdfRepository));
			setConnection(objectRepository.getConnection());
			loadSampleOWLFile();

		} catch (RepositoryConfigException e) {

			e.printStackTrace();
		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (RDFParseException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/*
	 * Loading sample owl file created from editor. For this test we will use
	 * objects created in simple bound request and tries to list them.
	 */
	public void loadSampleOWLFile() throws RDFParseException,
			RepositoryException, IOException
	{

		File sampleOWLFile = new File(AlibabaRetrieveCreateTest.class.getResource("/unboundRequestSimple.owl").getPath());
		String baseURI = "";
		getConnection().add(sampleOWLFile, baseURI, RDFFormat.RDFXML);

	}

	@After
	public synchronized void tearDown() throws Exception
	{
		getConnection().close();
		getRdfRepository().shutDown();
		System.out.println("Tear down ");
	}

	/**
	 * Thist test shows how to get all available Nodes, all available
	 * Interfaces, and all available links stored in temporary repository.
	 * 
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */
	//@Test
	public void testListingExistingConcepts() throws RepositoryException,
			QueryEvaluationException
	{
		Result<Node> nodeResIter = getConnection().getObjects(Node.class);
		System.out.println("Test retrieving code, listing all available types");
		System.out.println("Listing nodes");
		int ctrNode = 0, ctrInterface = 0, ctrLink = 0;
		while (nodeResIter.hasNext()) {
			Node currentNode = nodeResIter.next();
			System.out.println("  Current Node : " + currentNode);
			// Rather inconvinient, the generated code assumes a set of possible
			// values
			String hwTypes = currentNode.getHardwareType();
			System.out.println("  Check hardwareType :"
					+ hwTypes);
			ctrNode++;
		}

		Result<Interface> interfaceResIter = getConnection().getObjects(
				Interface.class);
		System.out.println("Listing interfaces");
		while (interfaceResIter.hasNext()) {
			System.out.println("  Current Interface : "
					+ interfaceResIter.next());
			ctrInterface++;
		}

		Result<Link> linkResIter = getConnection().getObjects(Link.class);
		System.out.println("Listing links");
		while (linkResIter.hasNext()) {
			System.out.println("  Current Link : " + linkResIter.next());
			ctrLink++;
		}
		// A Lame test, this is not really a unit test just a sample code
		// This is based on boundSimpleRequest file, if it is changed, this
		// assertion no longer holds
		assert ((ctrNode == 4) && (ctrInterface == 4) && (ctrLink == 1));
	}

	/**
	* In this test we will try to show how to do sparql query and get objects
	* We are getting all the nodes with hardwareType pc 
	*/
	@Test
	public void testSPARQLQueryObjectHWType() throws RepositoryException, 
		QueryEvaluationException, MalformedQueryException
	{
		
		String queryString = 
			"PREFIX im:<http://fp7-novi.eu/im.owl#>\n "+
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"SELECT ?node where { ?node rdf:type im:VirtualNode . \n" +
				"	      ?node im:hardwareType ?hwtype .\n"+
				"	      FILTER regex(str(?hwtype), \"pc\") }\n ";

		ObjectQuery query = getConnection().prepareObjectQuery(queryString);

		Result<Node> nodes =  query.evaluate(Node.class);
		System.out.println("\nExecuting query " + queryString+"\nAll nodes with hardware type pc :\n");
		while(nodes.hasNext()){
			Node current = nodes.next();
			System.out.println("   " + current+" with hardwareType " + current.getHardwareType());
		}
			
	}


	/**
	* In this test we will try to show how to do sparql query and get objects
	* We are getting all the nodes with cpu speed 2 
	*/
	@Test
	public void testSPARQLQueryObjectCPU() throws RepositoryException, 
		QueryEvaluationException, MalformedQueryException
	{
		String queryString = 
			"PREFIX im:<http://fp7-novi.eu/im.owl#>\n "+
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"+
			"SELECT ?node where { ?node rdf:type im:VirtualNode . \n" +
				"	      ?node im:hasComponent ?comp . \n"+
				"	      ?com  rdf:type im:CPU . \n" +
				" 	      ?com  im:hasCPUSpeed ?cpuSpeed . \n"+
				"	      FILTER regex(str(?cpuSpeed), \"2\") } \n";

		ObjectQuery query = getConnection().prepareObjectQuery(queryString);
		Result<Node> nodes =  query.evaluate(Node.class);
		System.out.println("\nExecuting Query "+ queryString +"\nAll nodes with cpuSpeed 2 : \n");
		while(nodes.hasNext()){
			Node current = nodes.next();
			System.out.println("    " + current + "    " + current.getHasComponent());
		}
			
	}
	@Sparql("prefix im:<http://fp7-novi.eu/im.owl#>\n"+
		"SELECT ?Node where {?Node im:hardwareType $hwtype } ")
	public Result<Node> findNodeByHardwareType(@Bind("hwtype") String hwtype){
		return null;
	}
	/**
	 * This test shows how to create a topology class and set its property, and
	 * added them to the repository.
	 * 
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */
	//@Test
	public void testCreateTopology() throws RepositoryException,
			QueryEvaluationException
	{

		/**
		 * Here is where we use Alibaba ObjectFactory to create instance of
		 * object based on available interfaces.
		 */
		ObjectFactory factory = getConnection().getObjectFactory();

		// We instantiate topology and nodes based on generated
		// classes/interfaces
		Topology myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#Topology", Topology.class);

		System.out.println("Creating one additional object called myTopology ");
		ValueFactory vf = getConnection().getValueFactory();

		URI myTopID = vf.createURI("http://fp7-novi.eu/im.owl#myTopology");
		getConnection().addObject(myTopID, myTopology);

		// Another example of creating a Node, and setting their Hardware Type
		Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node",
				Node.class);

		myNode.setHardwareType("linux");

		URI myNodeID = vf.createURI("http://fp7-novi.eu/im.owl#myNode");
		getConnection().addObject(myNodeID, myNode);

		Result<Topology> topResIter = getConnection()
				.getObjects(Topology.class);
		System.out.println("Listing topologies");
		while (topResIter.hasNext()) {
			System.out.println("  Current Topology : " + topResIter.next());
		}

		int nodeCtr = 0;
		Result<Node> nodeResIter = getConnection().getObjects(Node.class);
		System.out
				.println("Listing nodes after creation and addition of new node");
		while (nodeResIter.hasNext()) {
			Node currentNode = nodeResIter.next();
			System.out.println("  Current Node : " + currentNode);

			System.out.println("  Check hardwareType :"
					+ currentNode.getHardwareType());
			nodeCtr++;
		}
		// Another lame assertion, once boundSimpleRequest are change or the
		// codes above are changed this needs to change also.
		assert (nodeCtr == 5);
	}

	/**
	 * Printing out all triples stored in the repository.
	 * 
	 * @throws RepositoryException
	 */
	public void dumpRepository() throws RepositoryException
	{
		RepositoryResult<Statement> statements = getConnection().getStatements(
				null, null, null, null);
		for (final Statement s : statements.asList()) {
			System.out.println(s.getSubject().stringValue() + " "
					+ s.getPredicate().stringValue() + " "
					+ s.getObject().stringValue());
		}
	}

	public static boolean deleteDir(File dir)
	{
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
}
