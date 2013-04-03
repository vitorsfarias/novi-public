package eu.novi.nswitch.manager.impl;


import java.util.ArrayList;


import eu.novi.im.core.Topology;
import eu.novi.nswitch.manager.Federation;
import eu.novi.nswitch.manager.SliceFederations;


public class SliceFederationsImpl extends ArrayList<Federation> implements SliceFederations{

	private Topology topology;
	private String sliceName;
	
	
	public void setTopology(Topology topology) {
		this.topology = topology;
	}

	
	public Topology getTopology() {
		return topology;
	}

	
	public void setSliceName(String name) {
		sliceName = name;
		
	}

	
	public String getSliceName() {
		// TODO Auto-generated method stub
		return sliceName;
	}
	
}
