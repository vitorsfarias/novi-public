package eu.novi.resources.discovery.database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.openrdf.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
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
public class Inferencing {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(Inferencing.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}



	@Test
	public void testConnectedTo() throws RepositoryException {
		Node nodeA = new NodeImpl("nodeA");
		Node nodeB = new NodeImpl("nodeB");
		Node nodeC = new NodeImpl("nodeC");
		Node nodeD = new NodeImpl("nodeD");
		
		nodeA.setConnectedTo(IMUtil.createSetWithOneValue(nodeB));
		nodeB.setConnectedTo(IMUtil.createSetWithOneValue(nodeC));
		nodeC.setConnectedTo(IMUtil.createSetWithOneValue(nodeD));
		
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.addObject(nodeA);
		
		
		String queryString = 
				LocalDbCalls.PREFIXES +
				"SELECT ?node where { \n" +
				" ?node rdf:type im:Resource .\n" +
				"im:nodeA im:connectedTo+ ?node . }\n ";

		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = con.prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		Result<Node> nodes;
		try {
			nodes = query.evaluate(Node.class);
			if (!nodes.hasNext())
			{
				log.info("I can not find any node");
				return;
			}

			while (nodes.hasNext())
			{
				Node current = nodes.next();
				log.debug("I found the node {}", current.toString());
				
			}
			nodes.close();



		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		
		con.close();
		
	}
	
	@Test
	public void testNodeInterface() throws RepositoryException {

		ManipulateDB.clearTripleStore();
		//create the topology
		//nodeA is connected to NodeC through the router
		Node nodeA = new NodeImpl("nodeA");
		Node router = new NodeImpl("router");
		Node nodeC = new NodeImpl("nodeC");
		
		Interface infAout = new InterfaceImpl("interfaceA");
		Interface infRin = new InterfaceImpl("interfaceRin");
		Interface infRout = new InterfaceImpl("interfaceEout");
		Interface infCin = new InterfaceImpl("interfaceC");
		
		Link link1 = new LinkImpl("link1");
		Link link2 = new LinkImpl("link2");
		
		nodeA.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(infAout));
		infAout.setIsSource(IMUtil.createSetWithOneValue(link1));
		
		router.setHasInboundInterfaces(IMUtil.createSetWithOneValue(infRin));
		router.setHasOutboundInterfaces(IMUtil.createSetWithOneValue(infRout));
		infRin.setIsSink(IMUtil.createSetWithOneValue(link1));
		infRout.setIsSource(IMUtil.createSetWithOneValue(link2));
		
		nodeC.setHasInboundInterfaces(IMUtil.createSetWithOneValue(infCin));
		infCin.setIsSink(IMUtil.createSetWithOneValue(link2));

		
		
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.addObject(nodeA);
		con.addObject(nodeC);
		con.addObject(router);
		
		//LocalDbCalls.showAllContentOfDB();
		
		
		String queryString = 
				LocalDbCalls.PREFIXES +
				"SELECT ?node where { \n" +
				"?node rdf:type im:Node .\n" +
				"?inf rdf:type im:Interface .\n" +
				"im:nodeA im:hasOutboundInterface/im:isSource/im:hasSink ?inf .\n" +
				"?node im:hasInboundInterface ?inf.}\n";

		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = con.prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		Result<Resource> resources;
		try {
			resources = query.evaluate(Resource.class);
			if (!resources.hasNext())
			{
				log.info("I can not find any node");
				return;
			}

			while (resources.hasNext())
			{
				Resource current = resources.next();
				log.debug("I found the node {}", current.toString());
				
			}
			resources.close();



		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		
		con.close();
	}
	
	@Test
	public void testInterfaces() throws RepositoryException {

		ManipulateDB.clearTripleStore();
		//create the topology
		//nodeA is connected to NodeC through the router
		
		Interface infA = new InterfaceImpl("interfaceA");
		Interface infB = new InterfaceImpl("interfaceB");
		Interface infC = new InterfaceImpl("interfaceC");
		
		Link link1 = new LinkImpl("link1");
		Link link2 = new LinkImpl("link2");
		
		infA.setIsSource(IMUtil.createSetWithOneValue(link1));
		infB.setIsSink(IMUtil.createSetWithOneValue(link1));
		
		infB.setIsSource(IMUtil.createSetWithOneValue(link2));
		infC.setIsSink(IMUtil.createSetWithOneValue(link2));
		

		
		
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.addObject(infA);
		
		//LocalDbCalls.showAllContentOfDB();
		
		
		String queryString = 
				LocalDbCalls.PREFIXES +
				"SELECT ?inf where { \n" +
				"?inf rdf:type im:Interface .\n" +
				"im:interfaceA (im:isSource/im:hasSink)+ ?inf .}\n";

		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = con.prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}

		Result<Resource> resources;
		try {
			resources = query.evaluate(Resource.class);
			if (!resources.hasNext())
			{
				log.info("I can not find any interface");
				return;
			}

			while (resources.hasNext())
			{
				Resource current = resources.next();
				log.debug("I found the interface {}", current.toString());
				
			}
			resources.close();



		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		
		con.close();
	}

}
