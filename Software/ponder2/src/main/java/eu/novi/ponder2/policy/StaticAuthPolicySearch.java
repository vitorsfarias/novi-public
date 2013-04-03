package eu.novi.ponder2.policy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import eu.novi.ponder2.P2ManagedObject;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.Pair;

public class StaticAuthPolicySearch extends AuthPolicySearch {

	private static final boolean DEBUG = false;

	/**
	 * This method generates the combinations of pairs from the oid elements in
	 * the paths of the subject and target. For a given subject path, its
	 * elements (OID) are "multiplied" by the elements (OID) of all target's
	 * paths. This is repeated for all subject's paths. The pairs are stored in
	 * a linked list. The linked list is stored in the pathCombination. Example:
	 * subject path {(/,a,b)} where /, a, b are domains target path {(/,c,d);
	 * (/,e)} the multiplication of (/,a,b) by (/,c,d) gives the following pairs
	 * (that are all stored in the same linked list): (/,/) (/,c) (/,d) (a,/)
	 * (a,c) (a,d) and so on... then the subject path (/,a,b) is multiplied by
	 * the target path (/,e) and the pairs generated are stored in another
	 * linked list. All linked list are stored in the vector pathCombination.
	 */
	private Vector<LinkedList<Pair>> multiply(
			Vector<LinkedList<P2ManagedObject>> subjectPath,
			Vector<LinkedList<P2ManagedObject>> targetPath) {

		Vector<LinkedList<Pair>> pathCombination = new Vector<LinkedList<Pair>>();

		LinkedList<Pair> pairList; // contains the pairs for a subject and
									// target path product

		Iterator<LinkedList<P2ManagedObject>> subjectIter = subjectPath
				.iterator();
		Iterator<LinkedList<P2ManagedObject>> targetIter;
		Iterator<P2ManagedObject> sbjPathIter, trgPathIter;
		Pair pair;
		P2ManagedObject subject, target; // used to traverse the list of element
											// in the path of the subject and
											// target

		LinkedList<P2ManagedObject> nextSbjPath, nextTrgPath;
		// let's iterate through the subject paths
		while (subjectIter.hasNext()) {
			// get the next subject path from the vector
			nextSbjPath = subjectIter.next();
			targetIter = targetPath.iterator();
			// let's iterate through the target paths
			while (targetIter.hasNext()) {
				// initialize the iterator for the sbj path
				sbjPathIter = nextSbjPath.iterator();

				// get the next target path from the vector
				nextTrgPath = targetIter.next();

				pairList = new LinkedList<Pair>();

				while (sbjPathIter.hasNext()) {
					subject = sbjPathIter.next();

					// initialize the iterator for the sbj path
					trgPathIter = nextTrgPath.iterator();
					while (trgPathIter.hasNext()) {
						target = trgPathIter.next();
						pair = new Pair(subject, target);

						if (!pairList.contains(pair)) {
							pairList.add(pair);
							if (DEBUG)
								System.out
										.println("\tAuthPolicySearch.multiply(): added pair "
												+ pair);
						}
					}
				}

				pathCombination.add(pairList);
			}

		}

		return pathCombination;
	}

