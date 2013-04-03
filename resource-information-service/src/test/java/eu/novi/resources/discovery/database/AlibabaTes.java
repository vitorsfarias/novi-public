package eu.novi.resources.discovery.database;


import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Storage;
import eu.novi.im.core.Topology;
import eu.novi.im.util.IMUtil;

/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * for local testing about alibaba
 * @author chariklis
 *
 */
public class AlibabaTes {
	static LocalDbCalls calls;
	private static final transient Logger log = 
			LoggerFactory.getLogger(AlibabaTes.class);

	Topology myTopology = null;
	private ObjectConnection connect;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//LocalDbCalls.showAllContentOfDB();
		ConnectionClass.stopStorageService();
	}
	
	@Before
	public void setUp() throws Exception {
		connect = ConnectionClass.getNewConnection();
	
	}

	@After
	public void tearDown() throws Exception {
		ConnectionClass.closeAConnection(connect);
	}

	//@Test
	public void storing() throws RepositoryException {
		Node nod = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#nod", Node.class);
		log.debug("just use object facotory to create the node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);

		connect.addObject("http://fp7-novi.eu/im.owl#nod", nod);
		//connect.commit();
		log.debug("call the addObject and add node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);

		connect.addObject("http://fp7-novi.eu/im.owl#nod", nod);
		log.debug("call again the add object for node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);


		Node node2 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#nod", Node.class);
		connect.addObject("http://fp7-novi.eu/im.owl#nod", node2);
		//connect.commit();

		log.debug("call again the create object create node2 with name nod and call addObject and add it again");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);
		LocalDbCalls.execPrintStatement(null, null, NoviUris.createNoviURI("nod"), true);
		System.out.println();


		nod.setHardwareType("x86");
		log.debug("set hardware type x86 for node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);


		Node node3 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#node3a", Node.class);
		connect.addObject("http://fp7-novi.eu/im.owl#node3", node3);
		log.debug("use create object to create node3a and store using addObject this" +
				" object with uri node3");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node3a"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node3"), null, null, true);
		System.out.println();

		CPU cpu1 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#cpu1", CPU.class);
		Set<NodeComponent> component = new HashSet<NodeComponent>();
		component.add(cpu1);
		nod.setHasComponent(component);
		log.debug("create cpu1 with objectFactory and set it to node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("cpu1"), null, null, true);
		connect.addObject("http://fp7-novi.eu/im.owl#cpu1", cpu1);
		log.debug("add cpu1 using addObject");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("cpu1"), null, null, true);

		Set<NodeComponent> retComp = new HashSet<NodeComponent>();
		retComp = nod.getHasComponent();
		log.debug("get back the cpu1 from nod : " + retComp.toString());
		//CPU cputest = (CPU) IMUtil.getOneValueFromSet(retComp);
		log.info("I get back the cpu1 component to a CPU variable. " +
				"IF I OMIT THE addObject(cpu1) THEN I WILL HAVE ClassCastException");

		connect.addDesignation(nod, "http://fp7-novi.eu/im.owl#nod");
		log.debug("call add designation for node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);
		System.out.println();

		Node node10 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#node10", Node.class);
		Node node11 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#node11", Node.class);
		log.debug("create using create object node10 and node11");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node10"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node11"), null, null, true);
		node10.setConnectedTo(IMUtil.createSetWithOneValue(node11));
		log.debug("I set node10 conected to node11");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node10"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node11"), null, null, true);
		log.info("IF I TRY TO GET THE CONNECTED TO node10 NODE I WILL HAVE CLASS CAST EXCEPTION" +
				" BECAUSE I DON'T HAVE IN THE DB THAT node11 IS NODE");

		connect.addObject("http://fp7-novi.eu/im.owl#node10", node10);
		connect.addObject("http://fp7-novi.eu/im.owl#node11", node11);
		log.debug("add the node10 and node11");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node10"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node11"), null, null, true);
		//////////
		Node node12 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#node12", Node.class);
		Node node13 = ConnectionClass.getObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#node13", Node.class);

		connect.addObject("http://fp7-novi.eu/im.owl#node12", node12);
		connect.addObject("http://fp7-novi.eu/im.owl#node13", node13);
		log.debug("create and add the node12 and node13");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node12"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node13"), null, null, true);

		node13.setConnectedTo(IMUtil.createSetWithOneValue(node12));
		log.debug("I set node13 conected to node12");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node12"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("node13"), null, null, true);




		System.out.println("\n//////MEMORY REPOSITORY///////////////");

		Topology topology1 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#topology1", Topology.class);
		topology1.setContains(IMUtil.createSetWithOneValue(nod));
		log.debug("create in memory repository a topology1 and add node nod");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("topology1"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);
		log.debug("now let see in memory repository");
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("nod"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("topology1"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("cpu1"), null, null);
		log.info("We see that it store all the triples for the object which are not before" +
				"in the mem rep, and are below topology1 put it doesnt store the triple about" +
				" topology1, type, Topology");

		Memory memory1 = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#memory1", Memory.class);
		nod.setHasComponent(IMUtil.createSetWithOneValue(memory1));
		log.debug("create memory1 in memory repository (using create object) " +
				"and setComponent(memory1) for node nod (that is in file repository)");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("nod"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("memory1"), null, null, true);
		log.debug("Give attention now that it is stored automatically " +
				"(in file repository) that memory1 is type Memory");





	}


	//@Test
	public void testHowObjectWorks() throws RepositoryException {
		System.out.println("/n\\\\\\\\\\\\TEST HOW ALIBABA CREATE/USE OBJECTS //////////");
		ConnectionClass.getConnection2MemoryRepos().clearNamespaces();
		//LocalDbCalls.showAllContentOfMemoryRepos();
		testTopology();
		ConnectionClass.getConnection2MemoryRepos().clear();
		log.info("I clear the db");
		LocalDbCalls.showAllContentOfMemoryRepos();
		Set<Resource> resources = myTopology.getContains();
		Node nod = (Node) IMUtil.getOneValueFromSet(resources);
		log.info(nod.toString());
		Set<NodeComponent> comp = nod.getHasComponent();
		log.info("let see if there is node components...");
		log.info(comp.toString());



	}

	//@Test
	public void testTopology() throws RepositoryException {

		System.out.println("\n\n////////////////TEST TOPOLOGY/////////////////////////");
		// Another example of creating a Node, and setting their Hardware Type and components
		ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
		myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#TopologyTesttt", Topology.class);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);

		Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node20",
				Node.class);

		//set hardware type
		myNode.setHardwareType("i386");

		//////////add my node to topology ///////////

		myTopology.setContains(IMUtil.createSetWithOneValue(myNode));
		log.debug("after set the node to topology");
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("Node20"), null, null);

		/*//////////////////////////////////////////////////////////////////////
				YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
				NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
				RUNTIME
		 *///////////////////////////////////////////////////////////////////////

		//////add node components to the node /////////
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		////////CPU//////////////////
		CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#myCpu", CPU.class);
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(8));
		log.debug("after create cpu");
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);
		log.debug("now add cpu to a node component");
		nodeComponents.add(myCPU);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);


		/////////Memory Component///////////
		//Memory myMemory = factory.createObject("http://fp7-novi.eu/im.owl#Memory", 
		//Memory.class);
		//myMemory.setHasMemorySize(createSetWithOneValue(100f));
		//myMemory.setHasAvailableMemorySize(createSetWithOneValue(100f));
		//nodeComponents.add(myMemory);

		//////////Storage component///////////
		Storage myStorage = factory.createObject("http://fp7-novi.eu/im.owl#Storage1", 
				Storage.class);
		//myStorage.setHasStorageSize(createSetWithOneValue(1000f));
		myStorage.setHasAvailableStorageSize(1000f);
		nodeComponents.add(myStorage);
		////////////END OF COMPONENTS/////////////
		myNode.setHasComponent(nodeComponents);
		log.debug("after added the cpu to node");
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("Node20"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);
		
		ConnectionClass.getConnection2MemoryRepos().addObject("http://fp7-novi.eu/im.owl#TopologyTesttt", 
				myTopology);
		log.debug("\nafter added the topology to the repository using addObject");
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("Node20"), null, null);
		LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);


	}
	
	
	
	@Test
	public void testRollBack() throws RepositoryException {

		System.out.println("\n\n////////////////Roll back/////////////////////////");
		// Another example of creating a Node, and setting their Hardware Type and components
		connect.setAutoCommit(false);
		ObjectFactory factory = ConnectionClass.getObjectFactory();
		myTopology = factory.createObject(
				"http://fp7-novi.eu/im.owl#TopologyTesttt", Topology.class);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);

		Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node20",
				Node.class);

		//set hardware type
		myNode.setHardwareType("i386");

		//////////add my node to topology ///////////

		myTopology.setContains(IMUtil.createSetWithOneValue(myNode));
		log.debug("after set the node to topology");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("Node20"), null, null, true);

		/*//////////////////////////////////////////////////////////////////////
				YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
				NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
				RUNTIME
		 *///////////////////////////////////////////////////////////////////////

		//////add node components to the node /////////
		Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
		////////CPU//////////////////
		CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#myCpu", CPU.class);
		myCPU.setHasCPUSpeed(2f);
		myCPU.setHasCores(BigInteger.valueOf(8));
		myCPU.setHasAvailableCores(BigInteger.valueOf(8));
		log.debug("after create cpu");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);
		log.debug("now add cpu to a node component");
		nodeComponents.add(myCPU);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);


		////////////END OF COMPONENTS/////////////
		myNode.setHasComponent(nodeComponents);
		log.debug("after added the cpu to node");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("Node20"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);
		
		connect.addObject("http://fp7-novi.eu/im.owl#TopologyTesttt", 
				myTopology);
		log.debug("\nafter added the topology to the repository using addObject");
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("Node20"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);
		log.debug("Rolling back");
		connect.rollback();
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("Node20"), null, null, true);
		LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);


	}
	
	//@Test
	public void testMemoryRepos() throws RepositoryException {

			System.out.println("\n\n////////////////TEST memory repository/////////////////////////");
			// Another example of creating a Node, and setting their Hardware Type and components
			ObjectFactory factory = ConnectionClass.getMemoryObjectFactory();
			myTopology = factory.createObject(
					"http://fp7-novi.eu/im.owl#TopologyTesttt", Topology.class);
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);

			Node myNode = factory.createObject("http://fp7-novi.eu/im.owl#Node20",
					Node.class);

			//set hardware type
			myNode.setHardwareType("i386");

			//////////add my node to topology ///////////

			myTopology.setContains(IMUtil.createSetWithOneValue(myNode));
			log.debug("after set the node to topology");
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("Node20"), null, null);

			/*//////////////////////////////////////////////////////////////////////
					YOU MUST FIRST ADD THE NODE TO THE TOPOLOGY AND AFTER THAT ADD THE 
					NodeComponents TO THE NODE. THE OTHER WAY ROUND GIVES AN ERROR IN 
					RUNTIME
			 *///////////////////////////////////////////////////////////////////////

			//////add node components to the node /////////
			Set<NodeComponent> nodeComponents = new HashSet<NodeComponent>();
			////////CPU//////////////////
			CPU myCPU = factory.createObject("http://fp7-novi.eu/im.owl#myCpu", CPU.class);
			myCPU.setHasCPUSpeed(2f);
			myCPU.setHasCores(BigInteger.valueOf(8));
			myCPU.setHasAvailableCores(BigInteger.valueOf(8));
			log.debug("after create cpu");
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);
			log.debug("now add cpu to a node component");
			nodeComponents.add(myCPU);
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);


		
			////////////END OF COMPONENTS/////////////
			myNode.setHasComponent(nodeComponents);
			log.debug("after added the cpu to node");
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("TopologyTesttt"), null, null);
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("Node20"), null, null);
			LocalDbCalls.execPrintStatementMemoryRepos(NoviUris.createNoviURI("myCpu"), null, null);
			
			connect.addObject("http://fp7-novi.eu/im.owl#TopologyTesttt", 
					myTopology);
			log.debug("\nADD THE TOPOLOGY TO THE NORMAL REPOSITORY using addObject");
			LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("TopologyTesttt"), null, null, true);
			LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("Node20"), null, null, true);
			LocalDbCalls.execPrintStatement(NoviUris.createNoviURI("myCpu"), null, null, true);
			
	}



	/////////////////JUST SOME EXPIRAMENT///////////////////////



	/**
	 * a query test method.
	 * it returns the nodes that have hardware type x86_64
	 */
	private void test2()
	{
		String hwType = "x86_64";

		try {


			try {


				String queryString = "PREFIX im:<http://fp7-novi.eu/im.owl#> " +
						"select * where { ?node im:hardwareType ?hwtype FILTER " +
						"regex(str(?hwtype), \""+hwType+"\") }";
				TupleQuery tupleQuery = ConnectionClass.getConnection2MemoryRepos().prepareTupleQuery(
						QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					// do something with the result

					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfNode = bindingSet.getValue("node");
						Value valueOfHT = bindingSet.getValue("hwtype");
						System.out.println(valueOfNode+", "+valueOfHT);
						String node = valueOfNode.stringValue();
						int index = node.indexOf(" node ");
						System.out.println(node.substring(index+6));

						// do something interesting with the values here...
					}
				} finally {
					result.close();
				}

			} finally {


			}

		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (OpenRDFException e) {
			// handle exception
			e.printStackTrace();
		}


	}
	
	
	/**
	 * a query test method.
	 * it returns the nodes that have hardware type x86_64
	 */
	@Test
	public void testURIinQueries()
	{
		String hwType = "x86_64";

		try {


			try {


				String queryString = LocalDbCalls.PREFIXES +
				"SELECT ?lifetime where { \n" +
				" ?lifetime rdf:type im:Lifetime .\n" +
				"<http://fp7-novi.eu/im.owl#start~+=/#,'.?[-_]:!@#$%&*()end>" + " im:hasLifetime ?lifetime  . }\n" ;
				System.out.println(queryString);
				TupleQuery tupleQuery = ConnectionClass.getConnection2MemoryRepos().prepareTupleQuery(
						QueryLanguage.SPARQL, queryString);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					// do something with the result

					while (result.hasNext()) {
						

						// do something interesting with the values here...
					}
				} finally {
					result.close();
				}

			} finally {


			}

		} catch (RepositoryException e) {

			e.printStackTrace();
		} catch (OpenRDFException e) {
			// handle exception
			e.printStackTrace();
		}


	}


}
