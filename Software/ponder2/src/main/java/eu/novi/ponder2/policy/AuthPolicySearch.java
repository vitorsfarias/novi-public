package eu.novi.ponder2.policy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.AuthorisationPolicy;

/**
 * 
 * Abstract class for implementing the Strategy Pattern. This class provides
 * some basic method used by other classes to search for an applicable
 * authorization policy taking into account the conflict resolution described in
 * more detail in the Policy07 paper.
 * 
 * The following steps are executed: - Step 0: First, it builds and stores all
 * the possible paths in which the subject and the target are contained
 * 
 * - Step 1: For each combination subject_path and target_path, it searches for
 * a candidate policy that could be applied. First, it searches for the
 * outermost (= most general) final policy. If not final policies are available
 * then the innermost (= most specific) normal policy is searched. If for a
 * given path combination a negative policy is found, then the search is
 * concluded and the action is not authorized. Otherwise, the policy is stored
 * and the search continues with the next path combination. The action is
 * authorized if for all path combinations at least one positive policy is
 * found.
 * 
 * If there is no applicable policy (either because none have been specified or
 * because the condition failed) then the general default rule is applied(ALL+,
 * ALL-).
 * 
 * 
 * Version 2 - 13 February 2008
 * 
 * @author russello
 * 
 * 
 */
public abstract class AuthPolicySearch {

	public final static short AUTH = 0;
	public final static short NOTAUTH = 1;
	public final static short POL_NOT_DEFINED = 2;

	private static boolean DEBUG = false;

	/**
	 * Given an OID, the method constructs all the paths of that managed object
	 * up to the root domain. The paths are stored in the given vector. A path
	 * is a vector that contains the OID representation of each element in the
	 * path of the managed object.
	 * 
	 * @param mObj
	 *            the managed object
	 * @return the vector containing all the paths
	 */
	static protected Vector<LinkedList<P2ManagedObject>> buildPath(
			P2ManagedObject mObj) {
		Vector<LinkedList<P2ManagedObject>> path = new Vector<LinkedList<P2ManagedObject>>();

		if (mObj == null) {
			System.err
					.println("Class AuthPolicySearch, buildPath: the managed object is null");
			return path;
		}

		LinkedList<P2ManagedObject> currentPath = null;
		// let's check whether this is the root which does not have parent
		// domain(s).
		if (mObj.getOID().equals(SelfManagedCell.RootDomain.getOID())) {
			if (DEBUG)
				System.out
						.println("AuthPolicySearch.buildPath(): this is the root domain");
			currentPath = new LinkedList<P2ManagedObject>();
			currentPath.add(mObj);
			path.add(currentPath);
			return path;

		}
		// set the iterator to the parent set of the managed object.
		Iterator<P2ManagedObject> parentIter = mObj.getParentSet().iterator();

		P2ManagedObject parentObject = null;
		while (parentIter.hasNext()) {
			currentPath = new LinkedList<P2ManagedObject>();
			parentObject = parentIter.next();
			// let's follow the path up to the root.
			// we hit the root when the parentSet is empty and we have the real
			// registered root
			while (!parentObject.getParentSet().isEmpty()
					&& !parentObject.getOID().equals(
							SelfManagedCell.RootDomain.getOID())) {
				currentPath.addFirst(parentObject);
				if (DEBUG)
					System.out
							.println("\tAuthPolicySearch.buildPath(): just added: "
									+ parentObject.getOID());
				parentObject = parentObject.getParentSet().iterator().next();
			}
			// let's add the root domain in the linked list
			currentPath.addFirst(parentObject);
			if (DEBUG)
				System.out
						.println("\tAuthPolicySearch.buildPath(): just added: "
								+ parentObject.getOID());
			currentPath.addLast(mObj);
			// let's store the current path into the vector of paths
			path.add(currentPath);
		}

		// we should add the object oid in one of its path
		// we will use the first (position 0)in the vector
		// and add the oid at the end of the list
		// if(path.size()>0) path.elementAt(0).addLast(mObj);
		// if(DEBUG)
		// System.out.println("\tAuthPolicySearch.buildPath(): just added: "
		// +mObj.getOID());
		return path;

	}

