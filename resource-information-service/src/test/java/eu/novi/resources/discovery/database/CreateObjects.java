package eu.novi.resources.discovery.database;


import java.util.Set;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.CPUImpl;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
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
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class CreateObjects {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(CreateObjects.class);
	private ObjectConnection connect;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
	public void test() throws RepositoryException {
	
		 Node node1 = ConnectionClass.getObjectFactory().createObject(
	        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr",
	        		Node.class);
	        connect.addObject(
	        		"http://fp7-novi.eu/im.owl#planetlab1-novi.lab.netmode.ece.ntua.gr", node1);
	        System.out.println("exclusive is : "+node1.getExclusive());
	        node1.setExclusive(true);
	        System.out.println("exclusive is : "+node1.getExclusive());
	}
	
	@Test
	public void testDuplicateNodes() throws RepositoryException, QueryEvaluationException
	{
		//it does not find the lifetime for the node1  
		//if you make node1 and node2 as node then it find the wrong 
		//lifetime in the second case
		ManipulateDB.clearTripleStore();
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts"));
		LocalDbCalls.showAllContentOfDB();
		Node node = new NodeImpl("myNode1");
		node.setHardwareType("x86");
		node.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode1")));
		connect.addObject(node);
		LocalDbCalls.showAllContentOfDB();
		
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts222"));
		Node node2 = new NodeImpl("myNode2");
		node2.setHardwareType("x87");
		node2.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode2")));
		connect.addObject(node2);
		LocalDbCalls.showAllContentOfDB();
		
		connect.setReadContexts(
				NoviUris.createNoviURI("testbedContexts222"));
		log.debug("read context :");
		for (URI u : connect.getReadContexts())
		{
			log.debug(u.stringValue());
			
		}
		//Node dbNode= (Node) connect.getObject(
			//	LocalDbCalls.createNoviURI("myNode2"));
		ContextualConnection con = new ContextualConnection();
		con.setContext(NoviUris.createNoviURI("testbedContexts222"));
		Node dbNode= (Node) con.getObjectConnection().getObject(
				NoviUris.createNoviURI("myNode2"));
		LocalDbCalls.showAllContentOfDB();
		log.info("The Resource with URI: {}, hard: {}, lifetime : "+dbNode.getHasLifetimes().toString() + 
				" was found in the db.", dbNode.toString(), dbNode.getHardwareType());
		
		connect.setReadContexts(
				NoviUris.createNoviURI("testbedContexts"));
		log.debug("read context :");
		for (URI u : connect.getReadContexts())
		{
			log.debug(u.stringValue());
			
		}
		//Node dbNode1= (Node) connect.getObject(
		//		LocalDbCalls.createNoviURI("myNode1"));
		con.setContext(NoviUris.createNoviURI("testbedContexts"));
		Node dbNode1 = (Node) con.getObjectConnection().getObject(
						NoviUris.createNoviURI("myNode1"));
		log.info("The Resource with URI: {}, hard: {}, lifetime : "+dbNode1.getHasLifetimes().toString() + 
				" was found in the db.", dbNode1.toString(), dbNode1.getHardwareType());
		
		connect.setReadContexts();
		log.debug("read context :");
		for (URI u : connect.getReadContexts())
		{
			log.debug(u.stringValue());
			
		}
		Result<Node> nodes = connect.getObjects(Node.class);
		log.debug("I found the nodes");
		while (nodes.hasNext())
		{
			log.debug(nodes.next().toString());
		}
		
		//ConnectionClass.closeConnection();
		//ConnectionClass.openConnection();
		connect.setReadContexts(
				NoviUris.createNoviURI("testbedContexts"));
		log.debug("read context :");
		for (URI u : connect.getReadContexts())
		{
			log.debug(u.stringValue());
			
		}
		Result<Node> nodes2 = connect.getObjects(Node.class);
		log.debug("I found the nodes");
		while (nodes2.hasNext())
		{
			log.debug(nodes2.next().toString());
		}
		
	}
	
	
	//@Test
	public void testAddObjectWithContext() throws RepositoryException, QueryEvaluationException
	{
		//it does not find the lifetime for the node1  
		//if you make node1 and node2 as node then it find the wrong 
		//lifetime in the second case
		ManipulateDB.clearTripleStore();
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts"));
		LocalDbCalls.showAllContentOfDB();
		Node node = new NodeImpl("myNode1");
		node.setHardwareType("x86");
		node.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode1")));
		log.info("ADD SOME INFO WITH CONTEXT testbedContexts");
		connect.addObject(node);
		LocalDbCalls.showAllContentOfDB();
		
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts222"));
		Node node2 = new NodeImpl("myNode2");
		node2.setHardwareType("x87");
		node2.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode2")));
		log.info("ADD SOME INFO WITH CONTEXT testbedContexts222");
		connect.addObject(node2);
		LocalDbCalls.showAllContentOfDB();
		
		log.info("DELETE ALL THE testbedContexts");
		connect.clear(NoviUris.createNoviURI("testbedContexts"));
		LocalDbCalls.showAllContentOfDB();
		
		//ConnectionClass.closeConnection();
		//ConnectionClass.openConnection();
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts"));
		Node node3 = new NodeImpl("myNode1");
		node3.setHardwareType("x86");
		node3.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode1")));
		connect.addObject(node3);
		log.info("ADD BACK THE SAME INFORMATION WITH testbedContexts");
		LocalDbCalls.showAllContentOfDB();
		
		//connect.
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts99"));
		Node node9 = new NodeImpl("myNode1");
		node9.setHardwareType("x86");
		node9.setHasLifetimes(IMUtil.createSetWithOneValue(new LifetimeImpl("lifetimeNode1")));
		connect.addObject(node9.toString(), node9);
		log.info("ADD BACK THE SAME INFORMATION WITH testbedContexts99");
		LocalDbCalls.showAllContentOfDB();
		
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts"));
		Node node4 = new NodeImpl("myNode4");
		connect.addObject(node4.toString(), node4);
		log.info("ADD BACK SAME NEW INFORMATION WITH testbedContexts");
		LocalDbCalls.showAllContentOfDB();
		
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts333"));
		Node node5 = new NodeImpl("myNode4");
		connect.addObject(node5.toString(), node5);
		log.info("ADD THE SAME INFO AS BEFORE (myNode4) WITH testbedContexts333");
		LocalDbCalls.showAllContentOfDB();
		
		
		connect.setAddContexts(
				NoviUris.createNoviURI("testbedContexts333"));
		Node node6 = new NodeImpl("myNode6");
		node6.setHardwareType("x867");
		connect.addObject(node6.toString(), node6);
		log.info("ADD SAME NEW INFORMATION WITH testbedContexts333");
		LocalDbCalls.showAllContentOfDB();
		
		
	}
	
	
	//@Test
	public void testImplementedBy() throws RepositoryException, QueryEvaluationException
	{
		ManipulateDB.clearTripleStore();
		connect.setAddContexts(
				NoviUris.createNoviURI("testebedConexts"));
		LocalDbCalls.showAllContentOfDB();
		Node node = new NodeImpl("myNode");
		node.setHardwareType("x86");
		connect.addObject(node);
		LocalDbCalls.showAllContentOfDB();
		
		
		connect.setAddContexts(
				NoviUris.createNoviURI("slice12"));
		Node node2 = new NodeImpl("myNode");
		node2.setHardwareType("x88");
		VirtualNode vNode = new VirtualNodeImpl("virtualNode");
		vNode.setImplementedBy(IMUtil.createSetWithOneValue(node2));
		connect.addObject(vNode);
		LocalDbCalls.showAllContentOfDB();
		
		connect.setAddContexts(
				NoviUris.createNoviURI("slice22"));
		Node node3 = new NodeImpl("myNode");
		node3.setHardwareType("x88");
		VirtualNode vNode2 = new VirtualNodeImpl("virtualNode2");
		vNode2.setImplementedBy(IMUtil.createSetWithOneValue(node3));
		connect.addObject(vNode2);
		LocalDbCalls.showAllContentOfDB();
		
		/////////retrieve the implemented by v nodes//
		//connect.setReadContexts(
		//		LocalDbCalls.createNoviURI("slice12"));
		ObjectConnection con = connect;
		String queryString = 
				LocalDbCalls.PREFIXES +
				"SELECT ?vnodes where { \n" +
				" ?vnodes rdf:type im:VirtualNode .\n" +
				"?vnodes im:implementedBy im:myNode }\n ";

		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = con.prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return ;
		} catch (RepositoryException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return ;
		}

		Result<VirtualNode> vNodes;
		try {
			vNodes = query.evaluate(VirtualNode.class);
			if (!vNodes.hasNext())
			{
				log.info("there is no virtual nodes");
				return;
			}

			while (vNodes.hasNext())
			{
				VirtualNode current = vNodes.next();
				log.debug("I found the node {}", current);
				
			}

			

		} catch (QueryEvaluationException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return ;
		} catch (ClassCastException e) {
			log.warn("I can not get the cache lifetime for node {}", node);
			ConnectionClass.logErrorStackToFile(e);
			return;
		}
		
	}
	
	
	//@Test
	public void testStoreObjectsTopology() throws RepositoryException
	{
		connect.setAddContexts(
				NoviUris.createNoviURI("testebedConexts"));
		LocalDbCalls.showAllContentOfDB();
		Topology topology = new TopologyImpl("myTopol");
		Node node = new NodeImpl("myNode");
		node.setHardwareType("x86");
		CPU cpu = new CPUImpl("mycpu");
		cpu.setHasCPUSpeed(2.3f);
		node.setHasComponent(IMUtil.createSetWithOneValue(cpu));
		LocalDbCalls.showAllContentOfDB();
		topology.setContains(IMUtil.createSetWithOneValue(node));
	
		System.out.println(topology.getContains().toString());
		//connect.addObject(node.toString(), node);
		
		connect.addObject(topology);
		printAll();
		
		connect.setAddContexts();
		Node node2 = new NodeImpl("myNode2");
		connect.addObject(node2);
		printAll();
		connect.clear(NoviUris.createNoviURI("testebedConexts"));
		printAll();
		
	}
	
	
	//@Test
	public void testStoreLoop() throws RepositoryException
	{
		connect.setAddContexts(
				NoviUris.createNoviURI("testebedConexts"));
		LocalDbCalls.showAllContentOfDB();
		Node node = new NodeImpl("myNode");
		node.setHardwareType("x86");
		Node node2 = new NodeImpl("myNode2");
		node2.setConnectedTo(IMUtil.createSetWithOneValue(node));
		node.setConnectedTo(IMUtil.createSetWithOneValue(node2));

		//connect.addObject(node.toString(), node);
		
		connect.addObject(node);
		printAll();
		
		Node nodeGet = (Node) connect.getObject(
				NoviUris.createNoviURI("myNode"));
		System.out.println(nodeGet.toString());
		
		
	}
	
	private void printAll() throws RepositoryException
	{
		log.debug("Executing the statement: ");
		RepositoryResult<Statement> statements = 
				connect.getStatements(null,null,null, true);
		while (statements.hasNext())
		{
			Statement st = statements.next();
		     log.debug(st.getSubject()+", "
			+ st.getPredicate()+", "+st.getObject()+" "+st.getContext());
		     
		}
		statements.close();
		
	}
	
	//@Test
	public void testImplementedClasses() throws RepositoryException
	{
		Topology topology = new TopologyImpl("mytopology");
		Node node = ConnectionClass.getMemoryObjectFactory().createObject(
				NoviUris.createNoviURI("node1"), Node.class);
		ConnectionClass.getConnection2MemoryRepos().addObject(node.toString(), node);
		node.setHardwareType("x866");
		topology.setContains(IMUtil.createSetWithOneValue(node));
		ManipulateDB.clearTripleStoreMemory();
		//after you clean the memory rep, you still get the values from the node
		Node rNode = (Node)IMUtil.getOneValueFromSet(topology.getContains());
		System.out.println("node : " + rNode.toString() + " hard type : " + rNode.getHardwareType());
		
		Set<Resource> resources = topology.getContains();
		for (Resource r : resources)
		{
			if (r instanceof Node)
			{
				Node n = (Node) r;
				System.out.println(
						"node : " + n.toString() + " hard type : " + n.getHardwareType());
			}
		}
	}

}