	/**
	 * This method searches for policy candidates for a given subject and target
	 * path combinations. The method looks up for all policies that applies to
	 * the given action. For a given pair, the method search for all policies,
	 * final and normal. Final policies are stored in the given vector
	 * finalPolicyCandidate with the outermost final policy stored in the head
	 * of the vector. Normal policies are stored in the given vector
	 * normalPolicyCandidate, with the innermost policy at the end of the
	 * vector. For a given pair, negative policies are stored in the respective
	 * vector with a higher priority respect to positive ones.
	 * 
	 * Note that this method does not check whether the policy condition is true
	 * or not.
	 * 
	 * @param pairList
	 *            - the list of pairs for a given subject and target path
	 * @param action
	 *            - the action that should be authorized
	 * @param finalPolicyCandidate
	 *            - vector of all *final* policies that are available for this
	 *            action the policy are ordered according to their priority,
	 *            with the outermost policy at the head of the vector.
	 * @param normalPolicyCandidate
	 *            - vector of all *normal* policies that are available for this
	 *            action the policies are contained in the vector in the inverse
	 *            order of priority with the highest priority policy at the end
	 *            of the vector.
	 */
	private void findCandidatePolicy(LinkedList<Pair> pairList, String action,
			char focus, Vector<AuthorisationPolicy> finalPolicyCandidate,
			Vector<AuthorisationPolicy> normalPolicyCandidate) {
		if (DEBUG)
			System.out
					.println("\tAuthPolicySearch.findCandidatePolicy(): BEFORE FIND THE NORMAL POLICY VECTOR IS "
							+ printVector(normalPolicyCandidate));
		Vector<AuthorisationPolicy> tmpFinalNeg = new Vector<AuthorisationPolicy>(), tmpFinalPos = new Vector<AuthorisationPolicy>(); // contains
																																		// the
																																		// negative
																																		// and
																																		// positive
																																		// FINAL
																																		// policy
																																		// for
																																		// a
																																		// given
																																		// pair.
		Vector<AuthorisationPolicy> tmpNeg = new Vector<AuthorisationPolicy>(), tmpPos = new Vector<AuthorisationPolicy>();// contain
																															// the
																															// negative
																															// and
																															// positive
																															// NORMAL
																															// policy
																															// for
																															// a
																															// given
																															// pair.

		ListIterator<Pair> pairIter;
		Pair currentPair;
		P2ManagedObject subject, target;
		Iterator<AuthorisationPolicy> policyIter;

		pairIter = pairList.listIterator(0);
		while (pairIter.hasNext()) {
			currentPair = pairIter.next();
			if (DEBUG)
				System.out
						.println("\tAuthPolicySearch.findCandidatePolicy(): going to serach for policy on pair: "
								+ currentPair);

			subject = currentPair.getFirstElement();
			target = currentPair.getSecondElement();

			// get the iterator on the authorization policy list from the
			// subject
			policyIter = subject.getAuthorisationPolicies().iterator();
			AuthorisationPolicy policy = null;
			while (policyIter.hasNext()) {

				// find a policy that has as target the tOID
				policy = policyIter.next();
				if (((action.equals(policy.getAction())) || ("*".equals(policy
						.getAction()))) && // check the action
						(policy.getTarget() != null) && // the target could be
														// null
						(policy.getTarget().equals(target)) && // check the
																// target
						(policy.getSubject().equals(subject)) && // check the
																	// subject
						(policy.hasFocus(focus))) {// retrieve the focus

					// check whether it is a Final policy
					if (policy.isFinal()) {
						if (policy.isAuthRequestNeg())
							// if negative put it in the negative vector
							tmpFinalNeg.add(policy);
						if (DEBUG)
							System.out
									.println("\t\tAuthPolicySearch.findCandidatePolicy(): added negative final  policy: \n\t\t\t"
											+ policy);

						else
							// otherwise put it in the positive vector
							tmpFinalPos.add(policy);
						if (DEBUG)
							System.out
									.println("\t\tAuthPolicySearch.findCandidatePolicy(): added  final  policy: \n\t\t\t"
											+ policy);

					} else {
						// otherwise the policy is a normal
						if (policy.isAuthRequestNeg()) {
							// if negative put it in the negative vector
							tmpNeg.add(policy);
							if (DEBUG)
								System.out
										.println("\t\tAuthPolicySearch.findCandidatePolicy(): added negative normal policy: \n\t\t\t"
												+ policy);
						} else {
							// otherwise put it in the positive vector
							tmpPos.add(policy);
							if (DEBUG)
								System.out
										.println("\t\tAuthPolicySearch.findCandidatePolicy(): added normal policy: \n\t\t\t"
												+ policy);
						}
					}
				}
			}
			finalPolicyCandidate.addAll(tmpFinalNeg);
			finalPolicyCandidate.addAll(tmpFinalPos);
			normalPolicyCandidate.addAll(tmpPos);
			normalPolicyCandidate.addAll(tmpNeg);
			tmpFinalNeg.removeAllElements();
			tmpFinalPos.removeAllElements();
			tmpNeg.removeAllElements();
			tmpPos.removeAllElements();

		}

	}

