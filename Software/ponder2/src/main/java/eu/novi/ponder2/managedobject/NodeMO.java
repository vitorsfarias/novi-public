package eu.novi.ponder2.managedobject;

import eu.novi.im.core.impl.NodeImpl;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.*;

public class NodeMO extends NodeImpl implements ManagedObject{
	private String URI;
	@Ponder2op("create:")
    public NodeMO(String uri) {
        super(uri);
		System.out.println("Creating a Node with uri "+ uri);
        URI=uri;               
    }
	 @Ponder2op("access:")
	 void access(int value) {
		 
		//return "AUTH_for_Node";
		}
}