	/**
	 * This method is used to evaluate a given policy. The method fist checks
	 * whether the given policy is active and the condition is true. If this
	 * condition fails then the policy is not applicable. Otherwise, if the
	 * policy is a type PEP1 and is not negative then the policy is stored in
	 * the holder to be used for the return part (PEP4).
	 * 
	 * @param holder
	 *            stores the policy for the return part
	 * @param policy
	 *            the policy to be evaluated
	 * @param pepType
	 *            the PEP that is triggering this evaluation
	 * @param target
	 *            TODO
	 * @param argAttribute
	 *            the attribute Map used for evaluating the condition
	 * @return a short that represents AUTH, NOT_AUTH, or NOT_DEFINED
	 */
	protected static short evaluate(AuthPolicyHolder holder,
			AuthorisationPolicy policy, short pepType, P2Object subject,
			P2Object target, P2Object... argAttribute) {

		short result = AuthPolicySearch.POL_NOT_DEFINED;

		if ((pepType == AuthorisationModule.PEP1)
				|| (pepType == AuthorisationModule.PEP2)) {// check the request
															// condition
			if (policy.isActive()
					&& policy.checkRequestCondition(subject, target,
							argAttribute)) {
				if (!policy.isAuthRequestNeg()) {
					// if the policy is not negative store it to be used at PEP3
					if (pepType == AuthorisationModule.PEP1) {
						holder.setOutgoingAuthPol(policy);
					} else {
						holder.setIncomingAuthPol(policy);
					}
					if (DEBUG)
						System.out
								.println("\tAuthPolicySearch, evaluate @ PEP"
										+ pepType
										+ ": "
										+ "A VALID POLICY WAS FOUND.\n "
										+ "\t\tThe policy is an AUTH+ and the request condition is true.\n "
										+ "\t\tThe OPERATION "
										+ policy.getAction()
										+ " WILL BE AUTHORIZED");
					result = AuthPolicySearch.AUTH;
				} else {
					// otherwise the action is NOTAUTHorized
					if (DEBUG)
						System.out
								.println("\tAuthPolicySearch, evaluate @ PEP"
										+ pepType
										+ ": "
										+ "A VALID POLICY WAS FOUND.\n "
										+ "\t\tThe policy is an AUTH- and the request condition is true.\n "
										+ "\t\tThe OPERATION "
										+ policy.getAction()
										+ " WILL BE NOT AUTHORIZED");
					result = AuthPolicySearch.NOTAUTH;
				}
			}
		} else if ((pepType == AuthorisationModule.PEP3)
				|| (pepType == AuthorisationModule.PEP4)) {// otherwise check
															// the reply
															// condition
			if (policy.isActive()
					&& policy.checkReturnCondition(subject, target,
							argAttribute)) {
				if (!policy.isAuthReplyNeg()) {
					if (DEBUG)
						System.out
								.println("\tAuthPolicySearch, evaluate @ PEP"
										+ pepType
										+ ": "
										+ "A VALID POLICY WAS FOUND.\n "
										+ "\t\tThe policy is an AUTH+ and the return condition is true.\n "
										+ "\t\tThe OPERATION "
										+ policy.getAction()
										+ " WILL BE AUTHORIZED");
					result = AuthPolicySearch.AUTH;
				} else {
					if (DEBUG)
						System.out
								.println("\tAuthPolicySearch, evaluate @ PEP"
										+ pepType
										+ ": "
										+ "A VALID POLICY WAS FOUND.\n "
										+ "\\ttThe policy is an AUTH- and the return condition is true.\n "
										+ "\t\tThe OPERATION "
										+ policy.getAction()
										+ " WILL BE NOT AUTHORIZED");
					result = AuthPolicySearch.NOTAUTH;
				}

			}
		}

		return result;
	}

	/**
	 * This is the main method called externally for performing the search of an
	 * applicable policy.
	 * 
	 * @param holder
	 *            The holder vector is used to hold policy used in PEP1 and PEP2
	 *            to be used also in the respective returning part
	 * @param pepType
	 *            specifies the PEP type
	 * @param subject
	 *            the subject OID
	 * @param target
	 *            the target OID
	 * @param action
	 *            the action performed by the subject on the target
	 * @param focus
	 *            whether the policy that should be found is a subject or a
	 *            target policy
	 * @param args
	 *            the arguments used for the evaluation of the condition
	 * @param result
	 *            the standard Result used in Ponder
	 * @return AUTH or NOTAUTH
	 */
	abstract public short search(AuthPolicyHolder holder, short pepType,
			P2Object subject, P2Object target, String action, char focus,
			P2Object[] args, P2Object result);

	public static String printVector(Vector<AuthorisationPolicy> policyList) {
		String slist = "[";

		for (Iterator<AuthorisationPolicy> policyIter = policyList.iterator(); policyIter
				.hasNext();) {
			slist += policyIter.next() + "\n";

		}
		slist += "]";
		return slist;
	}

	public static String printState(short state) {

		if (AUTH == state) {
			return "The subject is AUTHORIZED";
		} else if (NOTAUTH == state) {
			return "The subject is NOTAUTHORIZED";

		} else if (POL_NOT_DEFINED == state) {
			return "NO policy was DEFINED for the subject+target+action combo";
		} else {
			return "the value " + state + " is unknown";
		}
	}

}
