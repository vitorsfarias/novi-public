package eu.novi.ponder2.policy;

import alice.tuprolog.*;

import java.io.FileNotFoundException;
import java.io.IOException;
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

public class FlexAuthPolicySearch extends AuthPolicySearch {

	private static boolean DEBUG = false;// print all the information
	private static boolean FEEDBACK = false;// print just the labels

	Prolog engine;

	public FlexAuthPolicySearch(String confStrgFile) {
		engine = new Prolog();

		Theory t;
		try {
			// load the prolog file that specifies the strategy for conflict
			// resolution
			System.out
					.println("Loading Conflict resolution strategy from file \""
							+ confStrgFile + "\"");
			t = new Theory(new java.io.FileInputStream(confStrgFile));
			engine.setTheory(t);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidTheoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int getDistance(LinkedList<P2ManagedObject> path,
			P2ManagedObject obj) {

		int pathSize = path.size();
		int index = path.indexOf(obj);
		if (index != -1) {
			return pathSize - index - 1;
		}

		return index;
	}

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

		// these iterators are used to go through the different paths for
		// subject and target
		Iterator<LinkedList<P2ManagedObject>> subjectPathsIter = subjectPath
				.iterator();
		Iterator<LinkedList<P2ManagedObject>> targetPathsIter;

		// these iterators are used for going through a single subject and
		// target path
		Iterator<P2ManagedObject> sbjPathIter, trgPathIter;

		Pair pair;
		P2ManagedObject subject, target; // used to traverse the list of element
											// in the path of the subject and
											// target
		int sbjDist = 0, trgDist = 0;

		LinkedList<P2ManagedObject> nextSbjPath, nextTrgPath;
		// boolean isFirstSbjPath = true, isFirstTrgPath = true;

		// let's iterate through the subject paths
		while (subjectPathsIter.hasNext()) {
			// get the next subject path from the vector
			nextSbjPath = subjectPathsIter.next();
			targetPathsIter = targetPath.iterator();

			// let's iterate through the target paths
			while (targetPathsIter.hasNext()) {
				// initialize the iterator for the sbj path
				sbjPathIter = nextSbjPath.iterator();

				// get the next target path from the vector
				nextTrgPath = targetPathsIter.next();

				pairList = new LinkedList<Pair>();

				while (sbjPathIter.hasNext()) {
					subject = sbjPathIter.next();

					sbjDist = getDistance(nextSbjPath, subject);
					// if(!isFirstSbjPath) sbjDist+=1;

					// initialize the iterator for the sbj path
					trgPathIter = nextTrgPath.iterator();
					while (trgPathIter.hasNext()) {
						target = trgPathIter.next();

						trgDist = getDistance(nextTrgPath, target);
						// if(!isFirstTrgPath) trgDist+=1;

						pair = new Pair(subject, target, sbjDist, trgDist);

						if (!pairList.contains(pair)) {
							pairList.add(pair);
							if (DEBUG)
								System.out
										.println("\tFlexAuthPolicySearch.multiply(): added pair "
												+ pair);
						}
					}
				}

				pathCombination.add(pairList);
				// if(isFirstTrgPath) isFirstTrgPath = false;
			}

			// if (isFirstSbjPath) isFirstSbjPath = false;
			// isFirstTrgPath = true;

		}

		return pathCombination;
	}

	private String getLabels(LinkedList<Pair> pairList, String action,
			char focus, short pepType, P2Object subject, P2Object target,
			P2Object[] args, P2Object result) {
		String labels = "";
		ListIterator<Pair> pairIter;
		Pair currentPair;
		P2ManagedObject subjectMO, targetMO;
		Iterator<AuthorisationPolicy> policyIter;

		pairIter = pairList.listIterator(0);
		while (pairIter.hasNext()) {
			currentPair = pairIter.next();
			if (DEBUG)
				System.out
						.println("\tFlexAuthPolicySearch.getLabels(): going to look for policies defined on pair: "
								+ currentPair);

			subjectMO = currentPair.getFirstElement();
			targetMO = currentPair.getSecondElement();

			// get the iterator on the authorization policy list from the
			// subject
			policyIter = subjectMO.getAuthorisationPolicies().iterator();
			AuthorisationPolicy policy = null;
			while (policyIter.hasNext()) {

				// find a policy that has as target the tOID
				policy = policyIter.next();
				if (((action.equals(policy.getAction())) || ("*".equals(policy
						.getAction()))) && // check the action
						(policy.getTarget() != null) && // the target could be
														// null
						(policy.getTarget().equals(targetMO)) && // check the
																	// target
						(policy.getSubject().equals(subjectMO)) && // check the
																	// subject
						(policy.hasFocus(focus))) {// retrieve the focus

					if (DEBUG)
						System.out
								.println("\t\tFlexAuthPolicySearch.getLabels(): found the following policy: \n\t\t\t"
										+ policy);

					if ((pepType == AuthorisationModule.PEP1)
							|| (pepType == AuthorisationModule.PEP2)) {// check
																		// the
																		// request
																		// condition
						if (policy.isActive()
								&& policy.checkRequestCondition(subject,
										target, args)) {
							if (!labels.equals(""))
								labels += ",";
							labels += generateLabelForPolicy(policy,
									currentPair, "rq");
						}
					} else if ((pepType == AuthorisationModule.PEP3)
							|| (pepType == AuthorisationModule.PEP4)) {// otherwise
																		// check
																		// the
																		// reply
																		// condition
						if (policy.isActive()
								&& policy.checkReturnCondition(subject, target,
										result)) {
							if (!labels.equals(""))
								labels += ",";
							labels += generateLabelForPolicy(policy,
									currentPair, "rp");
						}
					}
				}
			}// close the while-loop through the policies for a given subject
		}// close the while-loop through the pair list
		if (DEBUG)
			System.out
					.println("\t\tFlexAuthPolicySearch.getLabels(): returning the following labels "
							+ labels);
		return labels;

	}

	private String generateLabelForPolicy(AuthorisationPolicy policy,
			Pair currentPair, String direction) {

		int totalDist = -1, sbjDist = -1;
		String label = "(";
		// check whether it is a Final policy
		if (policy.isFinal())
			label += "f,";
		else
			label += "n,";

		// get distance between subject and target
		totalDist = currentPair.getSubjectDist() + currentPair.getTargetDist();
		// get distance fo subject from root
		sbjDist = currentPair.getSubjectDist();
		// add distance information into the label
		label += Integer.toString(totalDist) + "," + Integer.toString(sbjDist)
				+ ",";

		if ("rq".equals(direction)) {// if it is a request
			if (policy.isAuthRequestNeg())
				// if negative add 'n' to the label
				label += "n)";
			else
				// otherwise add 'p'
				label += "p)";
		} else if ("rp".equals(direction)) {// if it is a reply
			if (policy.isAuthReplyNeg())
				// if negative add 'n' to the label
				label += "n)";
			else
				// otherwise add 'p'
				label += "p)";

		}
		System.out
				.println("FlexAuthPolicySearch.generateLabelForPolicy(): generated label is "
						+ label);

		return label;
	}

	char CLPStrategyEvaluation(String labels) {
		char evaluationResult = 'd';
		SolveInfo answer;

		String queryStart = "quicksort([";
		String queryEnd = "],X).";

		String queryS = queryStart + labels + queryEnd;

		try {
			answer = engine.solve(queryS);

			Term result = answer.getTerm("X");
			if (DEBUG || FEEDBACK)
				System.out
						.println("\tFlexAuthPolicySearch.CLPStrategyEvaluation(): This is the result of the query: "
								+ result);

			char[] cArray = result.toString().toCharArray();
			for (int i = (cArray.length - 1); i > 0; i--) {
				if (cArray[i] == 'p') {
					evaluationResult = 'p';
					break;
				} else if (cArray[i] == 'n') {
					evaluationResult = 'n';
					break;
				}
			}
		} catch (MalformedGoalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownVarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (DEBUG || FEEDBACK)
			System.out
					.println("\tFlexAuthPolicySearch.CLPStrategyEvaluation(): the evaluation result label is "
							+ evaluationResult);

		return evaluationResult;

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
	public synchronized short search(AuthPolicyHolder holder, short pepType,
			P2Object subject, P2Object target, String action, char focus,
			P2Object[] args, P2Object result) {
		short conditionState = AuthPolicySearch.POL_NOT_DEFINED;
		if (DEBUG) {
			System.out
					.println("\n\n\nFlexAuthPolicySearch.search(): New Search started @ PEP"
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
		Vector<LinkedList<P2ManagedObject>> subjectPath = null;
		Vector<LinkedList<P2ManagedObject>> targetPath = null;

		if (DEBUG) {
			System.out
					.println("FlexAuthPolicySearch.search(): going to build the path for subject");
		}
		subjectPath = AuthPolicySearch.buildPath(subject.getManagedObject());

		if (DEBUG) {
			System.out
					.println("FlexAuthPolicySearch.search(): going to build the path for target");
		}
		targetPath = AuthPolicySearch.buildPath(target.getManagedObject());

		Vector<LinkedList<Pair>> pathCombination = null;
		if ((!subjectPath.isEmpty()) && (!targetPath.isEmpty())) {
			if (DEBUG) {
				System.out
						.println("AuthPolicySearch.search(): going to multiply subject by target paths");
			}

			pathCombination = multiply(subjectPath, targetPath);
			Iterator<LinkedList<Pair>> pathCombIter = pathCombination
					.iterator();
			String labels = "";
			while (pathCombIter.hasNext()) {

				if (DEBUG || FEEDBACK) {
					System.out
							.println("FlexAuthPolicySearch.search(): going to build policy labels");
				}
				// get the labels for a subject path and target path combination
				String labelPerPath = getLabels(pathCombIter.next(), action,
						focus, pepType, subject, target, args, result);
				if (DEBUG || FEEDBACK) {
					System.out
							.println("FlexAuthPolicySearch.search(): the constructed labels per a given path are: '"
									+ labelPerPath + "'");
				}
				if (!labels.equals(""))
					labels += ',';

				labels += CLPStrategyEvaluation(labelPerPath);
			}
			if (DEBUG || FEEDBACK) {
				System.out
						.println("FlexAuthPolicySearch.search(): the labels for all the paths are: '"
								+ labels + "'");
			}
			char authorisationStateLabel = 'd';
			if (labels.length() > 1)
				authorisationStateLabel = CLPStrategyEvaluation(labels);
			else if (labels.length() == 1)
				authorisationStateLabel = labels.charAt(0);
			if (authorisationStateLabel == 'p')
				conditionState = AuthPolicySearch.AUTH;
			else if (authorisationStateLabel == 'n')
				conditionState = AuthPolicySearch.NOTAUTH;
			if (DEBUG || FEEDBACK) {
				System.out
						.println("FlexAuthPolicySearch.search(): the authorisation state label is "
								+ authorisationStateLabel);
			}
		}

		return conditionState;
	}

}