	/**
	 * This method is used to go through the list of final policy candidates to
	 * find the final policy that is applicable.
	 * 
	 * @param finalPolicyCandidate
	 *            vector of all the final policies found for the
	 *            (subject,target,action) triple
	 * @param holder
	 *            the holder used for the returning part
	 * @param pepType
	 *            the type of policy to be searched for
	 * @param subject
	 *            the subject of the access
	 * @param target
	 *            the target of the access
	 * @param args
	 *            TaggedElement that contains the attribute to evaluate the
	 *            condition defined for the policy
	 * @param result
	 *            The standard Result used in Ponder calls
	 * @return a short that represents AUTH, NOTAUTH, or NOT_APPLICABLE
	 */
	private short findFinalPolicy(
			Vector<AuthorisationPolicy> finalPolicyCandidate,
			AuthPolicyHolder holder, short pepType, P2Object subject,
			P2Object target, P2Object[] args, P2Object result) {

		AuthorisationPolicy policy = null;
		short conditionState = AuthPolicySearch.POL_NOT_DEFINED;
		for (int i = 0; i < finalPolicyCandidate.size(); i++) {
			policy = finalPolicyCandidate.elementAt(i);
			if (pepType == AuthorisationModule.PEP1
					|| pepType == AuthorisationModule.PEP2)
				conditionState = evaluate(holder, policy, pepType, subject,
						target, args);
			else if (pepType == AuthorisationModule.PEP3
					|| pepType == AuthorisationModule.PEP4)
				conditionState = evaluate(holder, policy, pepType, subject,
						target, result);
			if (conditionState != AuthPolicySearch.POL_NOT_DEFINED) {
				return conditionState;
			}
		}

		return conditionState;
	}

