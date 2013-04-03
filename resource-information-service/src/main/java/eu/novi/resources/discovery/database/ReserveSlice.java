package eu.novi.resources.discovery.database;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.activemq.util.ByteArrayInputStream;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.result.Result;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import eu.novi.im.core.Interface;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Node;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Reservation;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.ReservationImpl;
import eu.novi.im.core.impl.TopologyImpl;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMUtil;
import eu.novi.im.util.UrisUtil;
import eu.novi.im.util.Validation;
import eu.novi.requesthandler.sfa.response.RHCreateDeleteSliceResponse;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.database.communic.TestbedCommunication;
import eu.novi.resources.discovery.response.ReserveMess;
import eu.novi.resources.discovery.response.ReserveResponse;
import eu.novi.resources.discovery.response.ReserveResponseImp;
import eu.novi.resources.discovery.util.NoviIPs;

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
 * It reserve the slice to the testbed and it creates the slice to the NOVI DB
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ReserveSlice {
	
	private static final transient Logger log =
			LoggerFactory.getLogger(ReserveSlice.class);
	
	private Integer sliceID = -1;
	private String sliceURN = null;
	private URI sliceURI = null;
	LockSession lockSession = null;
	private ReserveMess errorMessage = ReserveMess.NO_ERROR;
	private String message = null;
	private String userLoginInfo = "";
	//will be the stored information in the remote platform
	private Reservation sliceLimited = null;
	
	private Lifetime sliceLifetime;
	private final static int MONTHS_EXPIRE = 0; //the duration of slice in months
	private final static int DAYS_EXPIRE = 13; //the duration of slice in days

	//the name of the slice (reservation group) is the SLICE_PREFIX + sliceID
	protected final static String SLICE_PREFIX = "slice_"; //TODO change to private
	
	protected final static String REMOTE_SLICE_PRED = "remoteSlicePlatform";
	
	//for the planetlab ip configuration
	//private final static String PLANETLAB_URI = NoviUris.NOVI_IM_BASE_ADDRESS + "PlanetLab";
	
	
	/**
	 * if true then it store the slice information in OWL files
	 */
	public static boolean storeOwlFiles = false;
	//the address space, it starts from 101 and is in the format of 192.168.101.0/24
	private static int ipAddressSpace = 100;
	
	private ObjectConnection con; //the connection to the repository
	
	////////EXTERNAL SERVICES///////////////////////////////////////
	//the class that makes the communication to the testbed
	//private TestbedCommunication testbedComm; //get from IRMCallsImpl
	
	

	
	
	/**
	 * It reserve the slice (bound request).
	 * @param boundRequest the topology for reserved;
	 * It will be a Topology object that will contain virtualNodes.
	 * The virtual nodes can have also nodeComponents such as cpu, memory and storage.
	 * Each virtual node must have an implementBy node.
	 * The node will be a physical machine. For the node object
	 * I will take into account only the URI of the node (so I can find that in the DB).
	 * @return the ReserveResponse object that contain the reservation information
	 */
	public final ReserveResponse reserveLocalSlice(final Topology boundRequest, Integer sliceID,
			NOVIUserImpl user)
	{
		initVariables();
		log.info("Reserving the the slice with ID {} ...", sliceID);
		
		if (!checkBoundTopology(boundRequest))
		{
			return constructResponse();
		}

		
	
		this.sliceID = sliceID; //set the slice ID
		sliceURN = SLICE_PREFIX + sliceID;
		log.info("Setting the IPs...");
		setPlanetLabIPs(boundRequest);
		LockResources lockRs = new LockResources();
		lockSession = lockRs.startLockResources(boundRequest, sliceID);
		log.info("Reserving the slice to the testbed...");
		//TODO: see what to do with the user and sliceURN of the input
		RHCreateDeleteSliceResponse rsp = TestbedCommunication.reserveSlice(user, sliceURN, boundRequest);
		if(rsp == null){
			errorMessage = ReserveMess.RESERVATION_TO_TESTEBED_FAILED;
			message = "Testbed calls returns null";
			log.warn("Failed to reserve the slice to the testbed. The reservation failed. \n"
					+ message);
			sliceID = -1;
			lockRs.startUnlockResources(lockSession);
			return constructResponse();
			
		} else
		if (rsp.hasError())
		{

			errorMessage = ReserveMess.RESERVATION_TO_TESTEBED_FAILED;
			message = "Message from testbed: " + rsp.getErrorMessage();
			log.warn("Failed to reserve the slice to the testbed. The reservation failed. \n"
					+ message);
			sliceID = -1;
			lockRs.startUnlockResources(lockSession);
			return constructResponse();
			
		}
		else
		{
			log.info("The reservation to the testbed was successful. " +
					"Now I will create the slice to the NOVI DB. The slice id is: {}", sliceID);
			//sliceId = rsp.getSliceID().replaceFirst(LocalDbCalls.NOVI_IM_BASE_ADDRESS, "");
		}
		log.info("The user login info is : ", rsp.getUserLogin());
		if (rsp.getUserLogin() != null )
		{
			userLoginInfo = rsp.getUserLogin();
		}
		
		log.info("Storing the new slice information to the NOVI local DB...");
		con = ConnectionClass.getNewConnection();
		sliceURI = NoviUris.createNoviURI(sliceURN);
		//set the add context
		con.setAddContexts(sliceURI);
		
		//create and store the slice lifetime
		storeSliceLifetimes();
		if (errorMessage != ReserveMess.NO_ERROR)
		{
			//an error was happened
			ConnectionClass.closeAConnection(con);
			return constructResponse();
		}
		
		//slice manifest
		storeSliceManifest(rsp.getTopologyCreated());
		
		//store the slice platform objects
		storePlatforms(rsp.getListOfTestbedsWhereSliceIsCreated());
		
		//store the noviUser
		NOVIUserClass.storeNoviUserSlice(user, sliceURI.toString());
		
		//store the slice info to the DB
		Reservation res = storeSliceInfo2DB(boundRequest);
		LocalDbCalls.printGetCurrentSlices();
		if (errorMessage == ReserveMess.NO_ERROR)
		{
			log.info("The reservation was successful. The slice ID is: " + sliceID);
			message = "The reservation was successful";
			
			//store the slice information in owl files in the disk
			/*if (storeOwlFiles)
			{
				storeOwlFiles();
				
			}*/
			storeOwlFiles();
			
			//give the new slice info to policy service.
			Topology top = new TopologyImpl(res.toString());
			top.setContains(res.getContains());
			//Note: we actually need to pass sessionID to Policy instead of NULL
			PolicyServCommun.sendNewSliceInfo(null, top,
					sliceURI.toString());
		}
		//reset add context
		//ConnectionClass.getConnection2DB().setAddContexts();
		ConnectionClass.closeAConnection(con);
		
		log.info("Get the stored topology and check the hasSink hasSource of the links");
		Validation validation = new Validation();
		if (!validation.checkLinksForSinkSource(
				IRMLocalDbCalls.getLocalSlice(sliceURI.toString())).equals(""))
		{
			log.warn("There is a problem with the stored bound topology. " +
					"Some relation on a link are missing");


		}
		
		return constructResponse();
		
	}
	
	
	
	
	
	
	/**
	 * It updates an existing slice.
	 * it doesn't set the IPs
	 * @param boundRequest is the new topology;
	 * @return the ReserveResponse object that contain the reservation information
	 */
	public final ReserveResponse updateLocalSlice(final Topology boundRequest, Integer sliceID)
	{
		initVariables();
		log.info("Updating the the slice with ID {} ...", sliceID);

		if (!checkBoundTopology(boundRequest))
		{
			return constructResponse();
		}
		
		this.sliceID = sliceID; //set the slice ID
		sliceURN = SLICE_PREFIX + sliceID;
		
		log.info("Setting the IPs...");
		setPlanetLabIPs(boundRequest);
		
		Reservation oldSlice = IRMLocalDbCalls.getLocalSlice(createSliceURI(sliceID.toString()));
		if (oldSlice == null)
		{
			errorMessage = ReserveMess.SLICE_NOT_EXIST;
			message = "The slice with ID " + sliceID + ", doesn't exist in the DB, I can not update it";
			log.warn(message);
			return constructResponse();
			
		}
		
		log.info("Updating the slice to the testbed...");
		
		//get the novi user
		String sliceUri = createSliceURI(sliceID.toString());
		NOVIUserImpl user = NOVIUserClass.getNoviUserSlice(sliceUri);
		if (user == null)
		{
			errorMessage = ReserveMess.NOVIUSER_NOT_FOUND;
			message = "I can not find the NOVI user for the slice with ID " + sliceID + "," +
					"I can not update it";
			log.warn(message);
			return constructResponse();
			
		}
		Topology sliceManifest = IRMLocalDbCalls.getLocalSliceManifest(sliceUri);
		RHCreateDeleteSliceResponse rsp = TestbedCommunication.
				updateSlice(user, sliceURN, boundRequest, (TopologyImpl)sliceManifest);
		if (rsp.hasError())
		{

			errorMessage = ReserveMess.RESERVATION_TO_TESTEBED_FAILED;
			message = "Message from testbet: " + rsp.getErrorMessage();
			log.warn("Failed to update the slice to the testbed. The reservation failed. \n"
					+ message);
			sliceID = -1;
			return constructResponse();
			
		}
		else
		{
			log.info("The update slice to the testbed was successful. " +
					"Now I will update the slice to the NOVI DB. The slice id is: {}", sliceID);
			//sliceId = rsp.getSliceID().replaceFirst(LocalDbCalls.NOVI_IM_BASE_ADDRESS, "");
		}
		
		log.info("The user login info is : ", rsp.getUserLogin());
		if (rsp.getUserLogin() != null )
		{
			userLoginInfo = rsp.getUserLogin();
		}
		//give the new slice info to policy service.
		//log.info("Contacting policy service to send the new objects...");
		//PolicyServCommun.sendNewSliceInfo(boundRequest,
		//		LocalDbCalls.createNoviURI(sliceURN).toString());
		

		
		log.info("Updating the slice information to the NOVI local DB...");
		con = ConnectionClass.getNewConnection();
		//set the add context
		con.setAddContexts(NoviUris.createNoviURI(sliceURN));
		
		//delete the old information
		DeleteSlice.deleteLocalSliceInfo(createSliceURI(sliceID.toString()));
		
		//create and store the new slice lifetime
		storeSliceLifetimes();
		if (errorMessage != ReserveMess.NO_ERROR)
		{
			//an error was happened
			ConnectionClass.closeAConnection(con);
			return constructResponse();
		}
		
		//slice manifest
		storeSliceManifest(rsp.getTopologyCreated());
		
		//store the slice platform objects
		storePlatforms(rsp.getListOfTestbedsWhereSliceIsCreated());
		
		//store the user
		NOVIUserClass.storeNoviUserSlice(user, NoviUris.createNoviURI(sliceURN).toString());
		//store the new slice info to the DB
		storeSliceInfo2DB(boundRequest);
		
		if (errorMessage == ReserveMess.NO_ERROR)
		{
			log.info("The update slice was successful. The slice ID is: " + sliceID);
			message = "The update slice was successful";
		}
		//reset add context
		//ConnectionClass.getConnection2DB().setAddContexts();
		ConnectionClass.closeAConnection(con);
		return constructResponse();
		
	}
	
	/**check the boound topology if it is valid.
	 * It checks also the links for has source has sink
	 * @param boundRequest
	 * @return true if it valid otherwise false
	 */
	private boolean checkBoundTopology(Topology boundRequest)
	{
		if (boundRequest == null || boundRequest.getContains() == null)
		{
			message = "The bound request for the reservation is null";
			log.warn(message);
			errorMessage = ReserveMess.BOUND_REQUEST_IS_NULL;
			return false;
		}
		
		if (boundRequest.getContains().size() == 0)
		{
			message = "The bound request for the reservation is empty";
			log.warn(message);
			errorMessage = ReserveMess.BOUND_REQUEST_IS_EMPTY;
			return false;
		}
		
		Validation validation = new Validation();
		String st = validation.checkLinksForSinkSource(boundRequest);
		if (!st.equals(""))
		{
			message = "The bound topology is not valid. Not all the links have hasSource and hasSink. " +
					"I will quit the reservation\n" + st;
			log.warn(message);
			errorMessage = ReserveMess.INVALID_BOUND_TOPOLOGY;
			return false;
		}
		
		return true;
		
	}
	
	/**store locally the name of the slice and the remote platform that have the slice info
	 * @param slice
	 * @param remotePlatform platform name with out the base address
	 */
	public void storeRemoteSliceInfo(Reservation slice, String remotePlatform)
	{
		
		
		con = ConnectionClass.getNewConnection();
		//set the context
		con.setAddContexts(NoviUris.createURI(slice.toString()));
		
		try {
			con.addObject(slice.toString(), slice);
			con.add(
					NoviUris.createURI(slice.toString()), 
					NoviUris.createNoviURI(REMOTE_SLICE_PRED), 
					NoviUris.createNoviURI(remotePlatform)); 
			log.info("The remote slice {} was stored succesfully", slice.toString());
		} catch (RepositoryException e) {
			log.warn("Problem in storing the remote slice {} for {}",slice.toString(), remotePlatform);
			ConnectionClass.logErrorStackToFile(e);
		}
		
		ConnectionClass.closeAConnection(con);
		
	}
	
	
	

	
	
	
	/**it stores the manifest with a unique context
	 * @param manifestRdf the manifest in a RDF/OWL format
	 */
	private void storeSliceManifest(String manifestRdf)
	{//the information is stored as Topology object and not as Reservation object
		log.info("Storing the manifest for the slice {}", sliceID);
		if (manifestRdf == null || manifestRdf.isEmpty())
		{
			log.warn("The manifest is empty or null");
			return ;
		}
		
		log.debug("The manifest is: \n {}", manifestRdf);
		ObjectConnection tempCon = ConnectionClass.getNewConnection();
		try {
			// convert String into InputStream
			InputStream is = new ByteArrayInputStream(manifestRdf.getBytes());
			// read it with BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			URI context = NoviUris.getSliceManifestContextUri(sliceURI.toString());
			tempCon.add(br, "", RDFFormat.RDFXML, context);
			log.info("The manifest was stored");
			
			//remove all the triples about policy, namely ManagedEntity
			RepositoryResult<Statement> polSta = tempCon.getStatements(null, null, NoviUris.createPolicyURI("ManagedEntity"), context);
			//Statement managEntSta  = new StatementImpl(null, null, NoviUris.createPolicyURI("ManagedEntity"));
			tempCon.remove(polSta, context);
		} catch (RepositoryException e) {
			log.warn(e.getMessage());
			log.warn("Problem to store the manifest for the slice {}", sliceURI);
		} catch (RDFParseException e) {
			log.warn(e.getMessage());
			log.warn("Problem to store the manifest for the slice {}", sliceURI);
		} catch (IOException e) {
			log.warn(e.getMessage());
			log.warn("Problem to store the manifest for the slice {}", sliceURI);
		}
		finally {
			ConnectionClass.closeAConnection(tempCon);
		}
		
	}
	

	
	
	/**store the user login info as a LoginComponent
	 * @param loginInfo
	 */
	/*private void storeUserLoginInfo(String loginInfo)
	{
		log.info("Storing the user info {} for the slice {}", loginInfo, sliceID);
		LoginComponent login = new LoginComponentImpl("loginComponent_" + sliceID);
		login.setHasLoginIPv4Address(IMUtil.createUnitIPAddress(login.toString() + "-ip", loginInfo));
		try {
			con.addObject(login);
		} catch (RepositoryException e) {
			log.warn(e.getMessage());
			log.warn("Problem to store the user login info");
		}
	}*/
	
	
	/**it store the platform objects for the slice
	 * @param platforms the platforms URIs
	 */
	private void storePlatforms(List<String> platforms)
	{
		if (platforms == null)
		{
			log.warn("The list of platforms is null");
			return;
			
		}
		
		for (String s : platforms)
		{
			log.info("I will store the platform {} for the slice {}", s, sliceID);
			Platform plf = new PlatformImpl(s);
			
			try {
				con.addObject(plf);
			} catch (RepositoryException e) {
				log.error("Error in storing the platform object in the DB");
				ConnectionClass.logErrorStackToFile(e);
			}
		}
		
	}
	
	
	
	/**
	 * it stores the slice information in OWL files.
	 * It creates one file for the slice and 
	 * one file for each one virtual node in the slice
	 */
	private void storeOwlFiles()
	{
		OwlCreator creator = new OwlCreator();
		String sliceUri = NoviUris.createNoviURI(sliceURN).toString();
		creator.storeSliceInfoToFile(sliceUri);
		//creator.storeVNodesToFiles(sliceUri);
		
	}
	
	
	/**
	 * set the variables to null, for a new execution of reserve/update slice
	 */
	private void initVariables()
	{
		sliceID = -1;
		sliceURN = null;
		errorMessage = ReserveMess.NO_ERROR;
		message = null;
		sliceLimited = null;
		sliceLifetime = null;
		userLoginInfo = "";
		
	}
	
	/**
	 * create and store the lifetimes for this slice;
	 * the URI is created based on the sliceID (LIFETIME_PREFIX + sliceID)
	 * and the duration is defined from the final variables in this class
	 */
	protected void storeSliceLifetimes()
	{
		String lifetime_uri = NoviUris.getSliceLifetimeURI(sliceURI.toString()).toString();
		log.debug("I will create a new slice lifetime with ID : " + sliceID + 
				" and duration " + MONTHS_EXPIRE + " months, " + DAYS_EXPIRE + " days.");
		//this lifetime refers to the slice
		sliceLifetime = IRMLocalDbCalls.storeLifetimeObjectInDb(
				lifetime_uri, MONTHS_EXPIRE, DAYS_EXPIRE, 0, con);
		if (sliceLifetime == null)
		{
			log.warn("Problem storing the slice lifetime, with slice ID {}, in the db." +
					" The reserve slice will quit", sliceID);
			errorMessage = ReserveMess.STORE_SLICE_LIFETIME_DB_ERROR;
			message = "The Slice was created in testbed, " +
					"but a problem occur in storing the slice info to the NOVI DB." +
					" The slice exist in the testbed";
		}

	}


	
	/**it find the planetlab nodes in the topology and it set the ips
	 * @param boundRequest
	 */
	protected void setPlanetLabIPs(Topology boundRequest)
	{

		String planetLabKeyWord = "novipl";//look for that world in the PL physical machines
		//The inbound interface must end to in and the outbound to out
		ipAddressSpace++; //increase the address space
		int ipUnit = 1; //the last digit of the ip, start from 1
		for (Resource r : boundRequest.getContains())
		{//for all the resources that the topology contains

			//check for virtual nodes
			if (!(r instanceof VirtualNode))
			{
				log.warn("The resource {} is not virtual node. I will not assign IP",
						r.toString());
				continue;
			}

			log.debug("Setting the PL IPs: The resource {} is a virtual Node", r);
			//check if it has implementedBy
			if (((VirtualNode)r).getImplementedBy() == null || 
					((VirtualNode)r).getImplementedBy().size() == 0)
			{
				log.warn("The virtual node {} doesn't have implementBy node", r);
				continue;

			}

			//get the phusical machine, we checked before that it is not null
			Node phMachine = ((VirtualNode)r).getImplementedBy().iterator().next();
			if (!phMachine.toString().contains(planetLabKeyWord))
			{//the physical node should be planetlab node

				log.info("The physical node {} is not a planetlab node", r.toString());
				continue;
			}
			
			//the virtual node has implementBy a planetlab physical node 
			log.info("The Resource {} is in PlanetLab. I will assign IP", r.toString());


			VirtualNode  vNode = (VirtualNode) r;
			Set<Interface> inInterfaces = vNode.getHasInboundInterfaces();
			Set<Interface> outInterfaces = vNode.getHasOutboundInterfaces();
			if (inInterfaces != null && outInterfaces != null)
			{//assign the private IPs to the virtual node
				for (Interface in : inInterfaces)
				{
					int inLength = in.toString().length();
					String ip = getNewPrivateIPInTheSpace(ipUnit++);
					in.setHasIPv4Address(IMUtil.createUnitIPAddressSet(ip , ip));
					log.info("The ip {} was assigned to interface {}", ip, in.toString());
					for (Interface out : outInterfaces)
					{
						int outLength = out.toString().length();
						if (in.toString().substring(0, inLength-2).equals(
								out.toString().subSequence(0, outLength-3)))
						{
							//the inInterface end in in (2 chars) and the outInterface
							//end in out (3chars)
							log.debug("Match. inInterface : {}, outInterface : {}",
									in.toString(), out.toString());
							out.setHasIPv4Address(IMUtil.createUnitIPAddressSet(ip , ip));
							log.info("The ip {} was assigned to interface {}",
									ip, out.toString());

							break;

						}
					}
				}


			}
			else
			{
				log.warn("The Virtual node {}, doesn't have out or in interface." +
						"I can not assign private IPs", vNode.toString());
			}



			/////////assign public IPs to the physical node///////////
			//the virtual node has implementBy a planetlab physical node 

			log.info("Setting the public IP for {}", phMachine.toString());

			//I need the hostname in order to find out the IP
			String hstn = phMachine.getHostname();
			if (hstn == null)
			{
				log.info("The hostname was not set in the request. " +
						"I will try to get it from the DB");
				hstn = LocalDbCalls.getNodeHostname(phMachine.toString());

			}

			if (hstn == null)
			{
				log.warn("I can not get the hostname. The public IP can not be set");
				continue;

			}
			
			//the hostname was found
			String ip = NoviIPs.getPublicIP(hstn);
			log.info("The public IP for the {} is {}", hstn, ip);
			//in interface
			Interface inInterf = IMUtil.getOneValueFromSet(
					phMachine.getHasInboundInterfaces());
			if (inInterf == null)
			{
				Set<Interface> newInInterf = new HashSet<Interface>();
				inInterf = new InterfaceImpl(phMachine.toString() + "interface-in");
				newInInterf.add(inInterf);
				phMachine.setHasInboundInterfaces(newInInterf);
			}
			inInterf.setHasIPv4Address(IMUtil.createUnitIPAddressSet(
					UrisUtil.getURNfromURI(inInterf.toString()) + "-ip", ip));

			//out interface
			Interface outInterf = IMUtil.getOneValueFromSet(
					phMachine.getHasOutboundInterfaces());
			if (outInterf == null)
			{
				Set<Interface> newOutInterf = new HashSet<Interface>();
				outInterf = new InterfaceImpl(phMachine.toString() + "interface-out");
				newOutInterf.add(outInterf);
				phMachine.setHasOutboundInterfaces(newOutInterf);
			}
			outInterf.setHasIPv4Address(IMUtil.createUnitIPAddressSet(
					UrisUtil.getURNfromURI(outInterf.toString()) + "-ip", ip));

		}//end of for loop
	}


	
	private String getNewPrivateIPInTheSpace(int unit)
	{
		return "192.168." + ipAddressSpace + "." + unit + "/24";
	}
	
	
	
	/**store the bound topology to the testbed.
	 * it create a reservation group with the sliceURI.
	 * it also set the lifetime to the reservation
	 * @param boundRequest the bound request. The setContains is set to null at the end
	 * @return the reservation that was stored. Or null if an error happened
	 */
	private Reservation storeSliceInfo2DB(final Topology boundRequest)
	{
		Reservation sliceRese = new ReservationImpl(sliceURN);
		sliceRese.setHasLifetimes(IMUtil.createSetWithOneValue(sliceLifetime));
		sliceRese.setContains(boundRequest.getContains());
		sliceRese.setAutoUpdateOnfailure(boundRequest.getAutoUpdateOnfailure());
		boundRequest.setContains(null);
		try {
			con.addObject(sliceRese);
			log.info("The slice information was succesfully stored to the DB");
			
			//this is the information that will be stored in the remote platform
			sliceLimited = new ReservationImpl(sliceURN);
			sliceLimited.setHasLifetimes(IMUtil.createSetWithOneValue(sliceLifetime));
		} catch (RepositoryException e) {
			message = "Problem storing the slice information to the DB";
			errorMessage = ReserveMess.STORE_SLICE_INFORMATION_2DB_FAILED;
			log.warn(message);
			ConnectionClass.logErrorStackToFile(e);
			return null;
		}
		return sliceRese;
		
	}
	
	/**given the sliceID it creates the slice URI
	 * @param sliceID the slice id, is a 32 bits number
	 * @return
	 */
	protected static String createSliceURI(String sliceID)
	{
		return NoviUris.createNoviURI(SLICE_PREFIX + sliceID).toString();
	}
	
	
	
	/**it finds all the platforms URIs that are contained in the slice
	 * @param sliceURI the slice URI
	 * @return the platform's URI in a set
	 */
	public static Set<String> getPlatformsFromSlice(String sliceURI)
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		con.setReadContexts(NoviUris.createURI(sliceURI));
		
		Set<String> results = new HashSet<String>();
		
		try {
			 Result<Platform> platforms = con.getObjects(Platform.class);
			 
			 while(platforms.hasNext())
			 {
				 String st = platforms.next().toString();
				 log.info("I find the platform {} for slice {}", st, sliceURI);
				 results.add(st);
			 }
		} catch (RepositoryException e) {
			ConnectionClass.logErrorStackToFile(e);
		} catch (QueryEvaluationException e) {
			ConnectionClass.logErrorStackToFile(e);
		}
		if (results.size() == 0)
		{
			log.warn("I did not find any platforms for the slice {}", sliceURI);
		}
		ConnectionClass.closeAConnection(con);
		return results;
	}
	
	
	
	
	
	
	/**it produce a unique new id key for a new slice;
	 * (currently is a random number, it needs fix)
	 * @return the ID
	 */
	protected Integer getNewSliceID()
	{
		return IRMLocalDbCalls.createRandomInt();
	}
	

	/**construct the response of the reserve slice
	 * @return
	 */
	private ReserveResponse constructResponse()
	{
		ReserveResponseImp response = new ReserveResponseImp();
		response.setErrorMessage(errorMessage);
		response.setSliceID(sliceID);
		response.setMessage(message);
		response.setLimitedSliceInfo(sliceLimited);
		response.setUserLoginInfo(userLoginInfo);
		return response;
	}
	
	
	/*************************************************************
	********************** Setters & Getters *********************
	*************************************************************/
	
	/*public TestbedCommunication getTestbedComm() {
		return testbedComm;
	}


	public void setTestbedComm(TestbedCommunication testbedComm) {
		this.testbedComm = testbedComm;
	}*/
	
	
	/*************************************************************
	********************** OLD NOT USED CODE*********************
	*************************************************************/
	
