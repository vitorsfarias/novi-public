package eu.novi.federation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import eu.novi.resources.Resource;

/**
 * Represents a federated slice.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 * @author <a href="mailto:pikusa@man.poznan.pl">Piotr Pikusa</a>
 *
 */
public class Slice {
	private Collection<Resource> resources = new ArrayList<Resource>();
	
	private boolean configured = false;
	
	private boolean update = false;
	
	public void addResource(Resource resource) {
		resources.add(resource);
		update = configured;
	}
	
	public boolean isUpdated() {
		return update;
	}
	
	public boolean isConfigured(){
		return configured;
	}
	
	/**
	 * Create or update a slice.
	 * 
	 * @param testbed federation strategy
	 */
	public void configure(FederatedTestbed testbed) {
		testbed.configure(this);
		
		for (Resource resource : resources)
			resource.configure();
		
		configured = true;
	}
	
	/**
	 * Release the resources included in the slice
	 */
	public void releaseResources(){
		for (Resource resource : resources)
			resource.release(); //TODO change it to the the release method!!
		
	}
}
