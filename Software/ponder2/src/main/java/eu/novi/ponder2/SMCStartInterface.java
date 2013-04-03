package eu.novi.ponder2;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;

public interface SMCStartInterface {
	
	/**
	 * Create new Node(Managed Object) in the SMC
	 * @param PlNode
	 * @return id of the Node
	 */
	public void start();
	//public PlNode createNode(String newNode, String slice);
	//public P2Object createEvent(P2Object thisObj, ManagedObject obj,
	//		P2Object source, String operation, P2Object... args) throws Ponder2Exception;
	public void stop();
	
	public Boolean searchauth(AuthPolicyHolder holder, short pepType,
			P2Object subject, P2Object target, String action, char focus,
			P2Object[] args, P2Object result);
	
	public Boolean searchauth(P2Object myP2Object, P2Object resource);

}
