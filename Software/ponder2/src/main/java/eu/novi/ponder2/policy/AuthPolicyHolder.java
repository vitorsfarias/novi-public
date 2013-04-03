package eu.novi.ponder2.policy;

import eu.novi.ponder2.policy.AuthorisationPolicy;

/**
 * @author gio This is a class that holds the policy that was found using the
 *         search method. This is done at PEP1 and PEP2. Once the policy is
 *         found, this is stored in the holder to be used for the respective
 *         returning part (PEP4 and PEP3)
 */
public class AuthPolicyHolder {

	/**
	 * the policy for the incoming request (PEP2)
	 */
	private AuthorisationPolicy incomingAuthPol;

	/**
	 * the policy for the outgoing request (PEP1)
	 */
	private AuthorisationPolicy outgoingAuthPol;

	public AuthPolicyHolder() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * to retrieve the policy used in the incoming part of a request
	 * 
	 * @return the policy
	 */
	public AuthorisationPolicy getIncomingAuthPol() {
		return incomingAuthPol;
	}

	/**
	 * to set the policy used in the incoming request
	 * 
	 * @param authPol
	 *            the policy to be held
	 */
	public void setIncomingAuthPol(AuthorisationPolicy authPol) {
		this.incomingAuthPol = authPol;
	}

	/**
	 * to retrieve the policy used in the outgoing part of a request
	 * 
	 * @return the policy
	 */
	public AuthorisationPolicy getOutgoingAuthPol() {
		return outgoingAuthPol;
	}

	/**
	 * to set the policy used for the outgoing part of the request
	 * 
	 * @param outgoingAuthPol
	 */
	public void setOutgoingAuthPol(AuthorisationPolicy outgoingAuthPol) {
		this.outgoingAuthPol = outgoingAuthPol;
	}

}