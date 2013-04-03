package eu.novi.resources.discovery.database;

import java.util.Random;

import javax.xml.datatype.XMLGregorianCalendar;


import org.openrdf.model.Statement;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.LifetimeImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.UrisUtil;


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
 * It implements the queries for the IRM service. i.e. findResources.
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class IRMLocalDbCalls extends LocalDbCalls {

	private static final transient Logger log = 
			LoggerFactory.getLogger(IRMLocalDbCalls.class);

	/*	*//**
	 * the implementation of the find resources method for the local testbed
	 * @param topology The given topology that contain the requirements 
	 * @return a topology that contain the available resources
	 *//*
	public final Topology findLocalResources(final Topology topology) {


		Topology availResources = dummyFindLocalResources(topology);


		return availResources;
	}
	  */

	/**get the local slice information
	 * @param uri -- the URI of the slice
	 * @return the reservation group containing the slice information
	 * Or null if it is not found. All the object are translated to the implemented classes
	 */
	public static Reservation getLocalSlice(final String uri)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		try {

			con.setReadContexts(NoviUris.createURI(uri));
			Reservation slice = (Reservation) con.getObject(uri);
			log.info("The slice with URI: {}, was found in the db.", uri);
			//execPrintStatement(createNoviURI("virtualNode12"), null, null);
			//execPrintStatement(createURI(uri), null, null);

			if (slice.getContains() == null || slice.getContains().isEmpty())
			{
				log.info("The slice information is probaply stored in a remote platform");
				RepositoryResult<Statement> statements = con.getStatements(
						NoviUris.createURI(uri),
						NoviUris.createNoviURI(ReserveSlice.REMOTE_SLICE_PRED), null);
				while (statements.hasNext())
				{
					Statement st = statements.next();
					log.info("The remote platform is : {}", st.getObject());

				}

			}
			else
			{
				log.debug("contains : "+slice.getContains().toString());
				log.debug("lifetimes : "+slice.getHasLifetimes().toString());

			}
			IMCopy copier = new IMCopy();
			Reservation returnSlice = (Reservation) copier.copy(slice, -1);
			ConnectionClass.closeAConnection(con);
			return returnSlice;
		} catch (RepositoryException e) {

			ConnectionClass.logErrorStackToFile(e);
		}
		catch (ClassCastException e)
		{
			ConnectionClass.logErrorStackToFile(e);

		}
		log.warn("The slice with URI: {} was not found in the DB", uri);
		ConnectionClass.closeAConnection(con);
		return null;

	}


	/**get the local slice manifest information
	 * @param uri -- the URI of the slice
	 * @return the topology group containing the slice manifest information
	 * Or null if it is not found. All the object are translated to the implemented classes
	 */
	public static Topology getLocalSliceManifest(final String sliceUri)
	{

		String manifestRdf = OwlCreator.exportDBinOwl(
				NoviUris.getSliceManifestContextUri(sliceUri).toString());
		if (manifestRdf == null)
		{
			log.warn("I can not find the manifest for the slice {}", sliceUri);
		}

		IMRepositoryUtil imUtil = new IMRepositoryUtilImpl();
		Topology sliceManfiest = imUtil.getIMObjectFromString(manifestRdf, Topology.class);

		if (sliceManfiest == null)
		{
			log.warn("There is not topology object in the slice manifest: {}", sliceUri);
			return null;
		}
		log.info("I got the slice manifest for the slice : {}", sliceUri);
		return sliceManfiest;

	}


	/**get the entire substrate, ie. planetlab or federica
	 * @param uri -- the URI of the platform, can contain the novi base address
	 * or not
	 * @return the Platform group containing the nodes of the slice.
	 * Or null if it is not found
	 */
	public static Platform getSubstrate(final String uri)
	{
		log.info("I will get the substarte for {}", uri);
		String uri_c;
		if (uri.contains(NoviUris.NOVI_IM_BASE_ADDRESS))
			uri_c = uri;
		else
			uri_c = NoviUris.NOVI_IM_BASE_ADDRESS + uri;
		ObjectConnection con = ConnectionClass.getNewConnection();
		try {

			//set the context
			con.setReadContexts(ManipulateDB.TESTBED_CONTEXTS);
			Platform dbPlatform = (Platform) con.getObject(uri_c);
			log.info("The Platform with URI: {}, was found in the db.", uri);
			IMCopy copier = new IMCopy();
			Platform memPlatform = (Platform) copier.copy(dbPlatform, -1);


			/*Set<Resource> dbResources = dbPlatform.getContains();
			Set<Resource> memResources = memPlatform.getContains();
			log.debug("size: " + dbResources.size());
			int i = 1;
			for (Resource r : dbResources){
				log.debug("number is : " + i);
				i++;
				log.debug(r.toString());
				memResources.add(r);
				if (i == 31)
				{
					LocalDbCalls.execPrintStatementMemoryRepos(null,null,
							LocalDbCalls.createNoviURI("psnc.poz.vserver1"));
				LocalDbCalls.execPrintStatement(
						LocalDbCalls.createNoviURI("psnc.poz.vserver1"), null, null);
				}
				log.debug("memory object size : " +memPlatform.getContains().size());
			}*/
			//reset the contexts
			con.setReadContexts();
			ConnectionClass.closeAConnection(con);
			return memPlatform;

		} catch (RepositoryException e) {

			ConnectionClass.logErrorStackToFile(e);
		}
		catch (ClassCastException e)
		{
			//log.warn("I did not find the testbed substrate for {}", uri_c);
			ConnectionClass.logErrorStackToFile(e);

		}
		catch (StackOverflowError e)
		{
			log.error(e.getMessage());
			log.error("Stack Overflow error in getSubstrate");


		}
		log.warn("The Substrate with URI: {} was not found in the DB", uri_c);
		//reset the contexts
		con.setReadContexts();
		ConnectionClass.closeAConnection(con);
		return null;

	}





	/**create a random int of 6 digits
	 * @return the random number
	 */
	protected static Integer createRandomInt()
	{

		//note a single Random object is reused here
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(1000000);
		return randomInt;


	}


	/**It create and store in DB a Lifetime object.
	 * The start Time is the current time in GMT-0:00 zone
	 * and the end time is "months" months and "days" days later
	 * @param id -- the id of the lifetime, is added to the novi base address, it 
	 * cans also contain the novi base address
	 * @param months -- how many months later the lifetime will expire
	 * @param days -- how many days later the lifetime will expire
	 * @param minutes -- how many minutes later the lifetime will expire
	 * @param connection -- the objectConnection to perform the store
	 * @return the Lifetime object
	 */
	protected static final Lifetime storeLifetimeObjectInDb(String id, int months, int days, int minutes,
			ObjectConnection connection)
	{
		String id_ = UrisUtil.getURNfromURI(id);
		Lifetime sliceLife = new LifetimeImpl(id_);//ConnectionClass.getObjectFactory().
		//createObject("http://fp7-novi.eu/im.owl#" + id_, Lifetime.class);

		//set the start time
		XMLGregorianCalendar startTime = LocalDbCalls.currentDate();
		sliceLife.setStartTime(startTime);

		//set the end time, one month later for now
		XMLGregorianCalendar endTime = LocalDbCalls.getDate(months, days, minutes);
		sliceLife.setEndTime(endTime);
		try {
			connection.addObject(sliceLife);
		} catch (RepositoryException e) {
			log.error("Error in storing the Lifetime object in the DB");
			ConnectionClass.logErrorStackToFile(e);
		}

		return sliceLife;
	}


	/**get a node from the DB
	 * @param nodeURI -- the node URI
	 * @param connection -- the objectConnection to get the node from
	 * @param contexts -- the contexts to setRead, with out the base address
	 * @return the Node object or null if the node was not found or a problem
	 * was occur 
	 */
	protected static final Node getNodefromDB(String nodeURI, ObjectConnection connection,  String... contexts)
	{
		log.debug("I will get the node {} from the DB", nodeURI);
		ConstructFindResQuery queryStr = new ConstructFindResQuery(1, contexts);
		queryStr.setRdfType(1, "Node");
		queryStr.setBoundConstrain(1, nodeURI);
		queryStr.finalizeQuery();

		queryStr.printQuery(true);
		ObjectQuery query = null;
		try {
			query = connection.prepareObjectQuery(queryStr.getQuery());
			Node node = (Node) query.evaluate().singleResult();
			if (node == null)
			{
				log.debug("I can not find the node {} in the DB", nodeURI);

			}
			return node;

		} catch (MalformedQueryException e) {
			log.warn("Malformed query: I can not get the node {} from the DB", nodeURI);
			ConnectionClass.logErrorStackToFile(e);
		} catch (RepositoryException e) {
			log.warn("Repository exception: I can not get the node {} from the DB", nodeURI);
			ConnectionClass.logErrorStackToFile(e);
		}
		catch (QueryEvaluationException e) {
			log.warn("QueryEvaluationException: I can not get the node {} from the DB", nodeURI);
			ConnectionClass.logErrorStackToFile(e);
		}
		catch (ClassCastException e) {
			log.warn("ClassCastException: The object {} is not a node ", nodeURI);
			ConnectionClass.logErrorStackToFile(e);
		}
		return null;

	}




	/////////////////////////////////////////////////////////////////////////////
	//////////////////FIRST YEAR REVIEW DEMO CODE/////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////

	/*
	////////////////find local resources////////

	 *//**for the 1st year review DEMO.
	 * get the resources that are not reserved
	 * @param resources the given set of resources to check
	 * @return the resources that are available
	 *//*
	private static Set<Resource> getNotReserved(Set<Resource> resources)
	{
		Set<Resource> returnResources = new HashSet<Resource>();
		for (Resource r : resources)
		{
			if (!checkIfReserved(r))
			{
				returnResources.add(r);
				log.debug("The resource : " + r.toString() + ", is available");
			}
			else
			{
				log.debug("The resource : " + r.toString() + ", is reserved");
			}

		}
		return returnResources;
	}


	  *//**for the 1st year review DEMO.
	  * it check if a resource is reserved
	  * @param resource the resource to check
	  * @return true if it is reserved otherwise false
	  *//*
	private static boolean checkIfReserved(Resource resource)
	{
		boolean isReserved = false;
		Set<Lifetime> lifetimes = resource.getHasLifetimes();
		for(Lifetime l : lifetimes){
			if (!checkIfLifetimeIsNotValid(l))
				isReserved = true;
		}

		return isReserved;
	}



	   *//**for the 1st year review DEMO.
	   * a dummy implementation of the local find resources method
	   * @param topology
	   * @return the available resources in a topology
	   *//*
	private static Topology dummyFindLocalResources(final Topology topology)
	{

		Set<Resource> topologyResources = topology.getContains();
		Set<Resource> returnResources = new LinkedHashSet<Resource>(); 


		Iterator<Resource> it  = topologyResources.iterator();
		while (it.hasNext())
		{
			Resource currentResource = it.next();
			log.debug("Proccessing requirements from resource : " + currentResource.toString());
			if (currentResource instanceof Node)
			{
				Node node = (Node) currentResource;
				Set<Resource> currentList = getAvailableResourceForNodeSpecif(node);
				if (currentList == null || currentList.isEmpty())
				{
					log.info("No available resources that fill the requerment of Node :" + node);

				}
				else
				{
					log.debug(currentList.size()+
							" resources are available for the specification of Node : \n"
							+ node + "\n");
					returnResources.addAll(currentList);
				}

			}
			else
				log.warn("The topology contain resource that is not Node. " +
						"This resource will not be processed : " + currentResource); 

		}//end of while

		Topology retTopology = ConnectionClass.getMemoryObjectFactory().createObject(
				"http://fp7-novi.eu/im.owl#ReturnTopology", Topology.class);
		//filter the reserved resources and return only the available resources
		retTopology.setContains(getNotReserved(returnResources)); 

		//call Policy Service
		log.info("Calling Policy Service");
		PolicyServCommun.testAuthorizedForResource();


		return retTopology;
	}



	    *//**for the 1st year review DEMO.
	    * given the node specifications, it returns all the available nodes 
	    * in the DB that fill the requirement
	    * @param node a Node object that contain the required specification
	    * @return the available resources
	    *//*
	private static Set<Resource> getAvailableResourceForNodeSpecif(final Node node)
	{
		String hwType;
		Float cpuSpeed = null; 
		BigInteger cpuCores = null;
		BigInteger availCpuCores = null;
		Float memorySize = null;
		Float availMemorySize = null;
		Float storageSize = null; 
		Float availStorageSize = null;

		log.debug("Getting available resources for node " + node +
				" hard type=" + node.getHardwareType());
		hwType = node.getHardwareType();
		Set<NodeComponent> components = node.getHasComponent();

		Iterator<NodeComponent> iter = components.iterator();
		while (iter.hasNext())
		{
			NodeComponent nc = iter.next();
			if (nc instanceof CPU){
				log.debug("Has CPU component : " + nc.toString());
				cpuSpeed = ((CPU) nc).getHasCPUSpeed();
				cpuCores = ((CPU) nc).getHasCores();
				availCpuCores = ((CPU) nc).getHasAvailableCores();

				///make the specifications

				Iterator<Float> it2 = cpuSpeed.iterator();
				if (it2.hasNext())
					cpuSpeed1 = String.valueOf(it2.next().floatValue());

				Iterator<BigInteger> it3 = cpuCores.iterator();
				if (it3.hasNext())
					cpuCores1 = it3.next().toString();		

				Iterator<BigInteger> it4 = availCpuCores.iterator();
				if (it4.hasNext())
					availCpuCores1 = it4.next().toString();

			}
			else if (nc instanceof Memory)
			{
				log.debug("Has Memory component : " + nc.toString());
				memorySize = ((Memory) nc).getHasMemorySize();
				availMemorySize = ((Memory) nc).getHasAvailableMemorySize();


				Iterator<Float> it5 = memorySize.iterator();
				if (it5.hasNext())
					memorySize1 = String.valueOf(it5.next().longValue());		

				Iterator<Float> it6 = availMemorySize.iterator();
				if (it6.hasNext())
					availMemorySize1 = String.valueOf(it6.next().longValue());

			}
			else if (nc instanceof Storage)
			{
				log.debug("Has Storage component : " + nc.toString());
				storageSize = ((Storage) nc).getHasStorageSize();
				availStorageSize = ((Storage) nc).getHasAvailableStorageSize();


				Iterator<Float> it7 = storageSize.iterator();
				if(it7.hasNext())
				{
					storageSize1 = String.valueOf(it7.next().longValue());
				}


				Iterator<Float> it8 = availStorageSize.iterator();
				if(it8.hasNext())
				{
					availStorageSize1 = String.valueOf(it8.next().longValue());
				}
			}
		}

		String hwType1 = null;
		Iterator<String> it = hwType.iterator();
		if(it.hasNext())
		{
			hwType1 = it.next();
		}

		return execSPARQLQueryObject(hwType, String.valueOf(cpuSpeed), cpuCores.toString(), availCpuCores.toString(), 
				String.valueOf(memorySize), String.valueOf(availMemorySize), String.valueOf(storageSize),
				String.valueOf(availStorageSize));
	}



	     *//**for the 1st year review DEMO.
	     * Execution of a sparql query for a specific node and get objects
	     * You give the parameter for a node with hardware type and CPU, 
	     * Memory and Storage components. The null parameters will be omit
	     * from the query
	     * @param hwType hardware type
	     * @param cpuSpeed  CPU speed
	     * @param cpuCores number of CPU cores
	     * @param availCpuCores available cpu cores
	     * @param memorySize the memory size
	     * @param availMemorySize available memory size
	     * @param storageSize the storage size
	     * @param availStorageSize available storage size
	     * @return the result as a set of Resources, or null if an error occur
	     *//*
	protected static Set<Resource> execSPARQLQueryObject(String hwType, String cpuSpeed, String cpuCores, 
			String availCpuCores, String memorySize, String availMemorySize, String storageSize, 
			String availStorageSize) {


		String queryString = constructSPARQLQuery(hwType, cpuSpeed, cpuCores, 
				availCpuCores, memorySize, availMemorySize,  storageSize, availStorageSize);

		return execSPARQLQueryObject(queryString);

	}


	      *//**for the 1st year review DEMO.
	      * it execute the given query string and it prints the results.
	      * It evaluates the results as a set of Node
	      * @param queryString the query to execute
	      * @return the result as a set of Resources, or null if an error occur
	      *//*
	protected static Set<Resource> execSPARQLQueryObject(String queryString) 
	{


		log.debug("\nExecuting Query \n" + queryString + "\n");
		ObjectQuery query = null;
		try {
			query = ConnectionClass.getConnection2DB().prepareObjectQuery(queryString);
		} catch (MalformedQueryException e) {
			ConnectionClass.logErrorStackToFile(e);
			return null;
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
			return null;
		}

		Result<Node> nodes;
		Set<Resource> resList = new LinkedHashSet<Resource>();
		try {
			nodes = query.evaluate(Node.class);

			while (nodes.hasNext())
			{
				Node current = nodes.next();
				resList.add(current);
				log.debug("Node : " + current + " || Components :   " 
						+current.getHasComponent());
			}
			return resList;

		} catch (QueryEvaluationException e) {
			e.printStackTrace();
			return null;
		}

	}


	       *//**for the 1st year review DEMO.
	       * it constructs a sparql query for a node with a specific 
	       * hardware type and a CPU, memory and storage service.
	       * The null parameters will be omit
	       * from the query
	       * @param hwType hardware type
	       * @param cpuSpeed  CPU speed
	       * @param cpuCores number of CPU cores
	       * @param availCpuCores available cpu cores
	       * @param memorySize the memory size
	       * @param availMemorySize available memory size
	       * @param storageSize the storage size
	       * @param availStorageSize available storage size
	       * @return
	       *//*
	private static String constructSPARQLQuery(String hwType, String cpuSpeed, String cpuCores, 
			String availCpuCores, String memorySize, String availMemorySize, String storageSize, 
			String availStorageSize)
	{
		String queryString = 
				"PREFIX im:<http://fp7-novi.eu/im.owl#>\n "+
						"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
						"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> \n" +
						"SELECT ?node where { ?node rdf:type im:Node . \n";
		if(hwType != null){
			queryString +=
						"	      ?node im:hardwareType ?hwtype .\n" +
						"	      FILTER regex(str(?hwtype), \"" + hwType + "\") .\n ";
		}
		if (cpuSpeed != null || cpuCores != null || availCpuCores != null)
		{
			queryString += 

					"	      ?node im:hasComponent ?comp . \n" +
					"	      ?comp  rdf:type im:CPU . \n";
			if (cpuSpeed != null)
			{
				queryString +=
						" 	      ?comp  im:hasCPUSpeed ?cpuSpeed . \n" +
						"	      FILTER (xsd:float(?cpuSpeed) >= xsd:float(" + cpuSpeed + ")) . \n";
			}

			if (cpuCores != null)
			{
				queryString += 
						" 	      ?comp  im:hasCores ?cores . \n" +
						"	      FILTER (xsd:integer(?cores) = xsd:integer(" + cpuCores + ")) . \n";
			}
			if (availCpuCores != null)
			{
				queryString +=
						" 	      ?comp  im:hasAvailableCores ?avCores . \n" +
						"	      FILTER (xsd:integer(?avCores) >= xsd:integer(" + availCpuCores + ")) . \n";
			}
		}

		if (memorySize != null || availMemorySize != null)
		{
				queryString += 

					"	      ?node im:hasComponent ?comp2 . \n" +
					"	      ?comp2  rdf:type im:Memory . \n";
				if (memorySize != null)
				{
					queryString += 
					" 	      ?comp2  im:hasMemorySize ?memSize . \n" +
					"	      FILTER (xsd:float(?memSize) = xsd:float(" + memorySize + ")) . \n";
				}
				if (availMemorySize != null)
				{
					queryString +=
					" 	      ?comp2  im:hasAvailableMemorySize ?availMemSize . \n" +
					"	      FILTER (xsd:float(?availMemSize) >= xsd:float(" + availMemorySize + ")) . \n";
				}
		}

		if (storageSize != null || availStorageSize != null)
		{
			queryString +=
					"	      ?node im:hasComponent ?comp3 . \n" +
					"	      ?comp3  rdf:type im:Storage . \n";
			if (storageSize != null)
			{
				queryString +=
					" 	      ?comp3  im:hasStorageSize ?stoSize . \n" +
					"	      FILTER (xsd:float(?stoSize) = xsd:float(" + storageSize + ")) . \n";
			}

			if (availStorageSize != null)
			{
				queryString +=
					" 	      ?comp3  im:hasAvailableStorageSize ?availStoSize . \n" +
					"	      FILTER (xsd:float(?availStoSize) >= xsd:float(" + availStorageSize + ")) . \n";
			}
		}

		queryString += " }";

		return queryString;

	}


	////////////RESERVE SLICE//////////////////////////////////////////////////

	        *//**for 1st Year DEMO
	        * It reserve the slice (bound request).
	        * @param boundRequest the topology for reserved
	        * @return the ID of the slice
	        *//*
	public Integer reserveLocalSlice(final Topology boundRequest)
	{
		//the real implementation have to use
		// request handler and reserve the topology on the testbed
		return reserveLocalSliceDemo(boundRequest);

	}


	         *//**for the 1st year review DEMO.
	         * a dummy reserve slice for the 1st year review DEMO.
	         * it reserve the slice in the local db not in the testbed.
	         * it set for every node in the topology the lifetime (for one month) and it
	         * reserver the whole Node
	         * @param boundRequest -- the bound topology to reserve.
	         * @return
	         *//*
	private static Integer reserveLocalSliceDemo(final Topology boundRequest)
	{
		Integer sliceID = createRandomInt();
		boolean succed = false;
		log.debug("I will create a new slice with ID : " + sliceID);
		//this lifetime refers to the slice
		Lifetime lifetime = storeLifetimeObjectInDb("slice_" + sliceID, 1, 0, 0);
		Set<Resource> resources = boundRequest.getContains(); //the given resources
		for (Resource r : resources)
		{
			Node node = null; //the node from the DB
			try {
				String resourceStr = r.toString();
				if (r instanceof VirtualNode)
				{
					//we assume that is a virtual node
					VirtualNode vNode = (VirtualNode) r;
					Set<Node> nodesOrNetEl = vNode.getImplementedBy();
					Node nod = (Node) nodesOrNetEl;
					resourceStr = nod.toString();
				}
				node = (Node) ConnectionClass.getConnection2DB().getObject(resourceStr);
				log.debug("I found in the DB the node : " + node);
				if (reserveNode(node, lifetime))
				{
					log.debug("The node : " + r.toString() + ", is reserved successfuly");
					succed = true;
				}
				else
				{
					log.warn("Failed to reserve the node : " + r.toString());
				}
			} catch (RepositoryException e) {
				log.warn("I cant find in the DB the node : " + r.toString()
						+ "\nThe node will not be reserved");
				ConnectionClass.logErrorStackToFile(e);

			} 
			catch (ClassCastException e)
			{
				log.warn("I cant find in the DB the node : " + r.toString()
						+ "\nOr the typy of object is different from the expected." +
						"The node will not be reserved");
				ConnectionClass.logErrorStackToFile(e);

			}

		}
		//log.info("Reserving the slice to the testbed...");
		//String rspec = TestbedCommunication.getCalls2TestbedFromRH().
		//		translateTopologyToRequestRSpec(boundRequest);
		//log.info("The request in RSpec : \n" + rspec);
		if (succed)
		return sliceID;
		else 
		{
			log.warn("The reservation failed. None resource was reserved");
			return -1;
		}
	}// end reserveLocalSliceDemo



	          *//**For the DEMO.
	          * reserve a node to the local db.
	          * If the node is already reserved then the reservation
	          * can not been done.
	          * @param node -- the given bound node
	          * @param sliceLifetime - the object Lifetime that is allready stored in the DB
	          * @return true if the reservation is done. otherwise false
	          *//*
	private static boolean reserveNode(Node node, Lifetime sliceLifetime)
	{
		log.debug("I will reserve the node : " + node + ", in the lifetime : " + sliceLifetime);
		if (checkIfReserved(node))
		{
			log.warn("The node : " + node + ", is already reserved. I can not reserve it");
			return false;
		}
		else
		{
			log.debug("The node : " + node + ", is not reserved.");
			//set lifetime to node
			node.setHasLifetimes(IMUtil.createSetWithOneValue(sliceLifetime));
			return true;
		}

	}*/
}
