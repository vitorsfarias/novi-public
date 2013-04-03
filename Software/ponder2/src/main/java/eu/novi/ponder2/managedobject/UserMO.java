package eu.novi.ponder2.managedobject;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.StartStopPonder2SMC;
public class UserMO implements ManagedObject{
	
	//Should extend the User from the IM
	private P2Object myP2Object;
	private String Name;
	@Ponder2op("create:")
	public UserMO(P2Object myP2Object, String uri) {
		 Name =uri;
		 this.myP2Object = myP2Object;
	}
	
	@Ponder2op("search:")
	public Boolean search(P2Object resource) {
		System.out.println("Searching");
		int value = 1;					
		try {
			resource.operation(myP2Object, "access:", P2Object.create(value));
			return true;
			
		} catch (Ponder2AuthorizationException e) {
			return false;
		} catch (Ponder2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("No such object");
		return false;
		
	}

}
