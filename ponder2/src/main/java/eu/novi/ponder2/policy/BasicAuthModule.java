package eu.novi.ponder2.policy;

import eu.novi.ponder2.SelfManagedCell;
import eu.novi.ponder2.exception.Ponder2AuthorizationException;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.policy.AuthPolicyHolder;
import eu.novi.ponder2.policy.AuthPolicySearch;
import eu.novi.ponder2.policy.AuthorisationModule;
import eu.novi.ponder2.policy.AuthorisationPolicy;
import eu.novi.ponder2.policy.FlexAuthPolicySearch;
import eu.novi.ponder2.policy.StaticAuthPolicySearch;

public class BasicAuthModule extends AuthorisationModule {

	static boolean DEBUG = false;// to print extended amount of info
	static boolean FEEDBACK = false;// to print info strictly for user
									// understanding
	AuthPolicySearch aps = null;

	public BasicAuthModule(String confStrgFile) {
		if (confStrgFile != null)
			aps = new FlexAuthPolicySearch(confStrgFile);
		else
			aps = new StaticAuthPolicySearch();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.novi.ponder2.policy.AuthorisationModule#requestOutgoing(eu.novi.ponder2
	 * .objects.P2OID, eu.novi.ponder2.managedobject.P2ManagedObject,
	 * java.lang.String, eu.novi.ponder2.objects.P2Value[])
	 * 
	 * This method is for PEP1 and PEP2
	 */
	@Override
	public void request(short pepType, char focus, AuthPolicyHolder holder,
			P2Object subjectOID, P2Object targetOID, String operation,
			P2Object[] args) throws Ponder2AuthorizationException {
		print(pepType, subjectOID, targetOID, operation, args);
		short isAuth = -1;
		if (subjectOID.equals(rootDomain)) {
			print(pepType,
					"this action is invoked by the SMC and will be authorised!!",
					operation);
			isAuth = AuthPolicySearch.AUTH;
		} else {
			isAuth = aps.search(holder, pepType, subjectOID, targetOID,
					operation, focus, args, null);
		}
		if (isAuth == AuthPolicySearch.AUTH) {
			print(pepType, "the call is AUTHORIZED", operation);

		} else if (isAuth == AuthPolicySearch.NOTAUTH) {
			print(pepType, "the call is NOT AUTHORIZED", operation);
			throw new Ponder2AuthorizationException(operation);
		} else {
			print(pepType,
					"NO VALID POLICY WAS FOUND. Going for the default auth policy ("
							+ SelfManagedCell.getDefaulAuthPolicy() + ")",
					operation);
			if (!SelfManagedCell.getDefaulAuthPolicy()) {
				printerr(pepType, "the call is NOT AUTHORIZED", operation);
				throw new Ponder2AuthorizationException(operation);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.novi.ponder2.policy.AuthorisationModule#reply(short, char,
	 * eu.novi.ponder2.policy.AuthPolicyHolder,
	 * eu.novi.ponder2.objects.P2Object, eu.novi.ponder2.objects.P2Object,
	 * java.lang.String, eu.novi.ponder2.objects.P2Object[],
	 * eu.novi.ponder2.objects.P2Object)
	 * 
	 * This method is for PEP3 and PEP4
	 */
	@Override
	public void reply(short pepType, char focus, AuthPolicyHolder holder,
			P2Object subjectOID, P2Object targetOID, String operation,
			P2Object[] args, P2Object returnedValue)
			throws Ponder2AuthorizationException {

		print(pepType, subjectOID, targetOID, operation, returnedValue);
		short isAuth;
		if (subjectOID.equals(rootDomain)) {
			print(pepType,
					"this action is invoked by the SMC and will be authorised!!",
					operation);
			isAuth = AuthPolicySearch.AUTH;
		} else {

			AuthorisationPolicy policy;
			if (pepType == AuthorisationModule.PEP3)
				policy = holder.getIncomingAuthPol();
			else if (pepType == AuthorisationModule.PEP4)
				policy = holder.getOutgoingAuthPol();
			else
				throw new Ponder2AuthorizationException(
						"BasicAuthorisationModule.reply: invalid pepType - "
								+ pepType);
			if (policy != null) {
				print(pepType,
						"the request policy is not null. Going to evaluate the return condition!",
						operation);
				isAuth = AuthPolicySearch.evaluate(holder, policy, pepType,
						subjectOID, targetOID, returnedValue);

			} else {
				print(pepType,
						"the request policy is null. I need to invoke the search...",
						operation);
				isAuth = aps.search(holder, pepType, subjectOID, targetOID,
						operation, focus, args, returnedValue);
			}
		}
		if (isAuth == AuthPolicySearch.AUTH) {
			print(pepType, "the call is AUTHORIZED", operation);

		} else if (isAuth == AuthPolicySearch.NOTAUTH) {
			print(pepType, "the call is NOT AUTHORIZED", operation);
			throw new Ponder2AuthorizationException(operation);
		} else {
			print(pepType,
					"NO VALID POLICY WAS FOUND. Going for the default auth policy ("
							+ SelfManagedCell.getDefaulAuthPolicy() + ")",
					operation);
			if (!SelfManagedCell.getDefaulAuthPolicy()) {
				printerr(pepType, "the call is NOT AUTHORIZED", operation);
				throw new Ponder2AuthorizationException(operation);
			}
		}
	}

	private void print(short pepType, String check, String operation) {
		if (DEBUG || FEEDBACK)
			System.out.println("BasicAuthModule for op " + operation + " @"
					+ getName(pepType) + ": " + check);
	}

	private void printerr(short pepType, String check, String operation) {
		if (DEBUG || FEEDBACK)
			System.err.println("BasicAuthModule for op " + operation + " @"
					+ getName(pepType) + ": " + check);
	}

	private void print(short pepType, P2Object sourceOid, P2Object targetOid,
			String operation, P2Object... args) {
		if (DEBUG)
			System.out.println("BasicAuthModule " + getName(pepType) + ": "
					+ sourceOid + " => " + targetOid + " op: " + operation);
	}

	private String getName(short pepType) {
		String result;
		switch (pepType) {
		case PEP1:
			result = "requestOutgoing (PEP1)";
			break;
		case PEP2:
			result = "requestIncoming (PEP2)";
			break;
		case PEP3:
			result = "replyOutgoing (PEP3)";
			break;
		case PEP4:
			result = "replyIncoming (PEP4)";
			break;
		default:
			result = "************************************** unknown PEP value: "
					+ pepType;
			break;
		}
		return result;
	}

}
