package eu.novi.ponder2.managedobject;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;

public class SearhingMO implements ManagedObject{
	
	@Ponder2op("create:")
	public void SearchingMO(String name) {
		System.out.println("Creating a ManObj to search for AUTH Policies");
		
	}
	
	@Ponder2op("search:")
	public void search(String name) {
		System.out.println("Creating a ManObj to search for AUTH Policies");
		
	}
	
	
}