/*
	
	*//**it stores the slice resources to the DB.
	 * It stores all the components of the resources and
	 * the implementBy Node triple. The stored resources are also added to the
	 * Set sliceResources. It create the lifetime
	 * @param boundRequest
	 * @return true if succeed, that mean store at least one virtual node
	 *//*
	protected boolean storeSliceResources(final Topology boundRequest)
	{
		Set<Resource> resources = boundRequest.getContains(); //the given resources

		for (Resource resour : resources)
		{
			if (!storeResource(resour))
			{
				return false;
				
			}
			
		}
				
		if(sliceResources.isEmpty())
		{
			log.warn("Noone resource was added to the DB");
			return false;
		}
		return true;
	}
	

	*//**It stores a slice resource to the DB. Also
	 * add the created resource to the sliceResources Set, or
	 * if it fail to the failedResources Set.
	 * @param resource -- the resource. It must have implement by Node,
	 * where Node a physical machine. Currently the
	 * resource should be virtual node. 
	 * @return
	 *//*
	protected boolean storeResource(final Resource resource)
	{
		//check type of resource
		if (! (resource instanceof VirtualNode))
		{
			log.warn("The resource : " + resource.toString() + ", is not a virtual node. " +
					"I will not reserve it");
			failedResources.add(resource);
			return false;
			
		}
		VirtualNode vNode = (VirtualNode) resource;
		
		//check implement by
		Set<Node> nEleNodes = vNode.getImplementedBy();
		if (nEleNodes.isEmpty())
		{
			String s = "The resource : " + resource.toString() + ", has not implementBy node. " +
					"I can't reserve it in the DB";
			log.warn(s);
			failedResources.add(resource);
			message = s;
			errorMessage = ReserveMess.NOT_IMPLEMENT_BY;
			return false;
		}
		
		Node nEleOrNode;
		try {
			nEleOrNode = IMUtil.getOneValueFromSet(nEleNodes);
		} catch (ClassCastException e) {
			String s = "The node " + nEleNodes.toString() + " is not in the DB";
			log.warn(s);
			ConnectionClass.logErrorStackToFile(e);
			message = s;
			errorMessage = ReserveMess.NODE_NOT_IN_DB;
			return false;
		}
		
		if (! (nEleOrNode instanceof Node))
		{
			String s = "The implement by : " + nEleOrNode.toString() + ", is not a Node. " +
					"I will not reserve it in the DB";
			log.warn(s);
			failedResources.add(resource);
			message = s;
			errorMessage = ReserveMess.IMPLEMENT_BY_IS_NOT_NODE;
			return false;
			
		}

		//get the node from the DB
		Node node = null;
		try {
			node = (Node) ConnectionClass.getConnection2DB().
					getObject(nEleOrNode.toString());
			log.debug("I found the node: {}, in the DB ", node);

		} catch (RepositoryException e) {
			String s = "Repository exception : I can't find the node "
		+ nEleOrNode.toString() + " in the DB";
			log.error(s);
			ConnectionClass.logErrorStackToFile(e);
			failedResources.add(resource);
			message = s;
			errorMessage = ReserveMess.NODE_NOT_IN_DB;
			return false;
		}
		catch (ClassCastException e2)
		{
			String s = "I can't find the node " + nEleOrNode.toString() + " in the DB";
			log.warn(s);
			ConnectionClass.logErrorStackToFile(e2);
			failedResources.add(resource);
			message = s;
			errorMessage = ReserveMess.NODE_NOT_IN_DB;
			return false;
		}
		
		
		//store the virtual node object in the db.
		//the URI is the implementBy node URI plus the current datetime
		//Now is stored only the virtual node and not the object that contains
		String vNodeUri = node.toString() + "__" + LocalDbCalls.currentDate().toString();
		try {
			//before to store take out the node components
			//Set<NodeComponent> vNodeComponent = vNode.getHasComponent();
			//vNode.setHasComponent(null);//so don't store arbitrary name objects
			ConnectionClass.getConnection2DB().addObject(vNodeUri, vNode);
			log.debug("The virtual node with URI : {} was stored in the DB", vNodeUri);
			//get the just stored virtual node from the repository
			VirtualNode storedVNode = 
					(VirtualNode) ConnectionClass.getConnection2DB().getObject(vNodeUri);
			//add it to the successful resources list
			sliceResources.add(storedVNode);
			//add lifetime to the virtual node
			storedVNode.setHasLifetimes(IMUtil.createSetWithOneValue(sliceLifetime));
			//add the node components to the DB
			//storeVirtualNodeComponents(vNodeComponent, storedVNode);
			return true;
		} catch (RepositoryException e) {

			ConnectionClass.logErrorStackToFile(e);
		} catch (ClassCastException e)
		{
			ConnectionClass.logErrorStackToFile(e);
			
		}
		String mes = "Problem to store the vNode : " + vNode.toString() + ", to the DB.";
		log.warn(mes);
		failedResources.add(resource);
		message = mes;
		errorMessage = ReserveMess.STORE_VIRTUAL_NODES_DB_ERROR;
		return false;
	}
	
	

	*//**it stores all the node components to the DB
	 * @param nodeComponents -- the node components in the virtual node
	 * in the given bound topology
	 * @param repVNode -- the stored virtual node in the DB
	 *//*
	protected void storeVirtualNodeComponents(Set<NodeComponent> nodeComponents, VirtualNode repVNode)
	{
		String nodeURI = repVNode.toString();
		Set<NodeComponent> storedNodeComponents = new HashSet<NodeComponent>();
		for (NodeComponent nComp : nodeComponents)
		{
			String uri;
			String name;
			NodeComponent tempNodComp;
			if (nComp instanceof CPU)
			{
				uri = nodeURI + "-cpu";
				name = "CPU";
				
			}
			if (nComp instanceof Memory)
			{
				uri = nodeURI + "-memory";
				name = "Memory";
				
			}
			if (nComp instanceof Storage)
			{
				uri = nodeURI + "-storage";
				name = "Storage";
				
			}
			else
			{
				log.warn("Reserve slice: The node component, {}, is not acceptable", nComp.toString());
				continue;
			}
			
			try {
				ConnectionClass.getConnection2DB().addObject(uri, nComp);
				log.debug("The {} component with URI : {} was stored in the DB", name, uri);
				//get the just stored virtual node from the repository
				tempNodComp = (NodeComponent) ConnectionClass.getConnection2DB().getObject(uri);
				storedNodeComponents.add(tempNodComp);
			} catch (RepositoryException e) {
				log.error("Problem storing the node component {}, in the DB", uri);
				log.error(e.getMessage());
			}

			
		}
		repVNode.setHasComponent(storedNodeComponents);
		
	}
	


	*//**it create a Reservation group(slice) that contain all the
	 * virtual nodes of the slice
	 * @return true if succeed
	 *//*
	protected boolean createSlice() 
	{
		if (sliceResources.isEmpty())
		{
			log.warn("There are not slice resources. I will not create the slice group");
			return false;
		}
		URI sliceURI = LocalDbCalls.createNoviURI(SLICE_PREFIX + sliceID);
		Reservation slice = 
				ConnectionClass.getObjectFactory().createObject(sliceURI, Reservation.class);
		try {
			ConnectionClass.getConnection2DB().addObject(sliceURI, slice);
			//set the lifetime
			slice.setHasLifetimes(IMUtil.createSetWithOneValue(sliceLifetime));
			//set the allready stored resources
			slice.setContains(sliceResources);
			sliceGroup = slice;
			log.debug("The slice group, with ID {}, was successfully stored to the DB", sliceID);
			return true;
		} catch (RepositoryException e) {
			log.error("Problem storing the slice group, with ID {}, to the DB", sliceID);
			log.error(e.getMessage());
			//e.printStackTrace();
			return false;
		}

	}*/
	
	

}