	/**
	 * As findFinalPolicy, only that this is for normal policies
	 * 
	 * @param normalPolicyCandidate
	 *            vector of all the normal policies found for the
	 *            (subject,target,action) triple
	 * @param pepType
	 *            the type of policy to be searched for
	 * @param subject
	 *            the subject of the access
	 * @param target
	 *            the target of the access
	 * @param args
	 *            TaggedElement that contains the attribute to evaluate the
	 *            condition defined for the policy
	 * @param result
	 *            The standard Result used in Ponder calls
	 * @return a short that represents AUTH, NOTAUTH, or NOT_APPLICABLE
	 */
	private short findNormalPolicy(
			Vector<AuthorisationPolicy> normalPolicyCandidate,
			AuthPolicyHolder holder, short pepType, P2Object subject,
			P2Object target, P2Object[] args, P2Object result) {

		AuthorisationPolicy policy = null;
		short conditionState = AuthPolicySearch.POL_NOT_DEFINED;

		for (int i = normalPolicyCandidate.size(); i > 0; i--) {
			policy = normalPolicyCandidate.elementAt(i - 1);
			if (pepType == AuthorisationModule.PEP1
					|| pepType == AuthorisationModule.PEP2)
				conditionState = evaluate(holder, policy, pepType, subject,
						target, args);
			else if (pepType == AuthorisationModule.PEP3
					|| pepType == AuthorisationModule.PEP4)
				conditionState = evaluate(holder, policy, pepType, subject,
						target, result);
			if (conditionState != AuthPolicySearch.POL_NOT_DEFINED) {
				return conditionState;
			}
		}
		return conditionState;

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
	 * @return the condition state - AUTH, NOTAUTH or POL_NOT_DEFINED
	 */
	@Override
	public short search(AuthPolicyHolder holder, short pepType,
			P2Object subject, P2Object target, String action, char focus,
			P2Object[] args, P2Object result) {
		if (DEBUG) {
			System.out
					.println("\n\n\nAuthPolicySearch.search(): New Search started @ PEP"
							+ pepType
							+ " for tuple \n"
							+ "\t\tsubject:"
							+ subject
							+ "("
							+ subject.getOID()
							+ ")\n"
							+ "\t\ttarget:"
							+ target
							+ "("
							+ target.getOID()
							+ "),\n" + "\t\taction:" + action);

		}

		short conditionState = AuthPolicySearch.POL_NOT_DEFINED;

		Vector<LinkedList<P2ManagedObject>> subjectPath = null;
		Vector<LinkedList<P2ManagedObject>> targetPath = null;

		if (DEBUG) {
			System.out
					.println("AuthPolicySearch.search(): going to build the path for subject");

		}
		subjectPath = buildPath(subject.getManagedObject());
		if (DEBUG) {
			System.out
					.println("AuthPolicySearch.search(): going to build the path for target");

		}
		targetPath = buildPath(target.getManagedObject());

		if ((!subjectPath.isEmpty()) && (!targetPath.isEmpty())) {
			if (DEBUG) {
				System.out
						.println("AuthPolicySearch.search(): going to multiply subject by target paths");

			}

			Vector<LinkedList<Pair>> pathCombination = null;
			pathCombination = multiply(subjectPath, targetPath);

			short tmpConState;

			Iterator<LinkedList<Pair>> pathCombIter = pathCombination
					.iterator();
			while (pathCombIter.hasNext()) {
				/*
				 * For each subject-target path combination this vector contains
				 * the FINAL authorization policies that could be used to
				 * authorized a given action. The condition of the policies has
				 * not been checked yet at the time when the policy is stored in
				 * the vector. The highest priority policy is stored at the
				 * beginning of the vector.
				 */
				Vector<AuthorisationPolicy> finalPolicyCandidate = new Vector<AuthorisationPolicy>();
				/*
				 * As for the finalPolicyVector, only this vector contains
				 * normal policies. The policy with the highest priority is
				 * stored at the end of the vector
				 */
				Vector<AuthorisationPolicy> normalPolicyCandidate = new Vector<AuthorisationPolicy>();
				if (DEBUG) {
					System.out
							.println("AuthPolicySearch.search(): going to find candidate policies");

				}
				findCandidatePolicy(pathCombIter.next(), action, focus,
						finalPolicyCandidate, normalPolicyCandidate);
				if (DEBUG) {

					System.out
							.println("AuthPolicySearch, search: the vector of final policies for focus "
									+ focus
									+ " is \n\t"
									+ printVector(finalPolicyCandidate));
					System.out
							.println("AuthPolicySearch, search: the vector of normal policies for focus "
									+ focus
									+ " is \n\t"
									+ printVector(normalPolicyCandidate));
				}
				// First let's find a final policy
				tmpConState = findFinalPolicy(finalPolicyCandidate, holder,
						pepType, subject, target, args, result);

				// let's check the condition
				if (tmpConState == NOTAUTH) {
					/*
					 * if we found a final policy that does not authorize the
					 * action we can return this condition!!
					 */
					return NOTAUTH;
				} else if (tmpConState == AUTH) {
					/*
					 * If a final policy authorizes the action then we save the
					 * state but keep looking in other paths. It might be the
					 * case that a negative authorization can be given
					 */
					conditionState = AUTH;
				} else if (tmpConState == POL_NOT_DEFINED) {
					/*
					 * Otherwise, if no final policy was found, let's check for
					 * a normal
					 */
					tmpConState = findNormalPolicy(normalPolicyCandidate,
							holder, pepType, subject, target, args, result);
					// let's check the state
					if (tmpConState == NOTAUTH) {
						return NOTAUTH;
					} else if (tmpConState == AUTH) {
						/*
						 * If a normal policy authorizes the action then we save
						 * the state but keep looking in other paths. It might
						 * be the case that a negative authorization can be
						 * given
						 */
						conditionState = AUTH;
					}
				}
			}

		}

		return conditionState;
	}

}
