package eu.novi.resources.discovery.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Reservation;
import eu.novi.im.core.Topology;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.resources.discovery.PolicyCalls;
import eu.novi.resources.discovery.database.FindLocalPartitioningCost;
import eu.novi.resources.discovery.database.IRMLocalDbCalls;
import eu.novi.resources.discovery.database.LocalDbCalls;
import eu.novi.resources.discovery.database.NOVIUserClass;
import eu.novi.resources.discovery.database.communic.PolicyServCommun;
import eu.novi.resources.discovery.response.FPartCostTestbedResponseImpl;

/**
 * it implements the services that RIS expose to policy service
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class PolicyCallsImpl implements PolicyCalls {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(PolicyCallsImpl.class);
	 /**
     * Testbed name on which the service is running e.g. PlanetLab, FEDERICA.
     */
    private String testbed; //get for service mix blueprint


    
	
    @Override
	public void initPolicy()
	{
    	log.info("I am going to retrive and send to policy all the slices");
		Set<String> slicesUris = LocalDbCalls.printGetCurrentSlices();
		Set<Reservation> slices = new HashSet<Reservation>();
		for (String uri : slicesUris)
		{
			Reservation slice = IRMLocalDbCalls.getLocalSlice(uri);
			if (slice != null)
			{
				slices.add(slice);
			}
		}
		
		PolicyServCommun.sendAllSlices(slices);
		
	}
	
	

	@Override
	public FPartCostTestbedResponseImpl giveLocalPartitioningCost(
			String requestedTopology) {
		log.info("I got a remote call for the giveLocalPartitioningCost");
		FindLocalPartitioningCost findLocal = new FindLocalPartitioningCost(testbed);
		IMRepositoryUtil imRepo = new IMRepositoryUtilImpl();
		Topology top = imRepo.getIMObjectFromString(requestedTopology, Topology.class);
		if (top == null)
		{
			log.warn("giveLocalPartitioningCost: there is a problem geting the topology object " +
					"from the xml/rdf string : {}", requestedTopology);
			FPartCostTestbedResponseImpl answ = new FPartCostTestbedResponseImpl();
			answ.setTestbedURI(testbed);
			return answ;
			
		}
		return (FPartCostTestbedResponseImpl) findLocal.findLocalPartitioningCost(top);
	}
	
	public String getTestbed() {
		return testbed;
	}

	public void setTestbed(String testbed) {
		this.testbed = testbed;
	}

	@Override
	public String getNoviUser(String sliceURI) {

		log.info("I got a call to getNoviUser for the slice {}", sliceURI);
		NOVIUserImpl user = NOVIUserClass.getNoviUserSlice(sliceURI);

		if (user == null)
		{
			return null;
		}
		else 
		{
			return user.toString();
		}
	}

}
