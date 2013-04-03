package eu.novi.nswitch.manager;



import java.util.List;

import eu.novi.im.core.Topology;

public interface SliceFederations extends List<Federation>{

	public void setTopology(Topology topology);
	
	public Topology getTopology();
	
	public void setSliceName(String name);
	
	public String getSliceName();
}
