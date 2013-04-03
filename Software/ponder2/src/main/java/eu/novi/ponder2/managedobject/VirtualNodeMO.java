package eu.novi.ponder2.managedobject;

import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;

public class VirtualNodeMO extends VirtualNodeImpl implements ManagedObject{

	private String Uri;
	private String Topologyuri;
	

	@Ponder2op("create:topology:")
	public VirtualNodeMO(String vnode,String topology) {
		super(vnode);
		System.out.println("Creating a VirtualNode with uri "+ vnode + " for Slice " + topology);
        Uri=vnode;
        Topologyuri=topology;
	}
	
	@Ponder2op("remove:topology:")
	public void RemoveVirtualNodeMO(String vnode,String topology) {
		System.out.println("Removing the VirtualNode with uri "+ vnode + " from Slice " + topology);
        Uri=vnode;
        Topologyuri=topology;
        try {
			this.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	 @Ponder2op("getTopo:")
	 String getTopo(String vnode) {
		return Topologyuri;
		}
	
	 @Ponder2op("access:")
	 String access(String vnode) {
		return "AUTH_for_Vnode";
		}
	 
	 @Ponder2op("urifailure:")
	 void urifailure(String failure) {
		 System.out.println("We have the failure of the Virtual node "+ Uri +" of topology "+ Topologyuri);
		 
		 }

}
