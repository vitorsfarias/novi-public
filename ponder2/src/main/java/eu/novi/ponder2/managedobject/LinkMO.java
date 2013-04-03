package eu.novi.ponder2.managedobject;

import eu.novi.im.core.impl.LinkImpl;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;

public class LinkMO extends LinkImpl  implements ManagedObject{
	private String Uri;
	private String Topologyuri;
	
	@Ponder2op("create:topology:")
    public LinkMO(String link,String topology) {
        super(link);
		System.out.println("Creating a Link");
        Uri=link;
        Topologyuri=topology;
        
    }
	
	 @Ponder2op("linkfailure:")
	    void linkfailure(String urifailure) {
	    	System.out.println("We have a link failure");
	    	}
}
